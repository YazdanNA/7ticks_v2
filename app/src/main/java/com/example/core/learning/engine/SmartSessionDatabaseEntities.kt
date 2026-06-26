package com.example.core.learning.engine

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "smart_review_logs")
data class SmartReviewLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dateStr: String, // "YYYY-MM-DD"
    val elapsedSeconds: Int,
    val cardId: Int,
    val rating: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_daily_stats")
data class SmartDailyStatsEntity(
    @PrimaryKey val dateStr: String, // "YYYY-MM-DD"
    val averageSeconds: Double,
    val totalStudyTimeSeconds: Int,
    val cardsReviewedCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "smart_session_stats")
data class SmartSessionStatsEntity(
    @PrimaryKey val id: Int = 0,
    val studySeconds: Int,
    val capacity: Int,
    val remainingCapacity: Int,
    val isFinished: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

@Dao
interface SmartSessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviewLog(log: SmartReviewLogEntity)

    @Query("SELECT * FROM smart_review_logs WHERE dateStr = :dateStr")
    suspend fun getReviewLogsForDate(dateStr: String): List<SmartReviewLogEntity>

    @Query("SELECT * FROM smart_review_logs ORDER BY timestamp DESC")
    suspend fun getAllReviewLogs(): List<SmartReviewLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyStats(stats: SmartDailyStatsEntity)

    @Query("SELECT * FROM smart_daily_stats WHERE dateStr = :dateStr")
    suspend fun getDailyStatsForDate(dateStr: String): SmartDailyStatsEntity?

    @Query("SELECT * FROM smart_daily_stats ORDER BY dateStr DESC")
    fun getDailyStatsFlow(): Flow<List<SmartDailyStatsEntity>>

    @Query("SELECT * FROM smart_daily_stats ORDER BY dateStr DESC")
    suspend fun getDailyStatsOnce(): List<SmartDailyStatsEntity>

    @Query("DELETE FROM smart_daily_stats WHERE dateStr NOT IN (:datesToKeep)")
    suspend fun deleteDailyStatsNotIn(datesToKeep: List<String>)

    @Query("DELETE FROM smart_review_logs WHERE dateStr NOT IN (:datesToKeep)")
    suspend fun deleteReviewLogsNotIn(datesToKeep: List<String>)

    @Transaction
    suspend fun insertDailyStatsAndPrune(stats: SmartDailyStatsEntity) {
        insertDailyStats(stats)
        val all = getDailyStatsOnce()
        if (all.size > 30) {
            val toKeep = all.take(30).map { it.dateStr }
            deleteDailyStatsNotIn(toKeep)
            deleteReviewLogsNotIn(toKeep)
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionStats(stats: SmartSessionStatsEntity)

    @Query("SELECT * FROM smart_session_stats WHERE id = 0")
    suspend fun getSessionStats(): SmartSessionStatsEntity?

    @Query("DELETE FROM smart_session_stats")
    suspend fun clearSessionStats()
}
