package com.example.features.tiki.ambient

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AmbientEngine(
    private val scheduler: AmbientScheduler = AmbientScheduler(),
    private val idleDetector: IdleDetector = IdleDetector(),
    private val rules: List<AmbientRule> = DefaultAmbientRules.all
) {
    private val _state = MutableStateFlow(AmbientState())
    val state: StateFlow<AmbientState> = _state.asStateFlow()

    private val actionListeners = mutableSetOf<(AmbientAction) -> Unit>()

    fun addActionListener(listener: (AmbientAction) -> Unit) {
        actionListeners.add(listener)
    }

    fun removeActionListener(listener: (AmbientAction) -> Unit) {
        actionListeners.remove(listener)
    }

    fun setSpeaking(speaking: Boolean) {
        val current = _state.value
        val newPriority = if (speaking) AmbientPriority.SPEECH else AmbientPriority.IDLE
        _state.value = current.copy(
            isSpeaking = speaking,
            currentPriority = newPriority
        )
    }

    fun setThinking(thinking: Boolean, currentTimeMillis: Long = System.currentTimeMillis()) {
        val current = _state.value
        if (thinking) {
            idleDetector.onInteraction(currentTimeMillis)
            _state.value = current.copy(
                isThinking = true,
                thinkingDurationMillis = 0L,
                currentPriority = AmbientPriority.AMBIENT_ANIMATION
            )
        } else {
            _state.value = current.copy(
                isThinking = false,
                thinkingDurationMillis = 0L,
                currentPriority = AmbientPriority.IDLE
            )
        }
    }

    fun setPaused(paused: Boolean, currentTimeMillis: Long = System.currentTimeMillis()) {
        val current = _state.value
        if (paused) {
            scheduler.pause()
            _state.value = current.copy(isPaused = true)
        } else {
            scheduler.resume(currentTimeMillis)
            _state.value = current.copy(isPaused = false)
        }
    }

    fun onUserInteraction(currentTimeMillis: Long = System.currentTimeMillis()) {
        idleDetector.onInteraction(currentTimeMillis)
    }

    /**
     * Ticks the ambient engine. No heavy busy-loops; this is driven by
     * periodic scheduler checks or event loops in the application.
     */
    fun tick(currentTimeMillis: Long = System.currentTimeMillis()): AmbientAction? {
        val current = _state.value
        if (current.isPaused || current.isSpeaking) return null

        // Update thinking duration if active
        var updatedThinkingDuration = current.thinkingDurationMillis
        if (current.isThinking) {
            updatedThinkingDuration = idleDetector.getIdleDuration(currentTimeMillis)
            _state.value = current.copy(thinkingDurationMillis = updatedThinkingDuration)
        }

        // 1. Evaluate high priority rule (Thinking progression)
        if (current.isThinking) {
            val ruleAction = DefaultAmbientRules.thinkingRule.evaluate(_state.value)
            if (ruleAction != null && ruleAction != current.lastAction) {
                emitAction(ruleAction)
                return ruleAction
            }
            return null
        }

        // 2. Otherwise, check if Scheduler signals it's time for an idle animation
        if (scheduler.shouldTrigger(currentTimeMillis)) {
            val idleAction = DefaultAmbientRules.idleAnimationRule.evaluate(_state.value)
            if (idleAction != null) {
                emitAction(idleAction)
                return idleAction
            }
        }

        return null
    }

    private fun emitAction(action: AmbientAction) {
        val current = _state.value
        
        // Update recent action history (keeping last 4 actions to prevent loops/repetition)
        val updatedRecent = (current.recentActions + action).takeLast(4)
        
        _state.value = current.copy(
            lastAction = action,
            recentActions = updatedRecent
        )

        actionListeners.forEach { it(action) }
    }

    fun reset() {
        val currentTime = System.currentTimeMillis()
        idleDetector.reset(currentTime)
        scheduler.scheduleNext(currentTime)
        _state.value = AmbientState()
        actionListeners.clear()
    }
}
