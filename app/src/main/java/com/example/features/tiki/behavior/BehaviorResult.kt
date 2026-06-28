package com.example.features.tiki.behavior

import com.example.features.tiki.model.EmotionState

data class BehaviorResult(
    val emotion: EmotionState,
    val priority: Int,
    val ruleName: String,
    val forceInstant: Boolean = false
)
