package com.example.features.tiki.director

import com.example.features.tiki.model.EmotionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DirectorEngine(
    private val queue: DirectorQueue = DirectorQueue(),
    private val scheduler: DirectorScheduler = DirectorScheduler(),
    private val rules: DirectorRules = DirectorRules
) {
    private val _state = MutableStateFlow(DirectorState())
    val state: StateFlow<DirectorState> = _state.asStateFlow()

    private val decisionListeners = mutableSetOf<(DirectorDecision) -> Unit>()

    fun addDecisionListener(listener: (DirectorDecision) -> Unit) {
        decisionListeners.add(listener)
    }

    fun removeDecisionListener(listener: (DirectorDecision) -> Unit) {
        decisionListeners.remove(listener)
    }

    /**
     * Submit an event decision from any subsystem to the Director Engine.
     */
    fun submitEvent(
        decision: DirectorDecision,
        priority: DirectorPriority,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): DirectorDecision? {
        val current = _state.value
        if (current.isPaused) return null

        // 1. Conflict and Priority Filtering
        if (!DirectorRules.shouldProcessEvent(priority, current, currentTimeMillis)) {
            // Outprioritized or during active cooldown, discard
            return null
        }

        // 2. Preemption & Cancellation: If the incoming priority is strictly higher than current active priority,
        // we preempt and cancel the lower-priority ongoing activities/schedulers.
        if (priority.value > current.activePriority) {
            scheduler.cancelPending()
            queue.removeLowerPriorityThan(priority)
        }

        // 3. Dialogue & Action Refinement (e.g. natural silence insertion for lower priority events)
        val refinedDecision = DirectorRules.refineDecision(decision, priority)

        // 4. Decision Processing
        when (refinedDecision) {
            is DirectorDecision.DelayedDecision -> {
                // Schedule the inner decision to execute after the delay
                val delayTime = refinedDecision.delayMillis
                val next = refinedDecision.nextDecision
                scheduler.schedule(delayTime) {
                    executeDecision(next, priority, System.currentTimeMillis())
                }
                
                // Track that we are waiting for a delayed action of this priority
                _state.value = current.copy(
                    lastDecision = refinedDecision,
                    lastDecisionTimeMillis = currentTimeMillis,
                    activePriority = priority.value
                )
                return refinedDecision
            }
            else -> {
                executeDecision(refinedDecision, priority, currentTimeMillis)
                return refinedDecision
            }
        }
    }

    /**
     * Submits a list of simultaneous candidate events to be resolved and executed.
     */
    fun submitSimultaneousEvents(
        candidates: List<Pair<DirectorDecision, DirectorPriority>>,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): DirectorDecision? {
        val winner = DirectorRules.resolveConflict(candidates) ?: return null
        return submitEvent(winner.first, winner.second, currentTimeMillis)
    }

    private fun executeDecision(
        decision: DirectorDecision,
        priority: DirectorPriority,
        currentTimeMillis: Long
    ) {
        val current = _state.value
        if (current.isPaused) return

        // Compute cooldown expiration
        val cooldownDuration = DirectorRules.calculateCooldownDuration(priority)
        val newCooldownExpiration = currentTimeMillis + cooldownDuration

        // Update active speaking or emotion states based on the decision
        var isSpeaking = current.isSpeaking
        var newEmotion = current.currentEmotion

        when (decision) {
            is DirectorDecision.PlayDialogue -> {
                isSpeaking = true
            }
            is DirectorDecision.SpeakAndShowEmotion -> {
                isSpeaking = true
                newEmotion = decision.emotion
            }
            is DirectorDecision.ShowEmotion -> {
                newEmotion = decision.emotion
            }
            is DirectorDecision.RemainSilent -> {
                // If silent, speaking can be false/stopped
                isSpeaking = false
            }
            else -> {
                // Keep existing speaking status or reset
            }
        }

        val updatedHistory = (current.history + decision).takeLast(10)

        _state.value = current.copy(
            isSpeaking = isSpeaking,
            lastDecision = decision,
            lastDecisionTimeMillis = currentTimeMillis,
            activePriority = priority.value,
            currentEmotion = newEmotion,
            cooldownExpirationMillis = newCooldownExpiration,
            history = updatedHistory
        )

        // Notify listeners of the final selected presentation
        decisionListeners.forEach { it(decision) }
    }

    fun setSpeaking(speaking: Boolean) {
        val current = _state.value
        _state.value = current.copy(isSpeaking = speaking)
    }

    fun setPaused(paused: Boolean) {
        val current = _state.value
        if (paused) {
            scheduler.cancelPending()
        }
        _state.value = current.copy(isPaused = paused)
    }

    fun cancelCurrent() {
        scheduler.cancelPending()
        queue.clear()
        val current = _state.value
        _state.value = current.copy(
            isSpeaking = false,
            activePriority = 0,
            cooldownExpirationMillis = 0L
        )
    }

    fun reset() {
        cancelCurrent()
        _state.value = DirectorState()
        decisionListeners.clear()
    }

    companion object {
        @Volatile
        private var INSTANCE: DirectorEngine? = null

        fun getInstance(): DirectorEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = DirectorEngine()
                INSTANCE = instance
                instance
            }
        }
    }
}
