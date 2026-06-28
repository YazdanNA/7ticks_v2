package com.example.features.tiki.dialogue

data class WeightedDialogue(
    val text: String,
    val weight: Int = 1
) {
    init {
        if (text.isNotEmpty()) {
            val words = text.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            require(words.size <= 5) { "Dialogue text exceeds 5 words limit: '$text'" }
        }
    }
}

data class DialogueRuleResult(
    val category: DialogueCategory,
    val priority: Int,
    val candidates: List<WeightedDialogue>
)

interface DialogueRule {
    val ruleName: String
    val category: DialogueCategory
    val priority: Int
    fun evaluate(context: DialogueContext): DialogueRuleResult?
}
