package com.example.features.tiki.context

data class ContextSnapshot(
    val sessionProgress: Float,
    val currentCardIndex: Int,
    val remainingCards: Int,
    val sessionDurationMillis: Long,
    val thinkingDurationMillis: Long,
    val currentStreak: Int,
    val todayStudyTimeMillis: Long,
    val todayReviewedCards: Int,
    val masterCountToday: Int,
    val currentCefrLevel: String,
    val isTranslationEnabled: Boolean,
    val isMoreDetailsOpened: Boolean,
    val currentScreen: String,
    val lastEvent: ContextEvent? = null
)
