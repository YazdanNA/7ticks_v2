package com.example.features.tiki.api

import com.example.features.tiki.engine.TikiEngine
import com.example.features.tiki.model.EmotionState
import kotlinx.coroutines.flow.StateFlow

class TikiController(
    private val tikiEngine: TikiEngine = TikiEngine.getInstance()
) {
    val state: StateFlow<TikiState> = tikiEngine.emotionEngine.state

    fun onEvent(event: TikiEvents) {
        when (event) {
            is TikiEvents.TriggerEmotion -> {
                tikiEngine.emotionEngine.changeEmotion(
                    emotion = event.emotion,
                    customDialogue = event.customDialogue,
                    forceInstant = event.forceInstant
                )
            }
            is TikiEvents.TriggerDialogue -> {
                tikiEngine.emotionEngine.changeDialogue(
                    text = event.text,
                    emotion = event.emotion
                )
            }
            TikiEvents.ResetToDefault -> {
                tikiEngine.emotionEngine.reset()
            }
        }
    }

    fun setEmotion(emotion: EmotionState, customDialogue: String? = null, forceInstant: Boolean = false) {
        onEvent(TikiEvents.TriggerEmotion(emotion, customDialogue, forceInstant))
    }

    fun setDialogue(text: String, emotion: EmotionState? = null) {
        onEvent(TikiEvents.TriggerDialogue(text, emotion))
    }

    fun reset() {
        onEvent(TikiEvents.ResetToDefault)
    }
}
