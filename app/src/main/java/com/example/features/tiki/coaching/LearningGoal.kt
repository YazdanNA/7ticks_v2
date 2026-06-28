package com.example.features.tiki.coaching

sealed interface LearningGoal {
    val id: String
    val title: String
    val description: String

    data class DailyStudy(
        override val id: String = "daily_study",
        override val title: String = "Daily Study",
        override val description: String = "Spend time studying your active lessons",
        val targetMinutes: Int
    ) : LearningGoal

    data class DailyReview(
        override val id: String = "daily_review",
        override val title: String = "Daily Review",
        override val description: String = "Review your active cards to reinforce knowledge",
        val targetReviewCount: Int
    ) : LearningGoal

    data class DailyNewWords(
        override val id: String = "daily_new_words",
        override val title: String = "Daily New Words",
        override val description: String = "Learn new vocabulary words",
        val targetNewWords: Int
    ) : LearningGoal

    data class WeeklyConsistency(
        override val id: String = "weekly_consistency",
        override val title: String = "Weekly Consistency",
        override val description: String = "Maintain consistency throughout the week",
        val targetDays: Int
    ) : LearningGoal

    data class MasterWords(
        override val id: String = "master_words",
        override val title: String = "Master Words",
        override val description: String = "Reach mastered status on difficult words",
        val targetMasteredWordsCount: Int
    ) : LearningGoal

    data class LongestStreak(
        override val id: String = "longest_streak",
        override val title: String = "Longest Streak",
        override val description: String = "Keep your consecutive learning streak alive",
        val targetStreakDays: Int
    ) : LearningGoal

    data class SessionCompletion(
        override val id: String = "session_completion",
        override val title: String = "Session Completion",
        override val description: String = "Complete highly-focused learning sessions",
        val targetSessionsCount: Int
    ) : LearningGoal

    data class ReviewQueueCleanup(
        override val id: String = "review_queue_cleanup",
        override val title: String = "Review Queue Cleanup",
        override val description: String = "Fully clear the review queue to zero",
        val targetQueueSize: Int = 0
    ) : LearningGoal

    data class CustomGoal(
        override val id: String,
        override val title: String,
        override val description: String,
        val targetValue: Float
    ) : LearningGoal
}
