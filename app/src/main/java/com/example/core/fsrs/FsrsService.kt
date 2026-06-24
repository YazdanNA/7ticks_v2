package com.example.core.fsrs

import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * FsrsService implements the official Spaced Repetition algorithm FSRS v4.5
 * Reference: https://github.com/open-spaced-repetition/fsrs4anki
 */
@Singleton
class FsrsService @Inject constructor() {

    // FSRS v4.5 Default Parameters (17 Weights)
    private val w = doubleArrayOf(
        0.4025, 0.4615, 0.957, 1.5407, 15.391, 5.8933, 0.5734, 0.0722,
        1.7344, 0.1398, 0.9667, 2.0557, 0.1153, 0.2048, 1.403, 0.37, 1.9547
    )

    // Default target retrievability (desired retention probability)
    private val targetRetention = 0.9

    fun calculateNextReview(
        card: FsrsCardModel,
        rating: ReviewRatingModel,
        currentTimeMs: Long
    ): FsrsCardModel {
        val g = rating.value // 1 = AGAIN, 2 = HARD, 3 = GOOD, 4 = EASY
        val reps = card.reps
        val state = card.state // 0 = New, 1 = Learning, 2 = Review, 3 = Relearning

        val nextReps = reps + 1
        var nextLapses = card.lapses
        if (rating == ReviewRatingModel.AGAIN) {
            nextLapses += 1
        }

        // State Transitions
        val nextState = when (state) {
            0 -> if (rating == ReviewRatingModel.AGAIN) 1 else 2 // New -> Learning or Review
            1, 3 -> if (rating == ReviewRatingModel.AGAIN) state else 2 // Learning/Relearning -> remain or Review
            2 -> if (rating == ReviewRatingModel.AGAIN) 3 else 2 // Review -> Relearning or remain Review
            else -> 2
        }

        val nextStability: Double
        val nextDifficulty: Double

        if (reps == 0 || card.stability == 0.0) {
            // Initial Review State (First Exposure)
            nextStability = w[g - 1]
            nextDifficulty = (w[4] - (g - 3) * w[5]).coerceIn(1.0, 10.0)
        } else {
            // Subsequent Review State (Actual Spaced Repetition)
            val lastReviewedTime = card.lastReviewed?.time ?: (currentTimeMs - card.scheduledDays * 24L * 60 * 60 * 1000)
            val elapsedDays = ((currentTimeMs - lastReviewedTime).toDouble() / (24.0 * 60.0 * 60.0 * 1000.0)).coerceAtLeast(1.0)

            // Calculate Retrievability (probability of recall)
            val r = (1.0 + elapsedDays / (9.0 * card.stability)).pow(-1.0)

            // Update Difficulty
            val d0 = w[4] // baseline initial difficulty
            val deltaD = -w[6] * (g - 3)
            val baseD = card.difficulty + deltaD
            // Apply Mean Reversion to baseline
            val nextDUnclamped = w[7] * d0 + (1.0 - w[7]) * baseD
            nextDifficulty = nextDUnclamped.coerceIn(1.0, 10.0)

            // Update Stability
            if (rating == ReviewRatingModel.AGAIN) {
                // Forgotten review formula (failure)
                val forgetS = w[11] * nextDifficulty.pow(-w[12]) *
                        ((card.stability + 1.0).pow(w[13]) - 1.0) *
                        exp(w[14] * (1.0 - r))
                nextStability = forgetS.coerceIn(0.1, 36500.0)
            } else {
                // Successful review formula (passing)
                val factor = when (rating) {
                    ReviewRatingModel.HARD -> w[15]
                    ReviewRatingModel.GOOD -> 1.0
                    ReviewRatingModel.EASY -> w[16]
                    else -> 1.0
                }
                val passS = card.stability * (1.0 + exp(w[8]) * (11.0 - nextDifficulty) *
                        card.stability.pow(-w[9]) *
                        (exp(w[10] * (1.0 - r)) - 1.0) * factor)
                nextStability = passS.coerceIn(0.1, 36500.0)
            }
        }

        // Calculate next review interval in days using target retention formula
        // Interval I = 9 * S * (1 / R - 1)
        val intervalDaysDouble = 9.0 * nextStability * (1.0 / targetRetention - 1.0)
        val nextScheduledDays = intervalDaysDouble.roundToInt().coerceAtLeast(1)

        val nextDueDateMs = currentTimeMs + (nextScheduledDays * 24L * 60 * 60 * 1000)

        return card.copy(
            stability = nextStability,
            difficulty = nextDifficulty,
            elapsedDays = if (reps == 0) 0 else ((currentTimeMs - (card.lastReviewed?.time ?: currentTimeMs)) / (24L * 60 * 60 * 1000)).toInt().coerceAtLeast(0),
            scheduledDays = nextScheduledDays,
            reps = nextReps,
            lapses = nextLapses,
            state = nextState,
            lastReviewed = Date(currentTimeMs),
            dueDate = Date(nextDueDateMs)
        )
    }
}
