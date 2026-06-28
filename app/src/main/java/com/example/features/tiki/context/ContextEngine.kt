package com.example.features.tiki.context

class ContextEngine(
    val state: ContextState = ContextState(),
    initialRules: List<ContextRule> = DefaultContextRules.all,
    val dispatcher: ContextDispatcher = ContextDispatcher()
) {
    private val rules = initialRules.toMutableList()

    fun addRule(rule: ContextRule) {
        rules.add(rule)
    }

    fun removeRule(ruleName: String) {
        rules.removeAll { it.ruleName == ruleName }
    }

    fun onEvent(
        event: ContextEvent,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): ContextRecommendation? {
        state.onEvent(event, currentTimeMillis)
        val snapshot = state.toSnapshot()

        val results = rules.mapNotNull { it.evaluate(event, snapshot) }
        val bestRecommendation = results.maxByOrNull { it.priority }

        dispatcher.dispatch(event, snapshot, bestRecommendation)
        return bestRecommendation
    }

    fun getSnapshot(): ContextSnapshot {
        return state.toSnapshot()
    }

    fun addObserver(observer: ContextObserver) {
        dispatcher.addObserver(observer)
    }

    fun removeObserver(observer: ContextObserver) {
        dispatcher.removeObserver(observer)
    }

    fun reset() {
        state.reset()
        dispatcher.clear()
    }

    companion object {
        @Volatile
        private var INSTANCE: ContextEngine? = null

        fun getInstance(): ContextEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = ContextEngine()
                INSTANCE = instance
                instance
            }
        }
    }
}
