package com.example.core.ui.components.flashcard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.core.ui.components.UniversalFlashcard
import com.example.core.learning.FlashcardAnimator

/**
 * The standard, reusable SevenTicks Flashcard widget.
 * Wraps the visual UniversalFlashcard into a FlashcardAnimator to provide
 * highly performant, sequential slide-left/slide-right transitions.
 */
@Composable
fun FlashCardWidget(
    state: FlashCardState,
    onFlip: () -> Unit,
    modifier: Modifier = Modifier,
    onAgainClick: (() -> Unit)? = null,
    onHardClick: (() -> Unit)? = null,
    onGoodClick: (() -> Unit)? = null,
    onEasyClick: (() -> Unit)? = null,
    againSubtext: String = "<1m",
    hardSubtext: String = "<10m",
    goodSubtext: String = "1d",
    easySubtext: String = "4d",
    onMoreDetailsClick: () -> Unit = {},
    onPronounceClick: (text: String, isMale: Boolean) -> Unit = { _, _ -> }
) {
    FlashcardAnimator(
        data = state.data,
        isFlipped = state.isFlipped,
        modifier = modifier
    ) { animatedData, animatedFlipped ->
        UniversalFlashcard(
            data = animatedData,
            isFlipped = animatedFlipped,
            onFlip = onFlip,
            circleStates = state.circleStates,
            onAgainClick = onAgainClick,
            onHardClick = onHardClick,
            onGoodClick = onGoodClick,
            onEasyClick = onEasyClick,
            againSubtext = againSubtext,
            hardSubtext = hardSubtext,
            goodSubtext = goodSubtext,
            easySubtext = easySubtext,
            onMoreDetailsClick = onMoreDetailsClick,
            onPronounceClick = onPronounceClick,
            modifier = Modifier.fillMaxSize()
        )
    }
}
