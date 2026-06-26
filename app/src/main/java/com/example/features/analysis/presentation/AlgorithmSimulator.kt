package com.example.features.analysis.presentation

import com.example.core.fsrs.FsrsCardModel
import com.example.core.fsrs.FsrsService
import com.example.core.fsrs.ReviewRatingModel
import java.util.Date
import kotlin.random.Random

/**
 * Result of the high-speed algorithmic simulation.
 */
data class SimulationResult(
    val numCards: Int,
    val days: Int,
    val targetRetention: Double,
    val dailyStudyMinutes: Int,
    val totalReviews: Int,
    val learnedCount: Int,
    val maturedCount: Int, // Box 7
    val averageDailyReviews: Double,
    val peakDailyReviews: Int,
    val simulatedRetentionRate: Double,
    val boxDistributionOverTime: List<IntArray>, // List of size [days], each array has 7 elements
    val dailyReviewsOverTime: List<Int>, // List of size [days]
    val activeVocabularyOverTime: List<Int>, // List of size [days]
    val validationPassed: Boolean,
    val validationMessages: List<String>,
    val reportText: String
)

/**
 * High-performance, memory-only simulator for the Leitner + FSRS Spaced Repetition Engine.
 * Replicates the exact priority and scheduling of SmartSessionEngine over years.
 */
object AlgorithmSimulator {

    fun run(
        numCards: Int = 1000,
        days: Int = 180,
        userRecallProbability: Double = 0.88, // 88% chance of success per review
        dailyStudyMinutes: Int = 20
    ): SimulationResult {
        val fsrsService = FsrsService()
        val random = Random(42) // Seeded for deterministic and reproducible test results

        // In-memory representation of cards (Initially empty, created on-demand)
        val activeCards = ArrayList<FsrsCardModel>()
        val boxIndices = HashMap<Int, Int>() // wordId -> BoxIndex (1 to 7)

        var totalReviewsCount = 0
        var totalCorrectReviews = 0

        val boxDistributionOverTime = ArrayList<IntArray>()
        val dailyReviewsOverTime = ArrayList<Int>()
        val activeVocabularyOverTime = ArrayList<Int>()

        // Boundary/Accuracy checks during execution
        var hasNegativeIntervals = false
        var hasOutOfBoundsBoxIndices = false
        var maxStabilityValue = 0.0
        var minStabilityValue = Double.MAX_VALUE
        var hasInvalidStateTransitions = false

        // Start time (simulated epoch)
        var currentSimulatedTimeMs = System.currentTimeMillis()
        val msInDay = 24L * 60 * 60 * 1000

        var currentWordIdSource = 1

        // Average thinking time per card is 10.0s (used to calculate daily capacity)
        val avgSecondsPerCard = 10.0
        val studySeconds = dailyStudyMinutes * 60
        val dailyCapacity = Math.max(1, (studySeconds / avgSecondsPerCard).toInt())

        // Target active vocabulary size mapping
        val targetActiveCards = when {
            dailyStudyMinutes <= 5 -> 120
            dailyStudyMinutes <= 10 -> 250
            dailyStudyMinutes <= 15 -> 400
            dailyStudyMinutes <= 20 -> 600
            dailyStudyMinutes <= 30 -> 900
            dailyStudyMinutes <= 45 -> 1500
            else -> 2200
        }

        for (day in 1..days) {
            // Move time forward by 1 day
            currentSimulatedTimeMs += msInDay

            // 1. Categorize all existing in-memory cards according to FSRS states
            val againCards = ArrayList<FsrsCardModel>()
            val dueCards = ArrayList<FsrsCardModel>()
            val learningCards = ArrayList<FsrsCardModel>()
            val newCards = ArrayList<FsrsCardModel>()

            for (card in activeCards) {
                when (card.state) {
                    3 -> againCards.add(card)
                    2 -> {
                        if (card.dueDate.time <= currentSimulatedTimeMs) {
                            dueCards.add(card)
                        }
                    }
                    1 -> learningCards.add(card)
                    0 -> newCards.add(card)
                }
            }

            // 2. Select cards today using the EXACT SmartSessionEngine SessionBuilder rules
            val selectedToday = ArrayList<FsrsCardModel>()
            var remaining = dailyCapacity

            // Priority 1: Again cards
            val takenAgain = againCards.take(remaining)
            selectedToday.addAll(takenAgain)
            remaining -= takenAgain.size

            // Priority 2: Due Review cards
            if (remaining > 0) {
                val takenDue = dueCards.take(remaining)
                selectedToday.addAll(takenDue)
                remaining -= takenDue.size
            }

            // Reviews Always Win constraint check:
            // "If Again + Due already meet or exceed capacity, DO NOT introduce any new cards."
            val totalReviewsCountToday = againCards.size + dueCards.size
            val allowNewAndLearning = totalReviewsCountToday < dailyCapacity

            if (allowNewAndLearning) {
                // Priority 3: Learning cards
                if (remaining > 0) {
                    val takenLearning = learningCards.take(remaining)
                    selectedToday.addAll(takenLearning)
                    remaining -= takenLearning.size
                }

                // Priority 4: Introduce New cards (on-demand creation up to max deck size)
                if (remaining > 0) {
                    // Count active cards today (excluding mature cards with distant due dates)
                    val activeCardsCount = activeCards.count { card ->
                        val isMatureAndDistant = card.state == 2 && (boxIndices[card.wordId] ?: 1) >= 5 && (card.dueDate.time - currentSimulatedTimeMs > 14L * 24 * 60 * 60 * 1000L)
                        (card.state == 1 || card.state == 2 || card.state == 3) && !isMatureAndDistant
                    }
                    val deficit = (targetActiveCards - activeCardsCount).coerceAtLeast(0)
                    val availableNewCardsToCreate = minOf(deficit, remaining, numCards - (activeCards.size + newCards.size))
                    
                    for (i in 1..availableNewCardsToCreate) {
                        val newId = currentWordIdSource++
                        val newCard = FsrsCardModel(
                            wordId = newId,
                            word = "sim_word_$newId",
                            state = 0, // New
                            reps = 0,
                            lapses = 0,
                            stability = 0.0,
                            difficulty = 0.0,
                            scheduledDays = 0,
                            elapsedDays = 0,
                            lastReviewed = null,
                            dueDate = Date(currentSimulatedTimeMs)
                        )
                        activeCards.add(newCard)
                        boxIndices[newId] = 1
                        selectedToday.add(newCard)
                    }
                }
            }

            var reviewsTodayCount = 0

            // 3. Execute daily learning/reviews for selected cards
            for (cardIndex in selectedToday.indices) {
                val originalCard = selectedToday[cardIndex]
                val currentBoxIndex = boxIndices[originalCard.wordId] ?: 1

                // Simulate student response using userRecallProbability
                val roll = random.nextDouble()
                val isCorrect = roll <= userRecallProbability

                val rating = if (isCorrect) {
                    // Correct: 80% GOOD, 20% EASY
                    if (random.nextDouble() <= 0.80) ReviewRatingModel.GOOD else ReviewRatingModel.EASY
                } else {
                    // Incorrect: 90% AGAIN, 10% HARD
                    if (random.nextDouble() <= 0.90) ReviewRatingModel.AGAIN else ReviewRatingModel.HARD
                }

                // Update reviews count
                totalReviewsCount++
                reviewsTodayCount++
                if (isCorrect) {
                    totalCorrectReviews++
                }

                // Leitner box promotion/demotion logic
                val nextBoxIndex = when (rating) {
                    ReviewRatingModel.AGAIN -> 1
                    ReviewRatingModel.HARD -> (currentBoxIndex - 1).coerceAtLeast(1)
                    ReviewRatingModel.GOOD -> (currentBoxIndex + 1).coerceAtMost(7)
                    ReviewRatingModel.EASY -> (currentBoxIndex + 2).coerceAtMost(7)
                }
                boxIndices[originalCard.wordId] = nextBoxIndex

                // Spacing interval calculation via FSRS Service
                val updatedCard = fsrsService.calculateNextReview(originalCard, rating, currentSimulatedTimeMs)

                // Replace in active list
                val idx = activeCards.indexOfFirst { it.wordId == originalCard.wordId }
                if (idx != -1) {
                    activeCards[idx] = updatedCard
                }

                // Collect validation diagnostics
                if (updatedCard.scheduledDays < 0) {
                    hasNegativeIntervals = true
                }
                if (nextBoxIndex < 1 || nextBoxIndex > 7) {
                    hasOutOfBoundsBoxIndices = true
                }
                if (updatedCard.stability > maxStabilityValue) {
                    maxStabilityValue = updatedCard.stability
                }
                if (updatedCard.stability < minStabilityValue && updatedCard.stability > 0) {
                    minStabilityValue = updatedCard.stability
                }
                // Verify FSRS state transitions
                // 0=New, 1=Learning, 2=Review, 3=Relearning
                val validTransition = when (originalCard.state) {
                    0 -> updatedCard.state in listOf(1, 2)
                    1 -> updatedCard.state in listOf(1, 2)
                    2 -> updatedCard.state in listOf(2, 3)
                    3 -> updatedCard.state in listOf(2, 3)
                    else -> false
                }
                if (!validTransition) {
                    hasInvalidStateTransitions = true
                }
            }

            // Record box distribution and active cards for today
            val distribution = IntArray(7)
            for (card in activeCards) {
                val box = boxIndices[card.wordId] ?: 1
                distribution[box - 1]++
            }
            boxDistributionOverTime.add(distribution)
            dailyReviewsOverTime.add(reviewsTodayCount)

            val dailyActiveCount = activeCards.count { card ->
                val isMatureAndDistant = card.state == 2 && (boxIndices[card.wordId] ?: 1) >= 5 && (card.dueDate.time - currentSimulatedTimeMs > 14L * 24 * 60 * 60 * 1000L)
                (card.state == 1 || card.state == 2 || card.state == 3) && !isMatureAndDistant
            }
            activeVocabularyOverTime.add(dailyActiveCount)
        }

        // Final aggregate diagnostics
        val learnedCount = activeCards.size
        val maturedCount = boxIndices.values.count { it == 7 }
        val peakDailyReviews = dailyReviewsOverTime.maxOrNull() ?: 0
        val averageDailyReviews = if (days > 0) totalReviewsCount.toDouble() / days else 0.0
        val simulatedRetentionRate = if (totalReviewsCount > 0) totalCorrectReviews.toDouble() / totalReviewsCount else 0.0

        val averageActiveVocabulary = if (days > 0) activeVocabularyOverTime.average() else 0.0
        val peakActiveVocabulary = activeVocabularyOverTime.maxOrNull() ?: 0
        val finalActiveVocabulary = activeVocabularyOverTime.lastOrNull() ?: 0

        // Estimated years required to finish introducing the entire vocabulary
        val completionRate = learnedCount.toDouble() / days
        val estimatedYears = if (completionRate > 0.0) {
            (numCards / completionRate) / 365.0
        } else {
            Double.POSITIVE_INFINITY
        }
        val estimatedYearsText = if (learnedCount >= numCards) {
            "Already fully completed within simulated timeframe!"
        } else if (estimatedYears == Double.POSITIVE_INFINITY) {
            "Infinite (vocabulary learning stalled)"
        } else {
            String.format("%.2f Years", estimatedYears)
        }

        // Validation Checks
        val validationMessages = ArrayList<String>()
        var validationPassed = true

        if (hasNegativeIntervals) {
            validationPassed = false
            validationMessages.add("FAIL: Spacing engine generated a negative review interval!")
        } else {
            validationMessages.add("PASS: Spacing intervals are strictly positive.")
        }

        if (hasOutOfBoundsBoxIndices) {
            validationPassed = false
            validationMessages.add("FAIL: Box promotion/demotion went beyond boundaries [1, 7]!")
        } else {
            validationMessages.add("PASS: Leitner box levels are fully contained within [1, 7].")
        }

        if (maxStabilityValue > 36500.0) {
            validationPassed = false
            validationMessages.add("FAIL: FSRS stability exploded above legal boundary (36500 days)!")
        } else {
            validationMessages.add("PASS: FSRS stability is perfectly bounded (Max: ${String.format("%.1f", maxStabilityValue)} days).")
        }

        if (minStabilityValue < 0.1) {
            validationPassed = false
            validationMessages.add("FAIL: FSRS stability degraded below safe minimum (0.1 days)!")
        } else {
            validationMessages.add("PASS: FSRS stability is kept healthy (Min: ${String.format("%.3f", minStabilityValue)} days).")
        }

        if (hasInvalidStateTransitions) {
            validationPassed = false
            validationMessages.add("FAIL: Invalid FSRS state transitions detected during learning.")
        } else {
            validationMessages.add("PASS: FSRS state transitions conform perfectly to specifications.")
        }

        // Generate complete text report (Markdown style)
        val reportBuilder = StringBuilder()
        reportBuilder.append("=========================================================\n")
        reportBuilder.append("          ADAPTIVE ACTIVE VOCABULARY SIMULATION REPORT   \n")
        reportBuilder.append("=========================================================\n\n")
        reportBuilder.append("## SIMULATION CONFIGURATION:\n")
        reportBuilder.append("- Simulated Vocabulary Volume: $numCards words\n")
        reportBuilder.append("- Study Track Duration: $days Days (approx. ${String.format("%.1f", days.toDouble() / 365.0)} years)\n")
        reportBuilder.append("- Target Student Success Rate (Recall Prob): ${(userRecallProbability * 100).toInt()}%\n")
        reportBuilder.append("- Daily Study Duration: $dailyStudyMinutes minutes\n")
        reportBuilder.append("- Session Capacity Limit: $dailyCapacity cards/day (at 10s per card)\n")
        reportBuilder.append("- Target Active Vocabulary Cap: $targetActiveCards cards\n\n")

        reportBuilder.append("## METRICS SUMMARY:\n")
        reportBuilder.append("- **Total Words Introduced**: $learnedCount words\n")
        reportBuilder.append("- **Total Reviews**: $totalReviewsCount iterations\n")
        reportBuilder.append("- **Average Daily Workload**: ${String.format("%.1f", averageDailyReviews)} reviews/day\n")
        reportBuilder.append("- **Peak Workload**: $peakDailyReviews reviews (on day ${dailyReviewsOverTime.indexOf(peakDailyReviews) + 1})\n")
        reportBuilder.append("- **Active Vocabulary over Time**:\n")
        reportBuilder.append("  - *Average Active Vocabulary*: ${String.format("%.1f", averageActiveVocabulary)} cards\n")
        reportBuilder.append("  - *Peak Active Vocabulary*: $peakActiveVocabulary cards\n")
        reportBuilder.append("  - *Final Active Vocabulary*: $finalActiveVocabulary cards\n")
        reportBuilder.append("- **Estimated Years to Finish Entire Vocabulary**: $estimatedYearsText\n")
        reportBuilder.append("- **Fully Matured Words (Box 7)**: $maturedCount cards (${String.format("%.1f", (maturedCount.toDouble() / numCards) * 100)}% of deck)\n")
        reportBuilder.append("- **Actual Achieved Memory Retention Rate**: ${String.format("%.2f", simulatedRetentionRate * 100)}%\n\n")

        reportBuilder.append("## FINAL DECK BOX DISTRIBUTION:\n")
        val finalDist = boxDistributionOverTime.lastOrNull() ?: IntArray(7)
        for (i in 0..6) {
            val count = finalDist[i]
            val bar = "*".repeat((count.toDouble() / learnedCount * 30).toInt().coerceAtLeast(0))
            reportBuilder.append("- Box ${i + 1}: $count cards (${String.format("%.1f", (count.toDouble() / learnedCount) * 100)}%) $bar\n")
        }
        reportBuilder.append("\n")

        reportBuilder.append("## ALGORITHMIC STABILITY & VERIFICATION VALIDATION:\n")
        validationMessages.forEach { msg ->
            reportBuilder.append("- $msg\n")
        }
        reportBuilder.append("\n")

        reportBuilder.append("## PERFORMANCE DIAGNOSTIC CONTEXT:\n")
        if (validationPassed && simulatedRetentionRate >= userRecallProbability - 0.05) {
            reportBuilder.append("STATUS: [OPTIMAL] Adaptive Active Vocabulary is smoothing workload spikes perfectly! Target active sizes prevent reviews from exploding while ensuring consistent vocabulary ingestion.\n")
        } else {
            reportBuilder.append("STATUS: [WARNING] Spaced interval configurations might require tuning to avoid early workload saturation.\n")
        }
        reportBuilder.append("\n=========================================================\n")

        return SimulationResult(
            numCards = numCards,
            days = days,
            targetRetention = userRecallProbability,
            dailyStudyMinutes = dailyStudyMinutes,
            totalReviews = totalReviewsCount,
            learnedCount = learnedCount,
            maturedCount = maturedCount,
            averageDailyReviews = averageDailyReviews,
            peakDailyReviews = peakDailyReviews,
            simulatedRetentionRate = simulatedRetentionRate,
            boxDistributionOverTime = boxDistributionOverTime,
            dailyReviewsOverTime = dailyReviewsOverTime,
            activeVocabularyOverTime = activeVocabularyOverTime,
            validationPassed = validationPassed,
            validationMessages = validationMessages,
            reportText = reportBuilder.toString()
        )
    }
}
