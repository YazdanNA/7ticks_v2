package com.example.features.tiki.coaching

enum class SuggestionTiming {
    BEFORE_SESSION,
    AFTER_SESSION,
    HOME_SCREEN,
    LONG_INACTIVITY,
    GOAL_COMPLETION
}

data class CoachingContext(
    val progresses: Map<String, GoalProgress>,
    val remainingReviews: Int,
    val studyDurationMinutes: Int,
    val currentStreak: Int,
    val recentPerformanceScore: Float, // 0f to 1f (representing review correct percentage)
    val relationshipLevel: Int,
    val timing: SuggestionTiming,
    val preferredStyle: CoachingStyle = CoachingStyle.Encouraging
)

fun interface CoachingRule {
    fun evaluate(context: CoachingContext): CoachingSuggestion?
}
