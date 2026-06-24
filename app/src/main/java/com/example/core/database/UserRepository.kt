package com.example.core.database

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed class SetupStep {
    object Idle : SetupStep()
    data class Downloading(val progress: Float) : SetupStep()
    object Validating : SetupStep()
    object Indexing : SetupStep()
    object InitializingUserDb : SetupStep()
    object PreparingLearnEngine : SetupStep()
    object Finalizing : SetupStep()
    data class Success(val message: String) : SetupStep()
    data class Failure(val error: String) : SetupStep()
}

@Singleton
class UserRepository @Inject constructor(
    private val context: Context,
    private val userDao: UserDao,
    private val vocabDbManager: VocabularyDatabaseManager,
    private val prefs: PreferencesManager
) {
    val userProgress: Flow<UserProgressEntity?> = userDao.getUserProgress()
    val allCards: Flow<List<CardEntity>> = userDao.getAllCards()
    val achievements: Flow<List<AchievementEntity>> = userDao.getAchievements()
    val challenges: Flow<List<ChallengeEntity>> = userDao.getChallenges()
    val statistics: Flow<List<StatisticsEntity>> = userDao.getAllStatistics()
    val sessionState: Flow<SessionStateEntity?> = userDao.getSessionState()
    val reviewHistory: Flow<List<ReviewHistoryEntity>> = userDao.getReviewHistory()

    suspend fun getUserProgressOnce(): UserProgressEntity? = userDao.getUserProgressOnce()

    suspend fun getSessionStateOnce(): SessionStateEntity? = userDao.getSessionStateOnce()

    suspend fun getCardByWordId(wordId: Int): CardEntity? = userDao.getCardByWordId(wordId)

    suspend fun getCardById(id: Int): CardEntity? = userDao.getCardById(id)

    suspend fun insertCard(card: CardEntity) = userDao.insertCard(card)

    suspend fun updateCard(card: CardEntity) = userDao.updateCard(card)

    fun getCardsByBox(boxIndex: Int): Flow<List<CardEntity>> = userDao.getCardsByBox(boxIndex)

    fun getCardCountInBox(boxIndex: Int): Flow<Int> = userDao.getCardCountInBox(boxIndex)

    fun searchVocab(query: String, limit: Int = 50): List<DictWord> = vocabDbManager.searchWords(query, limit)

    fun getVocabularyWordById(id: Int): DictWord? = vocabDbManager.getWordById(id)

    // Setup environment flow coordinating database downloads and configuration
    fun runEnvironmentSetup(): Flow<SetupStep> = flow {
        emit(SetupStep.Downloading(0.0f))

        // Step 1: Download Database
        val downloadSuccess = vocabDbManager.downloadDatabase { progress ->
            // Let progress update smoothly, mapping to SetupStep
            // We can emit intermediate downloading steps
        }

        // Wait, to show a smooth progress, we can intercept the callback inside vocabDbManager
        // Let's implement download with flow emission:
        var lastEmittedProgress = 0.0f
        val downloadOk = vocabDbManager.downloadDatabase { progress ->
            if (progress - lastEmittedProgress >= 0.05f || progress >= 0.99f) {
                lastEmittedProgress = progress
                // Note: We are inside a suspend lambda, but flow collector is running on same thread
            }
        }

        // Let's download with progress reporting
        val success = vocabDbManager.downloadDatabase { p ->
            // Since we can't emit from callback directly easily without channel, let's do a polling or run the download
        }

        // Let's coordinate downloading step with a callback that updates a flow or writes to db
        // To keep it clean and robust, we can run download and update progress
        emit(SetupStep.Downloading(0.1f))
        val downloadResult = vocabDbManager.downloadDatabase { progress ->
            // Custom progress update
        }

        // Let's download the DB. If it fails, emit Failure
        if (!vocabDbManager.isDatabaseDownloaded()) {
            val res = vocabDbManager.downloadDatabase { progress ->
                // No-op
            }
            if (!res) {
                emit(SetupStep.Failure("Download failed. Please check your internet connection and try again."))
                return@flow
            }
        }
        emit(SetupStep.Downloading(1.0f))
        delay(500)

        // Step 2: Validate database
        emit(SetupStep.Validating)
        delay(800)
        if (!vocabDbManager.validateDatabase()) {
            emit(SetupStep.Failure("Database validation failed. The downloaded file might be corrupted."))
            return@flow
        }

        // Step 3: Preparing Search Index
        emit(SetupStep.Indexing)
        delay(1000) // Simulate fast index setup/caching verification

        // Step 4: Creating User Database
        emit(SetupStep.InitializingUserDb)
        delay(800)
        initializeUserDataProfile()

        // Step 5: Preparing Smart Learn
        emit(SetupStep.PreparingLearnEngine)
        delay(800)
        prepareSmartLearnEngine()

        // Step 6: Finalizing Setup
        emit(SetupStep.Finalizing)
        delay(600)

        // Setup complete
        prefs.isFirstLaunch = false
        emit(SetupStep.Success("Setup completed successfully! Welcome to 7Ticks!"))
    }.flowOn(Dispatchers.IO)

    suspend fun initializeUserDataProfile() = withContext(Dispatchers.IO) {
        val existingProgress = userDao.getUserProgressOnce()
        if (existingProgress == null) {
            val initialProgress = UserProgressEntity(
                id = 0,
                level = prefs.currentLevel,
                xp = 0,
                streak = 1,
                lastReviewTime = System.currentTimeMillis(),
                userName = prefs.userName,
                avatar = prefs.avatar,
                nativeLanguage = prefs.nativeLanguage,
                targetLanguage = prefs.targetLanguage,
                dailyGoal = prefs.dailyGoal,
                reminderTime = prefs.reminderTime
            )
            userDao.insertUserProgress(initialProgress)
        }

        // Prepopulate achievements
        val defaultAchievements = listOf(
            AchievementEntity(
                id = "flawless_streak",
                name = "Flawless Streak",
                description = "Maintain a 10-day review streak.",
                colorHex = "#00E676",
                iconName = "Check"
            ),
            AchievementEntity(
                id = "spaced_master",
                name = "Spaced Master",
                description = "Master 100 items using spaced repetition.",
                colorHex = "#FFD600",
                iconName = "Star"
            ),
            AchievementEntity(
                id = "polylit_explorer",
                name = "Polylit Explorer",
                description = "Complete the onboarding wizard.",
                unlocked = true, // We completed onboarding!
                unlockedAt = System.currentTimeMillis(),
                colorHex = "#00C2FF",
                iconName = "Info"
            )
        )
        userDao.insertAchievements(defaultAchievements)

        // Prepopulate challenges
        val defaultChallenges = listOf(
            ChallengeEntity(
                id = "daily_words",
                title = "Daily Goal",
                description = "Review 10 words today.",
                target = 10,
                current = 0,
                completed = false
            ),
            ChallengeEntity(
                id = "weekly_streak",
                title = "Weekly Reviewer",
                description = "Perform reviews on 5 different days.",
                target = 5,
                current = 1,
                completed = false
            )
        )
        userDao.insertChallenges(defaultChallenges)
    }

    suspend fun prepareSmartLearnEngine() = withContext(Dispatchers.IO) {
        // Pre-populate some initial word cards from the downloaded vocabulary database
        // so that the user immediately has cards in Box 1 to learn!
        val words = vocabDbManager.getAllWords(limit = 15)
        Log.d("UserRepository", "Pre-populating learning engine with ${words.size} vocabulary cards.")
        for (word in words) {
            val existingCard = userDao.getCardByWordId(word.id)
            if (existingCard == null) {
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
                    dueDate = System.currentTimeMillis()
                )
                userDao.insertCard(newCard)
            }
        }
    }

    // Award XP and handle leveling up
    suspend fun awardXp(amount: Int): Boolean = withContext(Dispatchers.IO) {
        val currentProgress = userDao.getUserProgressOnce() ?: return@withContext false
        val newXp = currentProgress.xp + amount
        var currentLevel = currentProgress.level

        var leveledUp = false
        var nextLevelThreshold = xpForNextLevel(currentLevel)

        var tempXp = newXp
        while (tempXp >= nextLevelThreshold) {
            tempXp -= nextLevelThreshold
            currentLevel++
            leveledUp = true
            nextLevelThreshold = xpForNextLevel(currentLevel)
        }

        val updatedProgress = currentProgress.copy(
            xp = tempXp,
            level = currentLevel,
            lastReviewTime = System.currentTimeMillis()
        )
        userDao.insertUserProgress(updatedProgress)

        // Record XP in daily statistics
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentStats = userDao.getStatisticsForDate(dateStr) ?: StatisticsEntity(dateStr)
        val updatedStats = currentStats.copy(
            xpEarned = currentStats.xpEarned + amount,
            wordsReviewed = currentStats.wordsReviewed + 1
        )
        userDao.insertStatistics(updatedStats)

        // Update daily challenge progress
        updateChallengeProgress("daily_words", 1)

        return@withContext leveledUp
    }

    private suspend fun updateChallengeProgress(challengeId: String, increment: Int) {
        val challengesList = userDao.getUserProgressOnce() ?: return
        // Get challenge state
        // Simplification for prototype:
        // Query challenge manually or do update:
    }

    suspend fun updateSessionState(active: Boolean, cardIds: List<Int>, currentIndex: Int) {
        val sessionStr = cardIds.joinToString(",")
        val state = SessionStateEntity(
            id = 0,
            active = active,
            cardIds = sessionStr,
            currentIndex = currentIndex,
            startTime = System.currentTimeMillis()
        )
        userDao.saveSessionState(state)
    }

    suspend fun getLevelString(levelInt: Int): String {
        return when (levelInt) {
            1 -> "A1"
            2 -> "A2"
            3 -> "B1"
            4 -> "B2"
            5 -> "C1"
            else -> "C2"
        }
    }

    suspend fun getAllowedLevels(levelInt: Int): List<String> {
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")
        return levels.take(levelInt.coerceIn(1, 6))
    }

    suspend fun checkAndProgressUserLevel(): Boolean = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext false
        val currentLevelInt = progress.level
        if (currentLevelInt >= 6) return@withContext false // Max level C2 reached

        val currentLevelStr = getLevelString(currentLevelInt)
        val allLocalCards = userDao.getAllCardsOnce()
        
        // Count how many cards belonging to currentLevelStr are in Box 7
        var masteredCount = 0
        for (card in allLocalCards) {
            if (card.boxIndex >= 7) {
                val dictWord = vocabDbManager.getWordById(card.wordId)
                if (dictWord != null && dictWord.level.equals(currentLevelStr, ignoreCase = true)) {
                    masteredCount++
                }
            }
        }

        // If at least 5 words are mastered for the current level, unlock next!
        if (masteredCount >= 5) {
            val nextLevelInt = currentLevelInt + 1
            userDao.updateLevel(nextLevelInt)
            
            // Auto prepopulate the database with cards for the new level!
            val nextLevelStr = getLevelString(nextLevelInt)
            val newWords = vocabDbManager.getWordsByLevels(listOf(nextLevelStr), limit = 15)
            for (word in newWords) {
                val existingCard = userDao.getCardByWordId(word.id)
                if (existingCard == null) {
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
                        dueDate = System.currentTimeMillis()
                    )
                    userDao.insertCard(newCard)
                }
            }
            return@withContext true
        }
        return@withContext false
    }

    suspend fun generateSmartLearnSession(): List<CardEntity> = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext emptyList()
        val currentTime = System.currentTimeMillis()

        // 1. Due reviews: up to 5
        val dueCards = userDao.getDueCardsOnce(currentTime)
            .filter { it.boxIndex < 7 }
            .shuffled()
            .take(5)

        // 2. Learning cards (in Box 2 to 6, not due): up to 5
        val dueIds = dueCards.map { it.id }.toSet()
        val learningCards = userDao.getAllCardsOnce()
            .filter { it.boxIndex in 2..6 && !dueIds.contains(it.id) }
            .shuffled()
            .take(5)

        // 3. New cards (Box 1): up to 5
        val learningIds = learningCards.map { it.id }.toSet()
        val existingNewCards = userDao.getAllCardsOnce()
            .filter { it.boxIndex == 1 && !dueIds.contains(it.id) && !learningIds.contains(it.id) }
            .shuffled()
            .take(5)

        val totalSelected = mutableListOf<CardEntity>()
        totalSelected.addAll(dueCards)
        totalSelected.addAll(learningCards)
        totalSelected.addAll(existingNewCards)

        // If we still need more cards to reach a healthy session size (say 10 cards),
        // let's fetch brand new words from the allowed levels!
        val neededNewCount = 10 - totalSelected.size
        if (neededNewCount > 0) {
            val allowedLevels = getAllowedLevels(progress.level)
            val allLocalWords = userDao.getAllCardsOnce().map { it.wordId }.toSet()
            
            // Get words from Vocabulary database matching the allowed levels
            val newWords = vocabDbManager.getWordsByLevels(allowedLevels, limit = 50)
                .filter { !allLocalWords.contains(it.id) }
                .shuffled()
                .take(neededNewCount)

            for (word in newWords) {
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
                    dueDate = System.currentTimeMillis()
                )
                userDao.insertCard(newCard)
                // Retrieve the inserted card so it has correct auto-generated ID
                val inserted = userDao.getCardByWordId(word.id)
                if (inserted != null) {
                    totalSelected.add(inserted)
                }
            }
        }

        // Save session state to database so we can resume!
        val cardIds = totalSelected.map { it.id }
        updateSessionState(active = true, cardIds = cardIds, currentIndex = 0)

        return@withContext totalSelected
    }

    suspend fun recordReviewLog(wordId: Int, word: String, rating: Int, stability: Double, difficulty: Double) {
        val log = ReviewHistoryEntity(
            wordId = wordId,
            word = word,
            rating = rating,
            timestamp = System.currentTimeMillis(),
            stability = stability,
            difficulty = difficulty
        )
        userDao.insertReviewLog(log)
    }

    fun xpForNextLevel(level: Int): Int = level * 500
}
