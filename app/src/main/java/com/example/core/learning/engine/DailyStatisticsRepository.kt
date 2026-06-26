package com.example.core.learning.engine

import kotlinx.coroutines.flow.Flow

/**
 * Handles persistence of daily summaries and individual card review logs.
 */
class DailyStatisticsRepository(private val dao: SmartSessionDao) {
    suspend fun saveReviewLog(log: SmartReviewLogEntity) {
        dao.insertReviewLog(log)
    }

    suspend fun getReviewLogsForDate(dateStr: String): List<SmartReviewLogEntity> {
        return dao.getReviewLogsForDate(dateStr)
    }

    suspend fun getDailyStatsForDate(dateStr: String): SmartDailyStatsEntity? {
        return dao.getDailyStatsForDate(dateStr)
    }

    suspend fun saveDailyStats(stats: SmartDailyStatsEntity) {
        dao.insertDailyStatsAndPrune(stats)
    }

    fun getDailyStatsFlow(): Flow<List<SmartDailyStatsEntity>> {
        return dao.getDailyStatsFlow()
    }
}
