package com.example.core.learning

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
 * - Slides the current card LEFT, fades out, and scales down slightly. (300ms)
 * - Swaps card content & resets flipped state.
 * - Slides the new card in from the RIGHT, fades in, and scales up. (300ms)
 */
@Composable
fun FlashcardAnimator(
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

    // Synchronize flip state when not mid-transition
    LaunchedEffect(isFlipped) {
        currentFlipped = isFlipped
    }

    LaunchedEffect(data) {
        if (data.word != currentData.word) {
            // Exit animation: Slide LEFT + Fade Out + Scale Down
            val exitJobX = launch {
                offsetX.animateTo(
                    targetValue = -350f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
            val exitJobAlpha = launch {
                fadeAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                )
            }
            val exitJobScale = launch {
                cardScale.animateTo(
                    targetValue = 0.88f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }

            exitJobX.join()
            exitJobAlpha.join()
            exitJobScale.join()

            // Swap card content & reset flip state
            currentData = data
            currentFlipped = false

            // Snap new card to starting position on the right
            offsetX.snapTo(350f)
            fadeAlpha.snapTo(0f)
            cardScale.snapTo(0.88f)

            // Entrance animation: Slide RIGHT to center + Fade In + Scale Up
            val enterJobX = launch {
                offsetX.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
            val enterJobAlpha = launch {
                fadeAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, easing = LinearEasing)
                )
            }
            val enterJobScale = launch {
                cardScale.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }

            enterJobX.join()
            enterJobAlpha.join()
            enterJobScale.join()
        } else {
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
