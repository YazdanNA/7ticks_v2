package com.example.features.tiki.memory

data class MemorySnapshot(
    val recentAgainCount: Int,
    val recentHardCount: Int,
    val recentGoodCount: Int,
    val recentEasyCount: Int,
    val currentStreak: Int,
    val currentEasyStreak: Int,
    val longestEasyStreak: Int,
    val currentAgainStreak: Int,
    val longestAgainStreak: Int,
    val averageThinkingTimeMillis: Long,
    val cardsAnswered: Int,
    val sessionElapsedTimeMillis: Long,
    val translationUsageCount: Int,
    val moreDetailsUsageCount: Int,
    val numberOfFlips: Int,
    val totalEventsInStore: Int,
    val learningMood: LearningMood
)
