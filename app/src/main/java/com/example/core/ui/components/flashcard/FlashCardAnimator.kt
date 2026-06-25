package com.example.core.ui.components.flashcard

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import com.example.core.ui.components.FlashcardData
import kotlinx.coroutines.launch

/**
 * Handles the sequential entrance/exit transitions for the flashcard.
 * When data changes, it:
 * 1. Slides the current card left, fades out, and scales down slightly.
 * 2. Swaps content to the new card and resets state.
 * 3. Slides the new card in from the right, fades in, and scales up slightly.
 */
@Composable
fun FlashCardAnimator(
    data: FlashcardData,
    isFlipped: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable (FlashcardData, Boolean) -> Unit
) {
    var currentData by remember { mutableStateOf(data) }
    var currentFlipped by remember { mutableStateOf(isFlipped) }

    val offsetX = remember { Animatable(0f) }
    val fadeAlpha = remember { Animatable(1f) }
    val cardScale = remember { Animatable(1f) }

    // Keep flip state in sync when not transitioning
    LaunchedEffect(isFlipped) {
        currentFlipped = isFlipped
    }

    LaunchedEffect(data) {
        if (data.word != currentData.word) {
            // STEP 3: Current card exits to the left
            val exitJobX = launch {
                offsetX.animateTo(
                    targetValue = -350f, // slide left
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
            val exitJobAlpha = launch {
                fadeAlpha.animateTo(
                    targetValue = 0f, // fade out
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                )
            }
            val exitJobScale = launch {
                cardScale.animateTo(
                    targetValue = 0.88f, // scale down
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
            
            // Wait for exit animation to complete
            exitJobX.join()
            exitJobAlpha.join()
            exitJobScale.join()

            // Swap card content & reset flip state
            currentData = data
            currentFlipped = false

            // Snap new card to start position on the right
            offsetX.snapTo(350f)
            fadeAlpha.snapTo(0f)
            cardScale.snapTo(0.88f)

            // STEP 4: New card enters from the right
            val enterJobX = launch {
                offsetX.animateTo(
                    targetValue = 0f, // slide in
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
            val enterJobAlpha = launch {
                fadeAlpha.animateTo(
                    targetValue = 1f, // fade in
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                )
            }
            val enterJobScale = launch {
                cardScale.animateTo(
                    targetValue = 1f, // scale up
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }

            enterJobX.join()
            enterJobAlpha.join()
            enterJobScale.join()
        } else {
            // Simply update other fields if word didn't change
            currentData = data
        }
    }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value * density
            }
            .alpha(fadeAlpha.value)
            .scale(cardScale.value),
        contentAlignment = Alignment.Center
    ) {
        content(currentData, currentFlipped)
    }
}
