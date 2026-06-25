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
import com.example.core.gamification.GamificationEngine
import com.example.core.gamification.XpConfig

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
    val rewardHistory: Flow<List<RewardHistoryEntity>> = userDao.getRewardHistory()

    suspend fun getReviewHistoryOnce(): List<ReviewHistoryEntity> = userDao.getReviewHistoryOnce()

    suspend fun getUserProgressOnce(): UserProgressEntity? = userDao.getUserProgressOnce()

    suspend fun getSessionStateOnce(): SessionStateEntity? = userDao.getSessionStateOnce()

    suspend fun getCardByWordId(wordId: Int): CardEntity? = userDao.getCardByWordId(wordId)

    suspend fun getCardById(id: Int): CardEntity? = userDao.getCardById(id)

    suspend fun insertCard(card: CardEntity) = userDao.insertCard(card)

    suspend fun updateCard(card: CardEntity) = userDao.updateCard(card)

    suspend fun updateUserProfile(username: String, avatar: String) = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: UserProgressEntity()
        val updated = progress.copy(userName = username, avatar = avatar)
        userDao.insertUserProgress(updated)
    }

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
                id = "first_review",
                name = "First Contact",
                description = "Complete your first spaced repetition word review.",
                colorHex = "#00C2FF",
                iconName = "Check"
            ),
            AchievementEntity(
                id = "first_session",
                name = "Initial Spark",
                description = "Finish your very first adaptive study session.",
                colorHex = "#00FFD2",
                iconName = "PlayArrow"
            ),
            AchievementEntity(
                id = "reviews_100",
                name = "Spaced Scholar",
                description = "Complete 100 cognitive card reviews.",
                colorHex = "#FFD600",
                iconName = "Star"
            ),
            AchievementEntity(
                id = "reviews_1000",
                name = "Memory Overlord",
                description = "Complete 1000 cognitive card reviews.",
                colorHex = "#FF3D00",
                iconName = "Star"
            ),
            AchievementEntity(
                id = "streak_7",
                name = "Weekly Warrior",
                description = "Maintain a 7-day active study streak.",
                colorHex = "#00E676",
                iconName = "Check"
            ),
            AchievementEntity(
                id = "streak_30",
                name = "Iron Consistency",
                description = "Maintain a 30-day active study streak.",
                colorHex = "#E040FB",
                iconName = "Check"
            ),
            AchievementEntity(
                id = "level_10",
                name = "Tenured Scholar",
                description = "Reach Player Level 10.",
                colorHex = "#9D00FF",
                iconName = "Star"
            ),
            AchievementEntity(
                id = "level_25",
                name = "Cognitive Legend",
                description = "Reach Player Level 25.",
                colorHex = "#3F51B5",
                iconName = "Star"
            ),
            AchievementEntity(
                id = "master_a1",
                name = "A1 Breakthrough",
                description = "Master 10 or more words in the CEFR A1 level.",
                colorHex = "#00FFD2",
                iconName = "LockOpen"
            ),
            AchievementEntity(
                id = "master_a2",
                name = "A2 Novice",
                description = "Master 10 or more words in the CEFR A2 level.",
                colorHex = "#00C2FF",
                iconName = "LockOpen"
            ),
            AchievementEntity(
                id = "master_b1",
                name = "B1 Intermediate",
                description = "Master 10 or more words in the CEFR B1 level.",
                colorHex = "#FF9800",
                iconName = "LockOpen"
            ),
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
                title = "Daily Reviewer",
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
            ),
            ChallengeEntity(
                id = "daily_review_20",
                title = "FSRS Calibration",
                description = "Review 20 words today to calibrate spaced retention.",
                target = 20,
                current = 0,
                completed = false
            ),
            ChallengeEntity(
                id = "daily_sessions_2",
                title = "Double Session",
                description = "Complete 2 separate adaptive study sessions.",
                target = 2,
                current = 0,
                completed = false
            ),
            ChallengeEntity(
                id = "daily_xp_150",
                title = "XP Power Hour",
                description = "Earn 150 total XP today.",
                target = 150,
                current = 0,
                completed = false
            ),
            ChallengeEntity(
                id = "weekly_learn_50",
                title = "Vocabulary Expander",
                description = "Introduce and learn 50 brand new words.",
                target = 50,
                current = 0,
                completed = false
            ),
            ChallengeEntity(
                id = "weekly_streak_5",
                title = "Persistent Mind",
                description = "Achieve or maintain a 5-day active learning streak.",
                target = 5,
                current = 1,
                completed = false
            ),
            ChallengeEntity(
                id = "weekly_xp_1000",
                title = "XP Marathon",
                description = "Acquire 1000 total XP within this week.",
                target = 1000,
                current = 0,
                completed = false
            )
        )
        userDao.insertChallenges(defaultChallenges)
    }

    suspend fun prepareSmartLearnEngine() = withContext(Dispatchers.IO) {
        // Under refactored architecture, automatic pre-population is removed.
        // Smart Learn is the sole authority for creating new cards on-demand.
        Log.d("UserRepository", "prepareSmartLearnEngine: Automatic card pre-population is disabled.")
    }

    // Award XP and handle leveling up
    suspend fun awardXp(amount: Int, category: String = "learning"): Boolean = withContext(Dispatchers.IO) {
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
            xpEarned = currentStats.xpEarned + amount
        )
        userDao.insertStatistics(updatedStats)

        // Update daily challenge progress and check achievements if from review
        if (category == "review") {
            val statsCopy = updatedStats.copy(wordsReviewed = currentStats.wordsReviewed + 1)
            userDao.insertStatistics(statsCopy)
            updateChallengeProgress("daily_review_20", 1)
            updateChallengeProgress("daily_words", 1) // backward compatibility
            updateChallengeProgress("daily_xp_150", amount)
            updateChallengeProgress("weekly_xp_1000", amount)
        } else {
            updateChallengeProgress("daily_xp_150", amount)
            updateChallengeProgress("weekly_xp_1000", amount)
        }

        if (leveledUp) {
            // Log level-up reward
            val levelUpReward = RewardHistoryEntity(
                type = "LEVEL_UP",
                title = "Level Up! Reached Level $currentLevel",
                rewardXp = currentLevel * 100
            )
            userDao.insertRewardLog(levelUpReward)
            // Grant level up bonus XP!
            awardXp(currentLevel * 100, "level_up")
        }

        // Trigger dynamic achievements evaluation
        evaluateAndTriggerAchievements()

        return@withContext leveledUp
    }

    suspend fun updateChallengeProgress(challengeId: String, increment: Int) = withContext(Dispatchers.IO) {
        val challenge = userDao.getChallengeById(challengeId) ?: return@withContext
        if (challenge.completed) return@withContext

        val newCurrent = challenge.current + increment
        val completed = newCurrent >= challenge.target

        userDao.updateChallengeProgress(challengeId, newCurrent.coerceAtMost(challenge.target), completed)

        if (completed) {
            // Log challenge reward
            val rewardXp = if (challengeId.startsWith("weekly")) 250 else XpConfig.CHALLENGE_COMPLETE
            val reward = RewardHistoryEntity(
                type = "CHALLENGE_COMPLETE",
                title = "Challenge Completed: ${challenge.title}",
                rewardXp = rewardXp
            )
            userDao.insertRewardLog(reward)
            awardXp(rewardXp, "challenge")
        }
    }

    private val gamificationEngine = GamificationEngine()

    suspend fun evaluateAndTriggerAchievements() = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext
        val allCards = userDao.getAllCardsOnce()
        val allLogs = userDao.getReviewHistoryOnce()
        val currentAchievements = userDao.getAchievementsOnce()

        val newlyUnlocked = gamificationEngine.evaluateAchievements(
            currentAchievements = currentAchievements,
            allCards = allCards,
            reviewHistory = allLogs,
            progress = progress,
            currentTimeMs = System.currentTimeMillis()
        )

        for (ach in newlyUnlocked) {
            userDao.unlockAchievement(ach.id, ach.unlockedAt)
            // Log achievement reward
            val reward = RewardHistoryEntity(
                type = "ACHIEVEMENT_UNLOCK",
                title = "Achievement Unlocked: ${ach.name}",
                rewardXp = XpConfig.ACHIEVEMENT_UNLOCK
            )
            userDao.insertRewardLog(reward)
            awardXp(XpConfig.ACHIEVEMENT_UNLOCK, "achievement")
        }
    }

    suspend fun updateStreakOnActivity() = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext
        val updatedProgress = gamificationEngine.processStreak(progress, System.currentTimeMillis())
        
        if (updatedProgress.streak != progress.streak || updatedProgress.lastActiveDay != progress.lastActiveDay) {
            userDao.insertUserProgress(updatedProgress)
            
            // Trigger streak challenge updates
            updateChallengeProgress("weekly_streak", 1)
            updateChallengeProgress("weekly_streak_5", 1)
            
            // Log a reward if streak milestone is reached!
            if (gamificationEngine.isStreakMilestone(updatedProgress.streak)) {
                val milestoneName = gamificationEngine.getStreakMilestoneName(updatedProgress.streak)
                val milestoneReward = RewardHistoryEntity(
                    type = "STREAK_MILESTONE",
                    title = "Streak Milestone! $milestoneName (${updatedProgress.streak} Days)",
                    rewardXp = updatedProgress.streak * XpConfig.STREAK_MILESTONE_MULTIPLIER
                )
                userDao.insertRewardLog(milestoneReward)
                awardXp(updatedProgress.streak * XpConfig.STREAK_MILESTONE_MULTIPLIER, "streak")
            }
        }
    }

    suspend fun restoreStreak(): Boolean = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext false
        if (progress.streakRestoreSpells > 0) {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val todayStr = sdf.format(Date())
            val restoredProgress = progress.copy(
                streak = progress.longestStreak.coerceAtLeast(3),
                lastActiveDay = todayStr,
                streakRestoreSpells = progress.streakRestoreSpells - 1
            )
            userDao.insertUserProgress(restoredProgress)
            return@withContext true
        }
        return@withContext false
    }

    suspend fun clearRewards() = withContext(Dispatchers.IO) {
        userDao.clearRewardHistory()
    }

    suspend fun dismissReward(id: Int) = withContext(Dispatchers.IO) {
        userDao.deleteRewardLog(id)
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

    private val adaptiveEngine = com.example.core.fsrs.AdaptiveLearningEngine()

    suspend fun getCefrLevelMasteryList(): List<com.example.core.fsrs.CefrLevelMastery> = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext emptyList()
        val allCards = userDao.getAllCardsOnce()
        val allLogs = userDao.getReviewHistoryOnce()
        val levels = listOf("A1", "A2", "B1", "B2", "C1", "C2")

        levels.map { levelStr ->
            val dbCount = vocabDbManager.getWordCountByLevels(listOf(levelStr))
            val totalInLevel = if (dbCount > 0) dbCount else when (levelStr) {
                "A1" -> 150
                "A2" -> 200
                "B1" -> 250
                "B2" -> 300
                "C1" -> 350
                "C2" -> 400
                else -> 200
            }

            // Filter cards belonging to this CEFR level
            val levelCards = allCards.filter { card ->
                val cachedLevel = vocabDbManager.getWordById(card.wordId)?.level ?: "A1"
                cachedLevel.equals(levelStr, ignoreCase = true)
            }

            val levelWordIds = levelCards.map { it.wordId }.toSet()
            val levelLogs = allLogs.filter { levelWordIds.contains(it.wordId) }

            // SevenTicks definitions: 3 Ticks = Learned, 7 Ticks = Mastered
            val wordsLearned = levelCards.count { it.boxIndex >= 3 }
            val wordsRetained = levelCards.count { it.boxIndex >= 7 }

            val reviewAccuracy = if (levelLogs.isEmpty()) {
                0.0f
            } else {
                val successful = levelLogs.count { it.rating in 2..4 }
                successful.toFloat() / levelLogs.size.toFloat()
            }

            // Mastery percentage is defined as the ratio of words that have reached at least 3 Ticks (Learned)
            val masteryPercentage = (wordsLearned.toFloat() / totalInLevel.toFloat()).coerceIn(0.0f, 1.0f)

            val levelInt = when (levelStr) {
                "A1" -> 1
                "A2" -> 2
                "B1" -> 3
                "B2" -> 4
                "C1" -> 5
                "C2" -> 6
                else -> 1
            }

            com.example.core.fsrs.CefrLevelMastery(
                level = levelStr,
                totalWordsInLevel = totalInLevel,
                wordsLearned = wordsLearned,
                wordsRetained = wordsRetained,
                reviewAccuracy = reviewAccuracy,
                masteryPercentage = masteryPercentage,
                isUnlocked = progress.level >= levelInt
            )
        }
    }

    suspend fun getLearningQualityScore(): com.example.core.fsrs.LearningQualityScore = withContext(Dispatchers.IO) {
        val allLogs = userDao.getReviewHistoryOnce()
        val allCards = userDao.getAllCardsOnce()
        adaptiveEngine.calculateLearningQuality(allLogs, allCards)
    }

    suspend fun checkAndProgressUserLevel(): Boolean = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext false
        val currentLevelInt = progress.level
        if (currentLevelInt >= 6) return@withContext false // Max level C2 reached

        val currentLevelStr = getLevelString(currentLevelInt)
        
        // Find total words in this level from vocabulary database
        val dbCount = vocabDbManager.getWordCountByLevels(listOf(currentLevelStr))
        val totalInLevel = if (dbCount > 0) dbCount else when (currentLevelStr) {
            "A1" -> 150
            "A2" -> 200
            "B1" -> 250
            "B2" -> 300
            "C1" -> 350
            "C2" -> 400
            else -> 200
        }

        // Filter local cards belonging to this CEFR level
        val allCards = userDao.getAllCardsOnce()
        val levelCards = allCards.filter { card ->
            val cachedLevel = vocabDbManager.getWordById(card.wordId)?.level ?: "A1"
            cachedLevel.equals(currentLevelStr, ignoreCase = true)
        }

        // Count how many cards in this level have boxIndex >= 3 (Learned state / >= 3 Ticks)
        val learnedCount = levelCards.count { it.boxIndex >= 3 }

        // Promotion Rule: 90% of words in current level must have reached >= 3 Ticks (Learned)
        val requiredLearned = (totalInLevel * 0.9).toInt()

        if (learnedCount >= requiredLearned) {
            val nextLevelInt = currentLevelInt + 1
            userDao.updateLevel(nextLevelInt)
            return@withContext true
        }
        return@withContext false
    }

    suspend fun generateSmartLearnSession(): List<CardEntity> = withContext(Dispatchers.IO) {
        val progress = userDao.getUserProgressOnce() ?: return@withContext emptyList()
        val currentTime = System.currentTimeMillis()

        // Get daily goals based on onboarding study-time mapping
        val goalLower = progress.dailyGoal.lowercase()
        val (dailyNewLimit, dailyReviewLimit) = when {
            goalLower.contains("5 min") || goalLower.contains("5-min") -> Pair(2, 8)
            goalLower.contains("10 min") -> Pair(3, 10)
            goalLower.contains("15 min") -> Pair(4, 15)
            goalLower.contains("20 min") -> Pair(5, 20)
            goalLower.contains("30 min") -> Pair(8, 30)
            goalLower.contains("45 min") -> Pair(12, 45)
            goalLower.contains("60 min") -> Pair(15, 60)
            else -> Pair(8, 30)
        }

        // Fetch stats for today
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayStats = userDao.getStatisticsForDate(dateStr)
        val reviewedToday = todayStats?.wordsReviewed ?: 0
        val learnedToday = todayStats?.wordsLearned ?: 0

        val remainingReviewsCapacity = (dailyReviewLimit - reviewedToday).coerceAtLeast(0)
        val remainingNewWordsCapacity = (dailyNewLimit - learnedToday).coerceAtLeast(0)

        // Fetch reviews logs from today to compute adaptive parameters if needed
        val allLogs = userDao.getReviewHistoryOnce()
        val todayStart = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
        val todayLogs = allLogs.filter { it.timestamp >= todayStart }
        val fatigueState = adaptiveEngine.calculateFatigue(todayLogs)
        val sessionConfig = adaptiveEngine.determineSessionConfig(todayLogs, todayStats, fatigueState, progress.dailyGoal)

        val allLocalCards = userDao.getAllCardsOnce()

        // 1. Gather Overdue Reviews (Priority #1)
        val overdueReviews = allLocalCards.filter { card ->
            card.reps > 0 && card.dueDate <= currentTime && card.state == 2
        }.sortedBy { it.dueDate } // Oldest due first

        // 2. Gather Learning Cards (Priority #2)
        val learningCards = allLocalCards.filter { card ->
            card.reps > 0 && card.state == 1
        }

        // 3. Gather Relearning Cards (Priority #3)
        val relearningCards = allLocalCards.filter { card ->
            card.reps > 0 && card.state == 3
        }

        val totalReviewWorkload = overdueReviews.size + learningCards.size + relearningCards.size

        val totalSelected = mutableListOf<CardEntity>()

        if (totalReviewWorkload > 0) {
            // We have review workload! Build session with reviews ONLY (up to remaining review capacity).
            // "Never prioritize new words over reviews. Never create new words if review workload exists."
            val reviewsToTake = overdueReviews.take(remainingReviewsCapacity)
            val learningToTake = learningCards.take(remainingReviewsCapacity - reviewsToTake.size)
            val relearningToTake = relearningCards.take(remainingReviewsCapacity - reviewsToTake.size - learningToTake.size)

            totalSelected.addAll(reviewsToTake)
            totalSelected.addAll(learningToTake)
            totalSelected.addAll(relearningToTake)
        } else {
            // No due reviews, learning, or relearning cards! We can introduce new words up to the remaining capacity.
            if (remainingNewWordsCapacity > 0) {
                // First, look for any pre-existing new cards in review_cards (reps == 0)
                val preExistingNew = allLocalCards.filter { it.reps == 0 }
                val newCardsToTake = preExistingNew.take(remainingNewWordsCapacity)
                totalSelected.addAll(newCardsToTake)

                val neededNewFromDb = remainingNewWordsCapacity - totalSelected.size
                if (neededNewFromDb > 0) {
                    val allowedLevels = getAllowedLevels(progress.level)
                    val allLocalWordIds = allLocalCards.map { it.wordId }.toSet()

                    // Fetch brand new words of allowed levels without any artificial limit/ceiling
                    val newWordsFromDb = vocabDbManager.getWordsByLevels(allowedLevels, limit = -1)
                        .filter { !allLocalWordIds.contains(it.id) }
                        .shuffled()
                        .take(neededNewFromDb)

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
                            dueDate = System.currentTimeMillis()
                        )
                        userDao.insertCard(newCard)
                        val inserted = userDao.getCardByWordId(word.id)
                        if (inserted != null) {
                            totalSelected.add(inserted)
                        }
                    }
                }
            }
        }

        // Save session state to database
        val cardIds = totalSelected.map { it.id }
        updateSessionState(active = cardIds.isNotEmpty(), cardIds = cardIds, currentIndex = 0)

        return@withContext totalSelected
    }

    /**
     * Centralized review engine function.
     * The ONLY place allowed to update card/word review state.
     */
    suspend fun reviewCard(
        cardId: Int,
        isBoxWord: Boolean,
        rating: com.example.core.fsrs.ReviewRatingModel
    ): Boolean = withContext(Dispatchers.IO) {
        val currentTime = System.currentTimeMillis()
        val fsrsRepo = com.example.core.fsrs.FsrsRepository()

        val xpAmount = when (rating) {
            com.example.core.fsrs.ReviewRatingModel.AGAIN -> 5
            com.example.core.fsrs.ReviewRatingModel.HARD -> 10
            com.example.core.fsrs.ReviewRatingModel.GOOD -> 15
            com.example.core.fsrs.ReviewRatingModel.EASY -> 20
        }

        if (isBoxWord) {
            val boxWord = userDao.getBoxWordById(cardId) ?: return@withContext false
            
            // Map to FsrsCardModel
            val fsrsModel = com.example.core.fsrs.FsrsCardModel(
                id = boxWord.id,
                wordId = boxWord.wordId,
                word = boxWord.word,
                stability = boxWord.stability,
                difficulty = boxWord.difficulty,
                elapsedDays = boxWord.elapsedDays,
                scheduledDays = boxWord.scheduledDays,
                reps = boxWord.reps,
                lapses = boxWord.lapses,
                state = boxWord.state,
                lastReviewed = if (boxWord.lastReviewed > 0) java.util.Date(boxWord.lastReviewed) else null,
                dueDate = java.util.Date(boxWord.dueDate)
            )

            // FSRS calculate
            val updatedFsrsModel = fsrsRepo.calculateNextReview(fsrsModel, rating, currentTime)

            // SevenTicks progression logic
            val currentBoxIndex = boxWord.boxIndex
            val nextBoxIndex = when (rating) {
                com.example.core.fsrs.ReviewRatingModel.AGAIN -> 1
                com.example.core.fsrs.ReviewRatingModel.HARD -> (currentBoxIndex - 1).coerceAtLeast(1)
                com.example.core.fsrs.ReviewRatingModel.GOOD -> (currentBoxIndex + 1).coerceAtMost(7)
                com.example.core.fsrs.ReviewRatingModel.EASY -> (currentBoxIndex + 2).coerceAtMost(7)
            }

            val updatedBoxWord = boxWord.copy(
                boxIndex = nextBoxIndex,
                stability = updatedFsrsModel.stability,
                difficulty = updatedFsrsModel.difficulty,
                elapsedDays = updatedFsrsModel.elapsedDays,
                scheduledDays = updatedFsrsModel.scheduledDays,
                reps = updatedFsrsModel.reps,
                lapses = updatedFsrsModel.lapses,
                state = updatedFsrsModel.state,
                lastReviewed = updatedFsrsModel.lastReviewed?.time ?: currentTime,
                dueDate = updatedFsrsModel.dueDate.time
            )

            userDao.updateBoxWord(updatedBoxWord)

            // Record review log
            recordReviewLog(
                wordId = boxWord.wordId,
                word = boxWord.word,
                rating = rating.value,
                stability = updatedFsrsModel.stability,
                difficulty = updatedFsrsModel.difficulty
            )

            val isNewWordLearned = boxWord.reps == 0

            // Award XP
            val leveledUp = awardXp(xpAmount, "review")

            if (isNewWordLearned) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentStats = userDao.getStatisticsForDate(dateStr) ?: StatisticsEntity(dateStr)
                val statsCopy = currentStats.copy(wordsLearned = currentStats.wordsLearned + 1)
                userDao.insertStatistics(statsCopy)
            }

            return@withContext leveledUp
        } else {
            val card = userDao.getCardById(cardId) ?: return@withContext false

            // Map to FsrsCardModel
            val fsrsModel = com.example.core.fsrs.FsrsCardModel(
                id = card.id,
                wordId = card.wordId,
                word = card.word,
                stability = card.stability,
                difficulty = card.difficulty,
                elapsedDays = card.elapsedDays,
                scheduledDays = card.scheduledDays,
                reps = card.reps,
                lapses = card.lapses,
                state = card.state,
                lastReviewed = if (card.lastReviewed > 0) java.util.Date(card.lastReviewed) else null,
                dueDate = java.util.Date(card.dueDate)
            )

            // FSRS calculate
            val updatedFsrsModel = fsrsRepo.calculateNextReview(fsrsModel, rating, currentTime)

            // SevenTicks progression logic
            val currentBoxIndex = card.boxIndex
            val nextBoxIndex = when (rating) {
                com.example.core.fsrs.ReviewRatingModel.AGAIN -> 1
                com.example.core.fsrs.ReviewRatingModel.HARD -> (currentBoxIndex - 1).coerceAtLeast(1)
                com.example.core.fsrs.ReviewRatingModel.GOOD -> (currentBoxIndex + 1).coerceAtMost(7)
                com.example.core.fsrs.ReviewRatingModel.EASY -> (currentBoxIndex + 2).coerceAtMost(7)
            }

            val updatedCard = CardEntity(
                id = card.id,
                wordId = card.wordId,
                word = card.word,
                boxIndex = nextBoxIndex,
                stability = updatedFsrsModel.stability,
                difficulty = updatedFsrsModel.difficulty,
                elapsedDays = updatedFsrsModel.elapsedDays,
                scheduledDays = updatedFsrsModel.scheduledDays,
                reps = updatedFsrsModel.reps,
                lapses = updatedFsrsModel.lapses,
                state = updatedFsrsModel.state,
                lastReviewed = updatedFsrsModel.lastReviewed?.time ?: currentTime,
                dueDate = updatedFsrsModel.dueDate.time
            )

            userDao.updateCard(updatedCard)

            // Record review log
            recordReviewLog(
                wordId = card.wordId,
                word = card.word,
                rating = rating.value,
                stability = updatedFsrsModel.stability,
                difficulty = updatedFsrsModel.difficulty
            )

            val isNewWordLearned = card.reps == 0

            // Award XP
            val leveledUp = awardXp(xpAmount, "review")

            if (isNewWordLearned) {
                val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val currentStats = userDao.getStatisticsForDate(dateStr) ?: StatisticsEntity(dateStr)
                val statsCopy = currentStats.copy(wordsLearned = currentStats.wordsLearned + 1)
                userDao.insertStatistics(statsCopy)
            }

            // Check level advancement
            checkAndProgressUserLevel()

            return@withContext leveledUp
        }
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
