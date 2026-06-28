package com.example.features.tiki.director

import com.example.features.tiki.model.EmotionState

sealed interface DirectorDecision {
    data class ShowEmotion(val emotion: EmotionState) : DirectorDecision
    data class PlayDialogue(val text: String, val category: String? = null) : DirectorDecision
    data class SpeakAndShowEmotion(val text: String, val emotion: EmotionState) : DirectorDecision
    object RemainSilent : DirectorDecision
    data class PlayCelebration(val celebrationName: String) : DirectorDecision
    data class PlayIdleAnimation(val animationName: String) : DirectorDecision
    data class DelayedDecision(val delayMillis: Long, val nextDecision: DirectorDecision) : DirectorDecision
}
