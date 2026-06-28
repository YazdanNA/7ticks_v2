package com.example.features.tiki.repository

import com.example.features.tiki.model.Dialogue
import com.example.features.tiki.model.EmotionState

class DialogueRepository {

    private val dialoguesByEmotion = mutableMapOf<EmotionState, List<Dialogue>>()

    init {
        // No hardcoded dialogues. Retrieved exclusively from Dialogue Library (JSON).
    }

    fun addDialogues(emotion: EmotionState, textList: List<String>) {
        val current = dialoguesByEmotion[emotion].orEmpty().toMutableList()
        textList.forEachIndexed { index, text ->
            current.add(Dialogue("${emotion.name}_$index", text, emotion))
        }
        dialoguesByEmotion[emotion] = current
    }

    fun getDialogueForEmotion(emotion: EmotionState, lastDialogueText: String?): String {
        val candidates = dialoguesByEmotion[emotion].orEmpty()
        if (candidates.isEmpty()) return "Ready!"
        if (candidates.size == 1) return candidates.first().text

        val filtered = if (lastDialogueText != null) {
            candidates.filter { it.text != lastDialogueText }
        } else {
            candidates
        }

        val finalCandidates = if (filtered.isEmpty()) candidates else filtered
        return finalCandidates.random().text
    }
}
