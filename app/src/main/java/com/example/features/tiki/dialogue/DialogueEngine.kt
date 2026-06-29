package com.example.features.tiki.dialogue

import com.example.features.tiki.model.EmotionState

data class DialogueResult(
    val selectedDialogue: String,
    val category: DialogueCategory,
    val priority: Int
)

class DialogueEngine(
    initialRules: List<DialogueRule> = DefaultDialogueRules.all,
    val selector: DialogueSelector = DialogueSelector(),
    val history: DialogueHistory = DialogueHistory()
) {
    private val rules = initialRules.toMutableList()

    fun addRule(rule: DialogueRule) {
        rules.add(rule)
    }

    fun removeRule(ruleName: String) {
        rules.removeAll { it.ruleName == ruleName }
    }

    fun selectDialogue(
        context: DialogueContext,
        categoryFilter: Set<DialogueCategory>? = null,
        emotionFilter: Set<EmotionState>? = null
    ): DialogueResult {
        val tikiEngine = com.example.features.tiki.engine.TikiEngine.getInstance()
        val snapshot = context.memorySnapshot
        val categoryStr = when (context.behaviorEvent) {
            is com.example.features.tiki.behavior.BehaviorEvent.SessionStarted -> "Greeting"
            is com.example.features.tiki.behavior.BehaviorEvent.SessionFinished -> "SessionComplete"
            is com.example.features.tiki.behavior.BehaviorEvent.CardFlipped -> "CardFlipped"
            is com.example.features.tiki.behavior.BehaviorEvent.CardAnsweredEasy -> "EasyStreak"
            is com.example.features.tiki.behavior.BehaviorEvent.CardAnsweredGood -> "Good"
            is com.example.features.tiki.behavior.BehaviorEvent.CardAnsweredHard -> "Hard"
            is com.example.features.tiki.behavior.BehaviorEvent.CardAnsweredAgain -> "Again"
            is com.example.features.tiki.behavior.BehaviorEvent.CardThinkingStarted -> "Thinking"
            is com.example.features.tiki.behavior.BehaviorEvent.TranslationOpened -> "TranslationOpened"
            is com.example.features.tiki.behavior.BehaviorEvent.MoreDetailsOpened -> "MoreDetailsOpened"
            else -> null
        }

        val langCode = com.example.features.tiki.engine.TikiEngine.getAppLanguage()

        val resolvedMetadata = tikiEngine.contentEngine.resolveDialogue(
            category = categoryStr,
            language = langCode,
            emotion = context.currentEmotion.name,
            relationshipLevel = tikiEngine.relationshipEngine.getSnapshot().level,
            currentStreak = snapshot?.currentStreak ?: 0,
            sessionProgress = context.sessionProgress,
            thinkingState = if (context.thinkingTimeMillis > 0) "THINKING_LONG" else null
        )

        val text = resolvedMetadata?.text ?: "Ready!"
        if (text.isNotEmpty()) {
            history.recordSpoken(text)
        }

        return DialogueResult(
            selectedDialogue = text,
            category = DialogueCategory.Idle,
            priority = resolvedMetadata?.priority ?: DialoguePriority.FALLBACK
        )
    }

    fun resetHistory() {
        history.clear()
    }

    private fun getEmptyFallback(): DialogueResult {
        val fallbackText = "Ready!"
        history.recordSpoken(fallbackText)
        return DialogueResult(
            selectedDialogue = fallbackText,
            category = DialogueCategory.Idle,
            priority = DialoguePriority.FALLBACK
        )
    }

    companion object {
        @Volatile
        private var INSTANCE: DialogueEngine? = null

        fun getInstance(): DialogueEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = DialogueEngine()
                INSTANCE = instance
                instance
            }
        }
    }
}
