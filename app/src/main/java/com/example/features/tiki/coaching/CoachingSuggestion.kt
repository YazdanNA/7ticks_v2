package com.example.features.tiki.coaching

data class CoachingSuggestion(
    val id: String,
    val text: String,
    val priority: Int,
    val actionType: CoachingActionType,
    val coachingStyle: CoachingStyle = CoachingStyle.Encouraging
)

enum class CoachingActionType {
    START_TODAY_SESSION,
    FINISH_TODAY_REVIEWS,
    REVIEW_DIFFICULT_WORDS,
    CONTINUE_YOUR_STREAK,
    TAKE_A_SHORT_BREAK,
    CELEBRATE_ACHIEVEMENT
}

enum class CoachingStyle {
    Encouraging,
    Gentle,
    Playful,
    Calm
}
