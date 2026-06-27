package com.example.features.tiki.engine

import com.example.features.tiki.api.TikiState
import com.example.features.tiki.model.EmotionState
import com.example.features.tiki.repository.DialogueRepository
import com.example.features.tiki.repository.EmotionAssetRepository
import kotlinx.coroutines.flow.StateFlow

class EmotionEngine(
    private val dialogueRepository: DialogueRepository,
    private val assetRepository: EmotionAssetRepository,
    private val transitionManager: EmotionTransitionManager
) {
    val state: StateFlow<TikiState> = transitionManager.state

    private var lastDialogueText: String? = null

    fun changeEmotion(
        emotion: EmotionState,
        customDialogue: String? = null,
        forceInstant: Boolean = false
    ) {
        val asset = assetRepository.resolve(emotion)
        val dialogue = customDialogue ?: dialogueRepository.getDialogueForEmotion(emotion, lastDialogueText)
        lastDialogueText = dialogue
        transitionManager.transitionTo(emotion, asset, dialogue, forceInstant)
    }

    fun changeDialogue(text: String, emotion: EmotionState? = null) {
        lastDialogueText = text
        val asset = emotion?.let { assetRepository.resolve(it) }
        transitionManager.updateDialogueOnly(text, emotion, asset)
    }

    fun reset() {
        lastDialogueText = null
        transitionManager.reset()
    }
}
