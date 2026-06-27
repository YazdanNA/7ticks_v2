package com.example.features.tiki.api

import com.example.features.tiki.model.EmotionState

sealed interface TikiEvents {
    data class TriggerEmotion(
        val emotion: EmotionState,
        val customDialogue: String? = null,
        val forceInstant: Boolean = false
    ) : TikiEvents

    data class TriggerDialogue(
        val text: String,
        val emotion: EmotionState? = null
    ) : TikiEvents

    object ResetToDefault : TikiEvents
}
