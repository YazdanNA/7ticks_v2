package com.example.features.tiki.behavior

import com.example.features.tiki.api.TikiController
import com.example.features.tiki.memory.MemoryEngine

class BehaviorEngine(
    private val controller: TikiController? = null,
    var cooldownMillis: Long = 3000L,
    initialRules: List<BehaviorRule> = DefaultBehaviorRules.all,
    val memoryEngine: MemoryEngine = MemoryEngine.getInstance()
) {
    private val rules = initialRules.toMutableList()
    val history = BehaviorHistory()

    private var lastEmissionTimeMillis: Long = 0L
    private var currentActivePriority: Int = 0

    var onResultEmitted: ((BehaviorResult) -> Unit)? = null

    fun addRule(rule: BehaviorRule) {
        rules.add(rule)
    }

    fun removeRule(ruleName: String) {
        rules.removeAll { it.name == ruleName }
    }

    fun processEvent(
        event: BehaviorEvent,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): BehaviorResult? {
        if (event is BehaviorEvent.AppPaused) {
            // Can pause or handle state
        }

        if (event !is BehaviorEvent.SessionFinished) {
            memoryEngine.processEvent(event, currentTimeMillis)
        }
        history.recordEvent(event, currentTimeMillis)

        val results = rules.mapNotNull { it.evaluate(event, history) }
        val bestResult = results.maxByOrNull { it.priority }

        if (event is BehaviorEvent.SessionFinished) {
            memoryEngine.processEvent(event, currentTimeMillis)
        }

        if (bestResult == null) return null

        val elapsed = currentTimeMillis - lastEmissionTimeMillis
        if (elapsed < cooldownMillis && lastEmissionTimeMillis > 0L) {
            // Cooldown is active. Only allow if new priority strictly overrides current active priority.
            if (bestResult.priority <= currentActivePriority) {
                return null
            }
        }

        lastEmissionTimeMillis = currentTimeMillis
        currentActivePriority = bestResult.priority

        onResultEmitted?.invoke(bestResult)
        controller?.setEmotion(bestResult.emotion, forceInstant = bestResult.forceInstant)

        return bestResult
    }

    fun resetHistory() {
        history.clear()
        memoryEngine.reset()
        lastEmissionTimeMillis = 0L
        currentActivePriority = 0
    }

    companion object {
        @Volatile
        private var INSTANCE: BehaviorEngine? = null

        fun getInstance(controller: TikiController? = null): BehaviorEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = BehaviorEngine(controller)
                INSTANCE = instance
                instance
            }
        }
    }
}
