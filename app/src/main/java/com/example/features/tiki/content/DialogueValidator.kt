package com.example.features.tiki.content

import com.example.features.tiki.dialogue.DialogueCategory
import com.example.features.tiki.model.EmotionState

data class ValidationError(
    val type: String,
    val description: String,
    val dialogueId: String? = null
)

class DialogueValidator {

    fun validate(library: DialogueLibrary): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val ids = mutableSetOf<String>()

        for (dialogue in library.dialogues) {
            val id = dialogue.id
            
            // 1. Duplicate IDs
            if (ids.contains(id)) {
                errors.add(ValidationError("DUPLICATE_ID", "Duplicate dialogue ID found: '$id'", id))
            } else {
                ids.add(id)
            }

            // 2. Empty Text
            if (dialogue.text.trim().isEmpty()) {
                errors.add(ValidationError("EMPTY_TEXT", "Dialogue text is empty", id))
            }

            // 3. Invalid Priorities
            if (dialogue.priority < 0) {
                errors.add(ValidationError("INVALID_PRIORITY", "Priority cannot be negative: ${dialogue.priority}", id))
            }

            // 4. Broken Tags
            for (tag in dialogue.tags) {
                if (tag.trim().isEmpty()) {
                    errors.add(ValidationError("BROKEN_TAG", "Dialogue contains empty/blank tag", id))
                }
            }

            // 5. Invalid Metadata: Verify Category
            val validCategories = setOf(
                "Greeting", "Encouragement", "Celebration", "Failure", "Recovery", 
                "Thinking", "LongThinking", "Again", "AgainStreak", "Easy", 
                "EasyStreak", "Good", "Hard", "HalfSession", "SessionComplete", 
                "Motivation", "Reminder", "Idle", "Silence"
            )
            if (dialogue.category !in validCategories) {
                errors.add(ValidationError("INVALID_CATEGORY", "Category '${dialogue.category}' is invalid", id))
            }

            // 6. Invalid Metadata: Verify Emotion State Name
            val validEmotions = setOf(
                "HAPPY", "SAD", "CURIOUS", "POKER", "THINKING", "SLEEPY"
            )
            if (dialogue.emotion.uppercase() !in validEmotions) {
                errors.add(ValidationError("INVALID_EMOTION", "Emotion '${dialogue.emotion}' is invalid", id))
            }
        }

        return errors
    }

    fun findMissingTranslations(baseLibrary: DialogueLibrary, targetLibrary: DialogueLibrary): List<ValidationError> {
        val baseIds = baseLibrary.dialogues.map { it.id }.toSet()
        val targetIds = targetLibrary.dialogues.map { it.id }.toSet()
        
        val missingIds = baseIds - targetIds
        return missingIds.map { id ->
            ValidationError(
                type = "MISSING_TRANSLATION",
                description = "Dialogue ID '$id' is in base library but missing in translation library for '${targetLibrary.language}'",
                dialogueId = id
            )
        }
    }
}
