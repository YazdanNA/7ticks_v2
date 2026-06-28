package com.example.features.tiki.director

import kotlinx.coroutines.*

class DirectorScheduler(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) {
    private var scheduledJob: Job? = null

    fun schedule(delayMillis: Long, action: () -> Unit) {
        cancelPending()
        scheduledJob = scope.launch {
            delay(delayMillis)
            if (isActive) {
                action()
            }
        }
    }

    fun cancelPending() {
        scheduledJob?.cancel()
        scheduledJob = null
    }
}
