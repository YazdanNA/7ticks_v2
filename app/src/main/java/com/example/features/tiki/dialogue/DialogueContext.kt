package com.example.features.tiki.dialogue

import com.example.features.tiki.behavior.BehaviorEvent
import com.example.features.tiki.memory.MemorySnapshot
import com.example.features.tiki.model.EmotionState

data class DialogueContext(
    val currentEmotion: EmotionState,
    val behaviorEvent: BehaviorEvent? = null,
    val memorySnapshot: MemorySnapshot? = null,
    val sessionProgress: Float = 0f,
    val thinkingTimeMillis: Long = 0L
)
