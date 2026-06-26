package com.example.core.learning.engine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Coordinates tracking, logging, and persisting study metrics.
 */
class StudyStatisticsManager(
    private val dailyRepo: DailyStatisticsRepository,
    private val rollingRepo: RollingStatisticsRepository
) {
    fun getCurrentDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    suspend fun logReview(
        cardId: Int,
        elapsedSeconds: Int,
        rating: Int,
        dateStr: String = getCurrentDateString()
    ) {
        if (elapsedSeconds < 1) return // Censor invalid measurements

        val log = SmartReviewLogEntity(
            dateStr = dateStr,
            elapsedSeconds = elapsedSeconds,
            cardId = cardId,
            rating = rating
        )
        dailyRepo.saveReviewLog(log)

        // Automatically recompute and update daily summary
        recomputeDailySummary(dateStr)
    }

    suspend fun recomputeDailySummary(dateStr: String = getCurrentDateString()) {
        val logs = dailyRepo.getReviewLogsForDate(dateStr)
        if (logs.isEmpty()) return

        val totalStudyTimeSeconds = logs.sumOf { it.elapsedSeconds }
        val cardsReviewedCount = logs.size
        val averageSeconds = if (cardsReviewedCount > 0) {
            totalStudyTimeSeconds.toDouble() / cardsReviewedCount
        } else {
            0.0
        }

        val stats = SmartDailyStatsEntity(
            dateStr = dateStr,
            averageSeconds = averageSeconds,
            totalStudyTimeSeconds = totalStudyTimeSeconds,
            cardsReviewedCount = cardsReviewedCount
        )
        dailyRepo.saveDailyStats(stats)
    }

    suspend fun getRollingAverageSeconds(): Double {
        return rollingRepo.getRollingAverageSeconds()
    }
}
