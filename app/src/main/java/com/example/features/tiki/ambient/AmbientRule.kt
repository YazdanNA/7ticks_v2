package com.example.features.tiki.ambient

interface AmbientRule {
    val ruleName: String
    val priority: AmbientPriority
    fun evaluate(state: AmbientState): AmbientAction?
}
