package com.example.core.learning.engine

import com.example.core.database.CardEntity
import com.example.core.database.UserDao
import com.example.core.database.VocabularyDatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * The Smart Session Engine facade that coordinates capacity estimates,
 * time tracking, priority queueing, CEFR matching, and on-demand card creation.
 */
class SmartSessionEngine(
    private val userDao: UserDao,
    private val smartSessionDao: SmartSessionDao,
    private val vocabDbManager: VocabularyDatabaseManager,
    private val capacityCalculator: SessionCapacityCalculator = SessionCapacityCalculator(),
    val timingTracker: CardTimingTracker = CardTimingTracker(),
    private val statsManager: StudyStatisticsManager = StudyStatisticsManager(
        DailyStatisticsRepository(smartSessionDao),
        RollingStatisticsRepository(smartSessionDao)
    ),
    private val sessionBuilder: SessionBuilder = SessionBuilder(),
    private val sessionSelector: SessionSelector = SessionSelector(vocabDbManager)
) {
    // Static in-memory cache of vocabulary list grouped by CEFR level to eliminate repetitive disk reads
    private val vocabCache = java.util.concurrent.ConcurrentHashMap<String, List<com.example.core.database.DictWord>>()

    private suspend fun getWordsByLevelsCached(levels: List<String>): List<com.example.core.database.DictWord> {
        val results = mutableListOf<com.example.core.database.DictWord>()
        val levelsToFetch = mutableListOf<String>()
        for (level in levels) {
            val cached = vocabCache[level]
            if (cached != null) {
                results.addAll(cached)
            } else {
                levelsToFetch.add(level)
            }
        }
        if (levelsToFetch.isNotEmpty()) {
            val fetched = vocabDbManager.getWordsByLevels(levelsToFetch, limit = -1)
            for (level in levelsToFetch) {
                val levelWords = fetched.filter { it.level.equals(level, ignoreCase = true) }
                vocabCache[level] = levelWords
                results.addAll(levelWords)
            }
        }
        return results
    }

    /**
     * Estimates and builds a complete, optimized smart session.
     */
    suspend fun buildSmartSession(
        durationMinutes: Int,
        currentTime: Long = System.currentTimeMillis()
    ): List<CardEntity> = withContext(Dispatchers.IO) {
        val studySeconds = durationMinutes * 60

        // 1. Get rolling average seconds per card
        val rollingAverage = statsManager.getRollingAverageSeconds()

        // 2. Calculate dynamic capacity based on average speed
        val capacity = capacityCalculator.calculateCapacity(studySeconds, rollingAverage)
        val targetActiveCards = capacityCalculator.getTargetActiveCards(durationMinutes)

        // 3. Fetch user progress and starting level (Placement Test result)
        val progress = userDao.getUserProgressOnce()
        val userLevel = progress?.level ?: 1
        val currentCefr = listOf("A1", "A2", "B1", "B2", "C1", "C2").getOrNull(userLevel - 1) ?: "A1"

        // 4. Adaptive Backfill (Part 3): Struggle detection
        // Query recent logs across multiple sessions (last 30 logs)
        val recentLogs = smartSessionDao.getAllReviewLogs().take(30)
        val isStruggling = if (recentLogs.size >= 10 && userLevel > 1) {
            val struggleCount = recentLogs.count { it.rating == 1 || it.rating == 2 }
            (struggleCount.toDouble() / recentLogs.size) > 0.40
        } else {
            false
        }

        val prevCefr = if (isStruggling && userLevel > 1) {
            listOf("A1", "A2", "B1", "B2", "C1", "C2").getOrNull(userLevel - 2)
        } else {
            null
        }

        // Allowed levels for filtering existing local cards: focused on current,
        // and optionally previous level if struggling (Adaptive Backfill).
        val allowedLevels = if (prevCefr != null) listOf(currentCefr, prevCefr) else listOf(currentCefr)

        // 5. Get all existing local cards and filter them by CEFR
        val allLocalCards = userDao.getAllCardsOnce()
        val filteredLocalCards = sessionSelector.filterCardsByCefr(allLocalCards, allowedLevels)

        // 6. Build session list in priority order
        val initialSelected = sessionBuilder.buildSession(filteredLocalCards, capacity, currentTime, targetActiveCards)
        val selectedList = initialSelected.toMutableList()

        val remainingCapacity = capacity - selectedList.size

        // Calculate the current active cards across the deck
        val activeCardsCount = filteredLocalCards.count { card ->
            val isMatureAndDistant = card.state == 2 && card.boxIndex >= 5 && (card.dueDate - currentTime > 14L * 24 * 60 * 60 * 1000L)
            (card.state == 1 || card.state == 2 || card.state == 3) && !isMatureAndDistant
        }
        val deficit = (targetActiveCards - activeCardsCount).coerceAtLeast(0)
        val newCardsToCreateCount = minOf(deficit, remainingCapacity)

        if (newCardsToCreateCount > 0) {
            val allLocalWordIds = allLocalCards.map { it.wordId }.toSet()
            
            // Query new vocabulary from the cached lists
            val currentLevelWords = getWordsByLevelsCached(listOf(currentCefr))
                .filter { !allLocalWordIds.contains(it.id) }
                .shuffled()

            val newWordsFromDb = if (prevCefr != null) {
                // Introduce small number of bridge words (Part 3: Adaptive Backfill, e.g. 20%)
                val prevLevelWords = getWordsByLevelsCached(listOf(prevCefr))
                    .filter { !allLocalWordIds.contains(it.id) }
                    .shuffled()

                val bridgeCount = (newCardsToCreateCount * 0.20).toInt().coerceAtLeast(1)
                val primaryCount = (newCardsToCreateCount - bridgeCount).coerceAtLeast(0)

                prevLevelWords.take(bridgeCount) + currentLevelWords.take(primaryCount)
            } else {
                currentLevelWords.take(newCardsToCreateCount)
            }

            if (newWordsFromDb.isNotEmpty()) {
                val newCardsToInsert = newWordsFromDb.map { word ->
                    CardEntity(
                        wordId = word.id,
                        word = word.word,
                        boxIndex = 1,
                        stability = 1.0,
                        difficulty = 3.0,
                        elapsedDays = 0,
                        scheduledDays = 1,
                        reps = 0,
                        lapses = 0,
                        state = 0,
                        lastReviewed = 0,
                        dueDate = currentTime
                    )
                }

                // Batch Insert to resolve N+1 writes
                userDao.insertCards(newCardsToInsert)

                // Batch Fetch to resolve N+1 reads
                val wordIds = newWordsFromDb.map { it.id }
                val insertedCards = userDao.getCardsByWordIds(wordIds)
                selectedList.addAll(insertedCards)
            }
        }

        // 7. Persist session metrics inside Room
        val sessionStats = SmartSessionStatsEntity(
            id = 0,
            studySeconds = studySeconds,
            capacity = capacity,
            remainingCapacity = Math.max(0, capacity - selectedList.size),
            isFinished = false
        )
        smartSessionDao.insertSessionStats(sessionStats)

        return@withContext selectedList
    }

    /**
     * Finalizes tracking on a reviewed card and registers the review log.
     */
    suspend fun logCardReview(
        cardId: Int,
        rating: Int,
        dateStr: String = statsManager.getCurrentDateString()
    ) {
        val seconds = timingTracker.stop()
        if (seconds >= 1) {
            statsManager.logReview(cardId, seconds, rating, dateStr)
        }
    }

    /**
     * Access the current session stats.
     */
    suspend fun getSessionStats(): SmartSessionStatsEntity? {
        return smartSessionDao.getSessionStats()
    }

    /**
     * Explicitly access the statistics coordinator.
     */
    fun getStatsManager(): StudyStatisticsManager = statsManager
}
