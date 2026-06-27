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

    private val dialogueManager = CompanionDialogueManager()

    var companionMood by mutableStateOf(CompanionMood.HAPPY)
        private set

    val tikiState: String get() = companionMood.tikiState

    var tikiReactionMessage by mutableStateOf("Tiki is watching! Recall correctly to impress me!")
        private set

    var consecutiveMistakesCount by mutableStateOf(0)
        private set

    var completedCardsCount by mutableStateOf(0)

    var isFlipped by mutableStateOf(false)

    // For 7-circle progress indicator animations
    var temporaryOverlayIndex by mutableStateOf(-1)
        private set
    var temporaryOverlayRating by mutableStateOf("")
        private set

    private var cardStartTime = System.currentTimeMillis()
    private var hasTriggered30s = false
    private var hasTriggered50s = false

    private val cardAgainCount = mutableMapOf<String, Int>()
    private var easyStreakCount = 0

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

        // Reset elapsed timer and spent-time flags when active card changes
        scope.launch {
            queueManager.currentItem.collect { item ->
                if (item != null) {
                    cardStartTime = System.currentTimeMillis()
                    hasTriggered30s = false
                    hasTriggered50s = false
                }
            }
        }

        // Active card spent duration monitor loop
        scope.launch {
            while (true) {
                delay(1000)
                val currentItemValue = queueManager.currentItem.value
                if (currentItemValue != null && !isSessionCompleted) {
                    val elapsedSec = (System.currentTimeMillis() - cardStartTime) / 1000
                    if (elapsedSec >= 50 && !hasTriggered50s) {
                        hasTriggered50s = true
                        triggerEvent(CompanionEvent.Spent50Sec)
                    } else if (elapsedSec >= 30 && !hasTriggered30s) {
                        hasTriggered30s = true
                        triggerEvent(CompanionEvent.Spent30Sec)
                    }
                }
            }
        }
    }

    /**
     * Updates companion mood and reaction message based on a given CompanionEvent.
     */
    fun triggerEvent(event: CompanionEvent) {
        companionMood = dialogueManager.resolveMood(event)
        tikiReactionMessage = dialogueManager.generateMessage(event)
    }

    /**
     * Submit rating with custom database persist callback.
     * Instantly proceeds to animate and transition cards, running database saves in the background.
     */
    fun submitRating(
        rating: ReviewRatingModel,
        currentCircleIndex: Int,
        xpAmount: Int,
        promotedToBox7: Boolean = false,
        onSaveDb: suspend () -> Unit
    ) {
        scope.launch {
            totalAnswers++

            // 1. Process reaction state & triggers
            val isSuccess = rating == ReviewRatingModel.GOOD || rating == ReviewRatingModel.EASY
            if (isSuccess) {
                correctAnswers++
                onCorrectHook()
                streakCount++
                consecutiveMistakesCount = 0
            } else {
                onWrongHook()
                streakCount = 0
                if (rating == ReviewRatingModel.AGAIN) {
                    consecutiveMistakesCount++
                }
            }

            xpEarned += xpAmount

            // Resolve proper companion event and trigger dialogue + state update
            val currentItemId = queueManager.currentItem.value?.id ?: ""
            val event = when {
                promotedToBox7 -> CompanionEvent.FirstBox7
                rating == ReviewRatingModel.EASY -> {
                    easyStreakCount++
                    when {
                        easyStreakCount >= 5 -> CompanionEvent.FiveEasyStreak
                        easyStreakCount >= 3 -> CompanionEvent.ThreeEasyStreak
                        else -> CompanionEvent.Easy
                    }
                }
                rating == ReviewRatingModel.GOOD -> {
                    easyStreakCount = 0
                    CompanionEvent.Good
                }
                rating == ReviewRatingModel.HARD -> {
                    easyStreakCount = 0
                    CompanionEvent.Hard
                }
                else -> { // AGAIN
                    easyStreakCount = 0
                    val count = cardAgainCount.getOrDefault(currentItemId, 0) + 1
                    cardAgainCount[currentItemId] = count
                    if (count > 1) CompanionEvent.AgainMultiple else CompanionEvent.AgainFirst
                }
            }
            triggerEvent(event)

            // 2. Circle animation fill overlay (only for Hard/Good/Easy, NOT for Again)
            if (rating != ReviewRatingModel.AGAIN) {
                val colorStr = when (rating) {
                    ReviewRatingModel.EASY -> "Green"
                    ReviewRatingModel.GOOD -> "Blue"
                    ReviewRatingModel.HARD -> "Yellow"
                    else -> ""
                }
                temporaryOverlayIndex = currentCircleIndex
                temporaryOverlayRating = colorStr
                completedCardsCount++
            } else {
                queueManager.postponeCurrentCard()
            }

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
