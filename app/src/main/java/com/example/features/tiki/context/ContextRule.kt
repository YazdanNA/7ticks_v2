package com.example.features.tiki.context

data class ContextRecommendation(
    val reactionName: String,
    val priority: Int,
    val suggestedDialogueCategory: String? = null
)

interface ContextRule {
    val ruleName: String
    val priority: Int
    fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation?
}
