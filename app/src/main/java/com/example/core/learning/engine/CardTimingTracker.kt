package com.example.core.learning.engine

/**
 * Tracks thinking time for active flashcards.
 * Supports pause/resume rules for app lifecycle events, screen locks, split screen, and details modals.
 * Censors readings under 1 second, and caps them at 60 seconds.
 */
open class CardTimingTracker {
    private var startTime: Long = 0L
    private var accumulatedTimeMs: Long = 0L
    private var isRunning: Boolean = false

    @Synchronized
    open fun start() {
        startTime = System.currentTimeMillis()
        accumulatedTimeMs = 0L
        isRunning = true
    }

    @Synchronized
    open fun pause() {
        if (isRunning) {
            accumulatedTimeMs += System.currentTimeMillis() - startTime
            isRunning = false
        }
    }

    @Synchronized
    open fun resume() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()
            isRunning = true
        }
    }

    @Synchronized
    open fun stop(): Int {
        if (isRunning) {
            accumulatedTimeMs += System.currentTimeMillis() - startTime
            isRunning = false
        }
        val totalSeconds = (accumulatedTimeMs / 1000L).toInt()
        return when {
            totalSeconds < 1 -> -1 // Ignore invalid readings under 1 second
            totalSeconds > 60 -> 60 // Cap at 60 seconds
            else -> totalSeconds
        }
    }

    @Synchronized
    open fun getAccumulatedSeconds(): Int {
        var extra = 0L
        if (isRunning) {
            extra = System.currentTimeMillis() - startTime
        }
        return ((accumulatedTimeMs + extra) / 1000L).toInt()
    }
}
