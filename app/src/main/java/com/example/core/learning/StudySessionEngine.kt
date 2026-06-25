package com.example.core.learning

import androidx.compose.runtime.*
import com.example.core.fsrs.ReviewRatingModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A highly performant, unified study session engine that coordinates
 * progress, streak counts, user reactions, haptics, and the preloaded queue.
 */
@Stable
class StudySessionEngine(
    val queueManager: SessionQueueManager,
    private val scope: CoroutineScope,
    initialStreak: Int = 0,
    private val onCorrectHook: () -> Unit = {},
    private val onWrongHook: () -> Unit = {},
    private val onSessionFinished: () -> Unit = {}
) {
    var streakCount by mutableStateOf(initialStreak)
        private set

    var xpEarned by mutableStateOf(0)
        private set

    var totalAnswers by mutableStateOf(0)
        private set

    var correctAnswers by mutableStateOf(0)
        private set

    var isSessionCompleted by mutableStateOf(queueManager.isFinished.value)
        private set

    var tikiReactionMessage by mutableStateOf("Tiki is watching! Recall correctly to impress me!")
        private set

    var consecutiveMistakesCount by mutableStateOf(0)
        private set

    var isFlipped by mutableStateOf(false)

    // For 7-circle progress indicator animations
    var temporaryOverlayIndex by mutableStateOf(-1)
        private set
    var temporaryOverlayRating by mutableStateOf("")
        private set

    init {
        // Observe finished state
        scope.launch {
            queueManager.isFinished.collect { finished ->
                if (finished) {
                    isSessionCompleted = true
                    onSessionFinished()
                }
            }
        }
    }

    /**
     * Submit rating with custom database persist callback.
     * Instantly proceeds to animate and transition cards, running database saves in the background.
     */
    fun submitRating(
        rating: ReviewRatingModel,
        currentCircleIndex: Int,
        xpAmount: Int,
        onSaveDb: suspend () -> Unit
    ) {
        scope.launch {
            totalAnswers++
            isFlipped = false // Reset flipped state for the upcoming card

            // 1. Process reaction state & triggers
            val isSuccess = rating == ReviewRatingModel.GOOD || rating == ReviewRatingModel.EASY
            if (isSuccess) {
                correctAnswers++
                onCorrectHook()
                streakCount++
                consecutiveMistakesCount = 0
                tikiReactionMessage = if (streakCount >= 3) {
                    "Incredible $streakCount-word streak! Tiki is excited! 🔥"
                } else {
                    "Awesome! Tiki is super happy! 🎉"
                }
            } else {
                onWrongHook()
                streakCount = 0
                consecutiveMistakesCount++
                tikiReactionMessage = if (consecutiveMistakesCount >= 3) {
                    "Tiki detects some fatigue! 🐾 Keep practicing, you got this!"
                } else {
                    "Ah, no worries! Tiki believes in you. Let's practice!"
                }
            }

            xpEarned += xpAmount

            // 2. Circle animation fill overlay
            val colorStr = when (rating) {
                ReviewRatingModel.EASY -> "Green"
                ReviewRatingModel.GOOD -> "Blue"
                ReviewRatingModel.HARD -> "Yellow"
                ReviewRatingModel.AGAIN -> "Red"
            }
            temporaryOverlayIndex = currentCircleIndex
            temporaryOverlayRating = colorStr

            // 3. Trigger concurrent background saving
            val dbJob = launch {
                try {
                    onSaveDb()
                } catch (e: Exception) {
                    android.util.Log.e("StudySessionEngine", "Failed to save progress in background", e)
                }
            }

            // 4. Pause so user sees the circle fill animation before card exits
            delay(850)

            // Reset overlays before queue promotion
            temporaryOverlayIndex = -1
            temporaryOverlayRating = ""

            // 5. Promote B to Current Card (which triggers the FlashcardAnimator's exit/enter transitions)
            val shifted = queueManager.next()
            if (!shifted) {
                isSessionCompleted = true
                onSessionFinished()
            }

            // Wait for DB write to complete if not yet done
            dbJob.join()
        }
    }
}

@Composable
fun rememberStudySessionEngine(
    queueManager: SessionQueueManager,
    initialStreak: Int = 0,
    onCorrectHook: () -> Unit = {},
    onWrongHook: () -> Unit = {},
    onSessionFinished: () -> Unit = {}
): StudySessionEngine {
    val scope = rememberCoroutineScope()
    return remember(queueManager) {
        StudySessionEngine(
            queueManager = queueManager,
            scope = scope,
            initialStreak = initialStreak,
            onCorrectHook = onCorrectHook,
            onWrongHook = onWrongHook,
            onSessionFinished = onSessionFinished
        )
    }
}
