package com.example.core.learning.engine

/**
 * Handles the calculation of rolling statistics over the last 30 days.
 */
class RollingStatisticsRepository(private val dao: SmartSessionDao) {
    suspend fun getRollingAverageSeconds(): Double {
        val stats = dao.getDailyStatsOnce()
        if (stats.isEmpty()) {
            return 20.0 // Default initial estimate if no history exists
        }
        val totalSeconds = stats.sumOf { it.totalStudyTimeSeconds }
        val totalCards = stats.sumOf { it.cardsReviewedCount }
        return if (totalCards > 0) {
            totalSeconds.toDouble() / totalCards
        } else {
            20.0
        }
    }

    suspend fun getDailyStatsHistory(): List<SmartDailyStatsEntity> {
        return dao.getDailyStatsOnce()
    }
}
