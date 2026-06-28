package com.example.features.tiki.ambient

import kotlin.random.Random

class AmbientScheduler(
    private val minIntervalMillis: Long = 6000L,
    private val maxIntervalMillis: Long = 18000L
) {
    private var isPaused: Boolean = false
    private var nextTriggerTimeMillis: Long = 0L
    private var lastTickTimeMillis: Long = System.currentTimeMillis()

    init {
        scheduleNext(lastTickTimeMillis)
    }

    fun pause() {
        isPaused = true
    }

    fun resume(currentTimeMillis: Long = System.currentTimeMillis()) {
        if (isPaused) {
            isPaused = false
            scheduleNext(currentTimeMillis)
        }
    }

    fun isPaused(): Boolean = isPaused

    fun shouldTrigger(currentTimeMillis: Long): Boolean {
        if (isPaused) return false
        
        if (currentTimeMillis >= nextTriggerTimeMillis) {
            scheduleNext(currentTimeMillis)
            return true
        }
        return false
    }

    fun scheduleNext(currentTimeMillis: Long) {
        val randomInterval = Random.nextLong(minIntervalMillis, maxIntervalMillis + 1)
        nextTriggerTimeMillis = currentTimeMillis + randomInterval
        lastTickTimeMillis = currentTimeMillis
    }

    fun getNextTriggerTimeMillis(): Long = nextTriggerTimeMillis
}
