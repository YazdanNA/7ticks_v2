package com.example.features.tiki.personality

import com.example.features.tiki.dialogue.DialogueCategory
import com.example.features.tiki.model.EmotionState

data class PersonalityContext(
    val baseDialogueText: String,
    val category: DialogueCategory,
    val emotion: EmotionState,
    val profile: PersonalityProfile = PersonalityProfile()
)
