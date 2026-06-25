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
            val totalInLevel = when (levelStr) {
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

            val wordsLearned = levelCards.count { it.reps > 0 }
            val wordsRetained = levelCards.count { it.reps > 0 && it.stability >= 2.0 }

            val reviewAccuracy = if (levelLogs.isEmpty()) {
                0.0f
            } else {
                val successful = levelLogs.count { it.rating in 2..4 }
                successful.toFloat() / levelLogs.size.toFloat()
            }

            // Mastery score formulation combining exposure and retention
            val learnedRatio = (wordsLearned.toFloat() / totalInLevel.toFloat()).coerceAtMost(1.0f)
            val retentionRatio = if (wordsLearned > 0) {
                (wordsRetained.toFloat() / wordsLearned.toFloat()).coerceAtMost(1.0f)
            } else {
                0.0f
            }
            val masteryPercentage = (0.4f * learnedRatio + 0.6f * retentionRatio).coerceIn(0.0f, 1.0f)

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
        val masteryList = getCefrLevelMasteryList()
        val currentLevelMastery = masteryList.find { it.level.equals(currentLevelStr, ignoreCase = true) }

        val masteryPercentage = currentLevelMastery?.masteryPercentage ?: 0.0f
        val masteredCount = currentLevelMastery?.wordsRetained ?: 0

        // Unlock next level if level mastery > 35% or user has mastered at least 5 words at this level
        if (masteryPercentage >= 0.35f || masteredCount >= 5) {
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

        // Fetch today's reviews to calculate current fatigue
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todayStats = userDao.getStatisticsForDate(dateStr)
        val allLogs = userDao.getReviewHistoryOnce()
        
        // Parse today's starting timestamp to extract logs from today
        val todayStart = try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
        val todayLogs = allLogs.filter { it.timestamp >= todayStart }
        
        // Calculate fatigue and determine session configuration dynamically
        val fatigueState = adaptiveEngine.calculateFatigue(todayLogs)
        val sessionConfig = adaptiveEngine.determineSessionConfig(todayLogs, todayStats, fatigueState)

        // Prioritize all local cards
        val allCards = userDao.getAllCardsOnce()
        val prioritizedCards = adaptiveEngine.prioritizeCards(allCards, currentTime)

        // Select review/learning cards based on limits
        val dueAndLearningCards = prioritizedCards.filter { card ->
            card.reps > 0 && (card.dueDate <= currentTime || card.state == 1 || card.state == 3)
        }.take(sessionConfig.maxReviews)

        val selectedIds = dueAndLearningCards.map { it.id }.toSet()

        // Select new cards
        val newCardsFromLocal = prioritizedCards.filter { card ->
            card.reps == 0 && !selectedIds.contains(card.id)
        }.take(sessionConfig.maxNewWords)

        val totalSelected = mutableListOf<CardEntity>()
        totalSelected.addAll(dueAndLearningCards)
        totalSelected.addAll(newCardsFromLocal)

        // If we still need more words to fulfill the session length (e.g. if the user runs out of cards),
        // let's fetch brand new words from the allowed levels!
        val neededNewCount = sessionConfig.sessionLength - totalSelected.size
        if (neededNewCount > 0 && sessionConfig.maxNewWords > 0) {
            val allowedLevels = getAllowedLevels(progress.level)
            val allLocalWords = userDao.getAllCardsOnce().map { it.wordId }.toSet()
            
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
                val inserted = userDao.getCardByWordId(word.id)
                if (inserted != null) {
                    totalSelected.add(inserted)
                }
            }
        }

        // Save session state to database
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
