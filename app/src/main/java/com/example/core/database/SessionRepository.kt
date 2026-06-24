package com.example.core.database

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionRepository @Inject constructor(
    private val userDao: UserDao
) {
    fun getSessionState(): Flow<SessionStateEntity?> {
        return userDao.getSessionState()
    }

    suspend fun saveSessionState(
        active: Boolean,
        cardIds: List<Int>,
        currentIndex: Int,
        pendingReviews: Int
    ) {
        val state = SessionStateEntity(
            id = 0,
            active = active,
            cardIds = cardIds.joinToString(","),
            currentIndex = currentIndex,
            startTime = System.currentTimeMillis(),
            pendingReviews = pendingReviews,
            timestamp = System.currentTimeMillis()
        )
        userDao.saveSessionState(state)
    }

    suspend fun clearSession() {
        val state = SessionStateEntity(
            id = 0,
            active = false,
            cardIds = "",
            currentIndex = 0,
            startTime = 0,
            pendingReviews = 0,
            timestamp = 0
        )
        userDao.saveSessionState(state)
    }
}
