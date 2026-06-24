package com.example.core.fsrs

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Date

class FsrsServiceTest {

    private val fsrsService = FsrsService()

    @Test
    fun testInitialReview_GoodRating() {
        val newCard = FsrsCardModel(
            wordId = 101,
            word = "apple"
        )
        val rating = ReviewRatingModel.GOOD
        val currentTime = System.currentTimeMillis()

        val updatedCard = fsrsService.calculateNextReview(newCard, rating, currentTime)

        // For initial review of GOOD:
        // Stability should equal w[2] = 0.957
        assertEquals(0.957, updatedCard.stability, 0.0001)

        // Difficulty should equal w[4] - (3 - 3) * w[5] = w[4] = 15.391 clamped to [1.0, 10.0] -> 10.0
        assertEquals(10.0, updatedCard.difficulty, 0.0001)

        // State transition: New (0) with Good rating -> Review (2)
        assertEquals(2, updatedCard.state)

        // Repetitions should increment
        assertEquals(1, updatedCard.reps)
        assertEquals(0, updatedCard.lapses)

        // Scheduled days should be calculated correctly
        // With S = 0.957 and TargetRetention = 0.9, Interval is round(9 * S * (1/0.9 - 1)) = round(0.957) = 1
        assertEquals(1, updatedCard.scheduledDays)
        assertTrue(updatedCard.dueDate.time > currentTime)
    }

    @Test
    fun testInitialReview_AgainRating() {
        val newCard = FsrsCardModel(
            wordId = 102,
            word = "banana"
        )
        val rating = ReviewRatingModel.AGAIN
        val currentTime = System.currentTimeMillis()

        val updatedCard = fsrsService.calculateNextReview(newCard, rating, currentTime)

        // For initial review of AGAIN:
        // Stability should equal w[0] = 0.4025
        assertEquals(0.4025, updatedCard.stability, 0.0001)

        // Difficulty should equal w[4] - (1 - 3) * w[5] = w[4] + 2 * w[5] = 15.391 + 2 * 5.8933 = 27.1776 clamped to [1.0, 10.0] -> 10.0
        assertEquals(10.0, updatedCard.difficulty, 0.0001)

        // State transition: New (0) with Again rating -> Learning (1)
        assertEquals(1, updatedCard.state)

        // Repetitions and lapses should increment
        assertEquals(1, updatedCard.reps)
        assertEquals(1, updatedCard.lapses)
    }

    @Test
    fun testSubsequentReview_GoodPassing() {
        val currentTime = System.currentTimeMillis()
        val oneDayAgo = currentTime - (24L * 60 * 60 * 1000)

        // A card that was reviewed once and had GOOD rating
        val card = FsrsCardModel(
            wordId = 103,
            word = "orange",
            stability = 0.957,
            difficulty = 10.0,
            elapsedDays = 1,
            scheduledDays = 1,
            reps = 1,
            lapses = 0,
            state = 2,
            lastReviewed = Date(oneDayAgo)
        )

        val updatedCard = fsrsService.calculateNextReview(card, ReviewRatingModel.GOOD, currentTime)

        // After subsequent successful review, repetitions should be 2
        assertEquals(2, updatedCard.reps)
        assertEquals(0, updatedCard.lapses)
        assertEquals(2, updatedCard.state) // Remains in Review state

        // Stability should increase (passS > card.stability)
        assertTrue("Next stability ${updatedCard.stability} should be greater than previous stability 0.957",
            updatedCard.stability > card.stability)

        // Scheduled interval should be at least 1 day
        assertTrue(updatedCard.scheduledDays >= 1)
    }

    @Test
    fun testSubsequentReview_AgainFailure() {
        val currentTime = System.currentTimeMillis()
        val twoDaysAgo = currentTime - (2 * 24L * 60 * 60 * 1000)

        val card = FsrsCardModel(
            wordId = 104,
            word = "cherry",
            stability = 3.0,
            difficulty = 5.0,
            elapsedDays = 2,
            scheduledDays = 3,
            reps = 2,
            lapses = 0,
            state = 2,
            lastReviewed = Date(twoDaysAgo)
        )

        val updatedCard = fsrsService.calculateNextReview(card, ReviewRatingModel.AGAIN, currentTime)

        // After failure, repetitions and lapses should increment
        assertEquals(3, updatedCard.reps)
        assertEquals(1, updatedCard.lapses)

        // State transition: Review (2) with Again -> Relearning (3)
        assertEquals(3, updatedCard.state)

        // Stability should drop significantly
        assertTrue("Next stability ${updatedCard.stability} should be less than previous stability 3.0",
            updatedCard.stability < card.stability)

        // Scheduled days should be reset to a short interval
        assertEquals(1, updatedCard.scheduledDays)
    }
}
