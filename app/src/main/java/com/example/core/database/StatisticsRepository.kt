package com.example.core.database

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StatisticsRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getAllStatistics(): Flow<List<StatisticsEntity>> {
        return userDao.getAllStatistics()
    }

    suspend fun getStatisticsForDate(dateStr: String): StatisticsEntity? {
        return userDao.getStatisticsForDate(dateStr)
    }

    suspend fun saveStatistics(statistics: StatisticsEntity) {
        userDao.insertStatistics(statistics)
    }
}
