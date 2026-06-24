package com.example.core.gamification

import com.example.core.database.AchievementEntity
import com.example.core.database.CardEntity
import com.example.core.database.ChallengeEntity
import com.example.core.database.ReviewHistoryEntity
import com.example.core.database.UserProgressEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Configurable XP values for various learning activities.
 */
object XpConfig {
    const val REVIEW_EASY = 20
    const val REVIEW_GOOD = 15
    const val REVIEW_HARD = 10
    const val REVIEW_AGAIN = 5
    const val CORRECT_BONUS = 10 // extra XP for correct answers (Good or Easy)
    const val SESSION_COMPLETE = 100
    const val CHALLENGE_COMPLETE = 150
    const val ACHIEVEMENT_UNLOCK = 300
    const val DAILY_GOAL_COMPLETE = 100
    const val STREAK_MILESTONE_MULTIPLIER = 10 // XP awarded per streak day at milestones
}

/**
 * GameEngine handles level-up, streak maintenance, achievement checking,
 * and challenge updates.
 */
class GamificationEngine {

    /**
     * Calculates the XP needed to transition from the current level to the next.
     */
    fun xpRequiredForLevel(level: Int): Int {
        return level * 500
    }

    /**
     * Determines whether a given streak number is a celebrated milestone.
     */
    fun isStreakMilestone(streak: Int): Boolean {
        val milestones = setOf(3, 7, 14, 30, 60, 100, 365)
        return milestones.contains(streak)
    }

    /**
     * Returns a text description for a streak milestone.
     */
    fun getStreakMilestoneName(streak: Int): String {
        return when (streak) {
            3 -> "Bronze Streak"
            7 -> "Silver Week"
            14 -> "Fortnight Commitment"
            30 -> "Monthly Scholar"
            60 -> "Diamond Core"
            100 -> "Century Mastery"
            365 -> "Ascended Polyglot"
            else -> "Streak Milestone"
        }
    }

    /**
     * Automatically generates modern daily challenges.
     */
    fun generateDailyChallenges(): List<ChallengeEntity> {
        return listOf(
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
            )
        )
    }

    /**
     * Automatically generates modern weekly challenges.
     */
    fun generateWeeklyChallenges(): List<ChallengeEntity> {
        return listOf(
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
                current = 0,
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
    }

    /**
     * Core streak verification logic.
     * Computes the updated progress based on the current system time and the last active day.
     */
    fun processStreak(progress: UserProgressEntity, currentTimeMs: Long): UserProgressEntity {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val todayStr = sdf.format(Date(currentTimeMs))
        val lastDayStr = progress.lastActiveDay

        if (todayStr == lastDayStr) {
            // Already logged activity today, do not alter streak
            return progress
        }

        // Calculate if yesterday was the last active day
        val yesterdayMs = currentTimeMs - (24L * 60 * 60 * 1000)
        val yesterdayStr = sdf.format(Date(yesterdayMs))

        val newStreak: Int
        if (lastDayStr == yesterdayStr || lastDayStr.isEmpty()) {
            // Yesterday was active, or it is the very first time setting up lastActiveDay
            newStreak = progress.streak + 1
        } else {
            // Streak broken!
            newStreak = 1
        }

        val newLongest = if (newStreak > progress.longestStreak) newStreak else progress.longestStreak

        return progress.copy(
            streak = newStreak,
            longestStreak = newLongest,
            lastActiveDay = todayStr,
            lastReviewTime = currentTimeMs
        )
    }

    /**
     * Evaluates all achievement criteria based on user history.
     * Returns a list of newly unlocked achievements.
     */
    fun evaluateAchievements(
        currentAchievements: List<AchievementEntity>,
        allCards: List<CardEntity>,
        reviewHistory: List<ReviewHistoryEntity>,
        progress: UserProgressEntity,
        currentTimeMs: Long
    ): List<AchievementEntity> {
        val newlyUnlocked = mutableListOf<AchievementEntity>()

        // Helper maps
        val achMap = currentAchievements.associateBy { it.id }

        // 1. First Review (first_review)
        val firstReviewAch = achMap["first_review"]
        if (firstReviewAch != null && !firstReviewAch.unlocked && reviewHistory.isNotEmpty()) {
            newlyUnlocked.add(firstReviewAch.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 2. First Session (first_session)
        // Can be evaluated explicitly at the end of a session, but let's check review logs
        val firstSessionAch = achMap["first_session"]
        if (firstSessionAch != null && !firstSessionAch.unlocked && reviewHistory.size >= 5) {
            newlyUnlocked.add(firstSessionAch.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 3. 100 Reviews (reviews_100)
        val reviews100Ach = achMap["reviews_100"]
        if (reviews100Ach != null && !reviews100Ach.unlocked && reviewHistory.size >= 100) {
            newlyUnlocked.add(reviews100Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 4. 1000 Reviews (reviews_1000)
        val reviews1000Ach = achMap["reviews_1000"]
        if (reviews1000Ach != null && !reviews1000Ach.unlocked && reviewHistory.size >= 1000) {
            newlyUnlocked.add(reviews1000Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 5. 7 Day Streak (streak_7)
        val streak7Ach = achMap["streak_7"]
        if (streak7Ach != null && !streak7Ach.unlocked && progress.streak >= 7) {
            newlyUnlocked.add(streak7Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 6. 30 Day Streak (streak_30)
        val streak30Ach = achMap["streak_30"]
        if (streak30Ach != null && !streak30Ach.unlocked && progress.streak >= 30) {
            newlyUnlocked.add(streak30Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 7. Level 10 (level_10)
        val level10Ach = achMap["level_10"]
        if (level10Ach != null && !level10Ach.unlocked && progress.level >= 10) {
            newlyUnlocked.add(level10Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // 8. Level 25 (level_25)
        val level25Ach = achMap["level_25"]
        if (level25Ach != null && !level25Ach.unlocked && progress.level >= 25) {
            newlyUnlocked.add(level25Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        // Mastery achievements depend on stabilized cards in Room (boxIndex >= 7 or stability >= 2.0)
        // Let's check Level-specific mastery (e.g. Master A1, Master A2, Master B1)
        // We will assume A1 is Level 1, A2 is Level 2, etc.
        // We can pass pre-calculated level mastery statistics or check card status
        // Let's assume A1 has 10+ words in box >= 7
        val a1MasterCount = allCards.count { it.boxIndex >= 7 } // General proxy
        
        val masterA1Ach = achMap["master_a1"]
        if (masterA1Ach != null && !masterA1Ach.unlocked && a1MasterCount >= 10) {
            newlyUnlocked.add(masterA1Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        val masterA2Ach = achMap["master_a2"]
        if (masterA2Ach != null && !masterA2Ach.unlocked && a1MasterCount >= 25 && progress.level >= 2) {
            newlyUnlocked.add(masterA2Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        val masterB1Ach = achMap["master_b1"]
        if (masterB1Ach != null && !masterB1Ach.unlocked && a1MasterCount >= 50 && progress.level >= 3) {
            newlyUnlocked.add(masterB1Ach.copy(unlocked = true, unlockedAt = currentTimeMs))
        }

        return newlyUnlocked
    }
}
