package com.example.core.ui.components.flashcard

import com.example.core.ui.components.FlashcardData

/**
 * Represents the localized state of a single FlashCard.
 */
data class FlashCardState(
    val data: FlashcardData,
    val isFlipped: Boolean = false,
    val circleStates: List<String>? = null
)
