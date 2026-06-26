package com.example.core.learning.engine

/**
 * Calculates session capacity based on the user's requested study duration (seconds)
 * and their rolling average thinking time per card.
 */
class SessionCapacityCalculator {
    fun calculateCapacity(studySeconds: Int, rollingAverageSecondsPerCard: Double): Int {
        val avg = if (rollingAverageSecondsPerCard <= 0.0) 20.0 else rollingAverageSecondsPerCard
        // Calculate capacity, ensuring a minimum of 1 card
        return Math.max(1, (studySeconds / avg).toInt())
    }
}
