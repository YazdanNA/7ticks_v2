package com.example.features.tiki.ambient

class IdleDetector(private val idleThresholdMillis: Long = 5000L) {
    private var lastInteractionTimeMillis: Long = System.currentTimeMillis()

    fun onInteraction(currentTimeMillis: Long = System.currentTimeMillis()) {
        lastInteractionTimeMillis = currentTimeMillis
    }

    fun isIdle(currentTimeMillis: Long = System.currentTimeMillis()): Boolean {
        return (currentTimeMillis - lastInteractionTimeMillis) >= idleThresholdMillis
    }

    fun getIdleDuration(currentTimeMillis: Long = System.currentTimeMillis()): Long {
        return (currentTimeMillis - lastInteractionTimeMillis).coerceAtLeast(0L)
    }

    fun reset(currentTimeMillis: Long = System.currentTimeMillis()) {
        lastInteractionTimeMillis = currentTimeMillis
    }
}
