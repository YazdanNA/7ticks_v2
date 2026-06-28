package com.example.features.tiki.api

import com.example.features.tiki.engine.TikiEngine
import com.example.features.tiki.model.EmotionState
import com.example.features.tiki.context.ContextEvent
import com.example.features.tiki.behavior.BehaviorEvent
import com.example.features.tiki.relationship.RelationshipEvent
import kotlinx.coroutines.flow.StateFlow

class TikiController(
    private val tikiEngine: TikiEngine = TikiEngine.getInstance()
) {
    val state: StateFlow<TikiState> = tikiEngine.emotionEngine.state

    fun triggerPipeline(
        contextEvent: ContextEvent,
        behaviorEvent: BehaviorEvent? = null,
        relationshipEvent: RelationshipEvent? = null
    ) {
        tikiEngine.triggerPipeline(contextEvent, behaviorEvent, relationshipEvent)
    }

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

    companion object {
        @Volatile
        private var INSTANCE: TikiController? = null

        fun getInstance(): TikiController {
            return INSTANCE ?: synchronized(this) {
                val instance = TikiController()
                INSTANCE = instance
                instance
            }
        }
    }
}
