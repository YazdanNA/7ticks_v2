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
        val applicableRules = rules.filter { rule ->
            if (categoryFilter != null && rule.category !in categoryFilter) return@filter false
            if (emotionFilter != null && context.currentEmotion !in emotionFilter) return@filter false
            true
        }

        val results = applicableRules.mapNotNull { it.evaluate(context) }

        if (results.isEmpty()) {
            return getEmptyFallback()
        }

        val highestPriority = results.maxOf { it.priority }
        val topResults = results.filter { it.priority == highestPriority }

        // Combine candidates from all top priority rules
        val allCandidates = topResults.flatMap { it.candidates }

        val selectedText = selector.select(allCandidates, history)
        if (selectedText.isNotEmpty()) {
            history.recordSpoken(selectedText)
        }

        val winningCategory = topResults.first().category

        return DialogueResult(
            selectedDialogue = selectedText,
            category = winningCategory,
            priority = highestPriority
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
