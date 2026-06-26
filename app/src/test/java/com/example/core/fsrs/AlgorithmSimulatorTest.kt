package com.example.core.fsrs

import com.example.features.analysis.presentation.AlgorithmSimulator
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AlgorithmSimulatorTest {

    @Test
    fun testHighSpeedSpacedRepetitionSimulation() {
        val numCards = 500
        val days = 90
        val userRecall = 0.90
        val dailyStudyMinutes = 20

        val startTime = System.nanoTime()
        val result = AlgorithmSimulator.run(
            numCards = numCards,
            days = days,
            userRecallProbability = userRecall,
            dailyStudyMinutes = dailyStudyMinutes
        )
        val endTime = System.nanoTime()
        val durationMs = (endTime - startTime) / 1_000_000.0

        // Output complete report to console during test execution
        println(result.reportText)
        println("Simulation execution speed: ${String.format("%.2f", durationMs)} milliseconds.")

        // Structural and logical assertions
        assertNotNull(result)
        assertTrue(result.validationPassed)
        assertTrue(result.totalReviews > 0)
        assertTrue(result.learnedCount <= numCards)
        assertTrue(result.dailyReviewsOverTime.size == days)
        assertTrue(result.boxDistributionOverTime.size == days)

        // Ensure stability ranges are valid
        assertTrue("Execution duration must be very fast (under 1 second)", durationMs < 1000.0)
    }
}
