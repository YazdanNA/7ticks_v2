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

        // 3. Fetch user progress and unlocked CEFR levels
        val progress = userDao.getUserProgressOnce()
        val userLevel = progress?.level ?: 1
        val allowedLevels = listOf("A1", "A2", "B1", "B2", "C1", "C2").take(userLevel.coerceIn(1, 6))

        // 4. Get all existing local cards and filter them by CEFR
        val allLocalCards = userDao.getAllCardsOnce()
        val filteredLocalCards = sessionSelector.filterCardsByCefr(allLocalCards, allowedLevels)

        // 5. Build session list in priority order
        val initialSelected = sessionBuilder.buildSession(filteredLocalCards, capacity, currentTime)
        val selectedList = initialSelected.toMutableList()

        val remainingCapacity = capacity - selectedList.size

        // 6. Reviews Always Win check: if Again + Due already meet/exceed capacity, do not add new cards.
        val totalReviewsCount = filteredLocalCards.count { it.state == 3 || (it.state == 2 && it.dueDate <= currentTime) }
        
        if (remainingCapacity > 0 && totalReviewsCount < capacity) {
            // Find brand new dictionary words of allowed CEFR levels
            val allLocalWordIds = allLocalCards.map { it.wordId }.toSet()
            val newWordsFromDb = vocabDbManager.getWordsByLevels(allowedLevels, limit = -1)
                .filter { !allLocalWordIds.contains(it.id) }
                .shuffled()
                .take(remainingCapacity)

            for (word in newWordsFromDb) {
                val newCard = CardEntity(
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
                userDao.insertCard(newCard)
                // Fetch generated card to obtain valid database auto-increment ID
                val inserted = userDao.getCardByWordId(word.id)
                if (inserted != null) {
                    selectedList.add(inserted)
                }
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
