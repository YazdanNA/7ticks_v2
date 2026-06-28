package com.example.features.tiki.director

import com.example.features.tiki.model.EmotionState

data class DirectorState(
    val isSpeaking: Boolean = false,
    val lastDecision: DirectorDecision? = null,
    val lastDecisionTimeMillis: Long = 0L,
    val activePriority: Int = 0,
    val currentEmotion: EmotionState = EmotionState.POKER,
    val cooldownExpirationMillis: Long = 0L,
    val isPaused: Boolean = false,
    val history: List<DirectorDecision> = emptyList()
)
