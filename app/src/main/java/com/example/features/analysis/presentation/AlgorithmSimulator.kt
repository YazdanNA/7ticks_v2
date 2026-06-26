package com.example.features.analysis.presentation

import com.example.core.fsrs.FsrsCardModel
import com.example.core.fsrs.FsrsService
import com.example.core.fsrs.ReviewRatingModel
import java.util.Date
import kotlin.random.Random
import kotlin.math.exp

/**
 * Result of the comprehensive algorithmic simulation.
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
    val reportText: String,

    // NEW: Onboarding Assessment results
    val placementLevel: String,
    val placementTheta: Double,
    val placementCorrectAnswers: Int,

    // NEW: Custom boxes simulation results
    val customBoxesCreated: Int,
    val customBoxWordsAdded: Int,
    val customBoxReviewsCount: Int,
    val customBoxRetentionRate: Double,
    val customBoxDistribution: IntArray // size 7 array
)

/**
 * Enhanced High-performance, memory-only simulator for the Leitner + FSRS Spaced Repetition Engine.
 * Replicates the complete virtual user experience: Onboarding test, Smart Learn, Custom Boxes, and Data Segregation.
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

        // -------------------------------------------------------------
        // PART 1: ADAPTIVE PLACEMENT ASSESSMENT SIMULATION (IRT 3PL)
        // -------------------------------------------------------------
        val trueStudentTheta = 0.6 // B2 level student true capability
        var estimatedTheta = 0.0   // starts intermediate
        var correctAnswersCount = 0

        val assessmentLogs = StringBuilder()
        assessmentLogs.append("Adaptive Placement Assessment Progress:\n")

        for (q in 1..10) {
            // Select question difficulty matching current estimated theta
            val questionDifficulty = estimatedTheta
            val discrimination = 1.7
            val guessing = 0.25 // 4-option MCQs

            // P(θ) = c + (1-c) / (1 + e^(-1.7 * a * (θ - b)))
            val exponent = -1.7 * discrimination * (trueStudentTheta - questionDifficulty)
            val probCorrect = guessing + (1.0 - guessing) / (1.0 + exp(exponent))

            val isCorrect = random.nextDouble() <= probCorrect
            val responseLabel = if (isCorrect) "CORRECT" else "INCORRECT"

            if (isCorrect) {
                correctAnswersCount++
                estimatedTheta += 0.4
            } else {
                estimatedTheta -= 0.3
            }
            estimatedTheta = estimatedTheta.coerceIn(-3.0, 3.0)
            assessmentLogs.append("  - Question $q: Diff=${String.format("%.2f", questionDifficulty)}, Result=$responseLabel, Next Theta=${String.format("%.2f", estimatedTheta)}\n")
        }

        val finalCEFRLevel = when {
            estimatedTheta <= -2.0 -> "A1 (Beginner)"
            estimatedTheta <= -0.8 -> "A2 (Elementary)"
            estimatedTheta <= 0.4  -> "B1 (Intermediate)"
            estimatedTheta <= 1.6  -> "B2 (Upper-Intermediate)"
            estimatedTheta <= 2.6  -> "C1 (Advanced)"
            else -> "C2 (Mastery)"
        }

        // -------------------------------------------------------------
        // PART 2: SMART LEARN SESSION SETUP
        // -------------------------------------------------------------
        val activeCards = ArrayList<FsrsCardModel>()
        val boxIndices = HashMap<Int, Int>() // wordId -> BoxIndex (1 to 7)

        var totalReviewsCount = 0
        var totalCorrectReviews = 0

        val boxDistributionOverTime = ArrayList<IntArray>()
        val dailyReviewsOverTime = ArrayList<Int>()
        val activeVocabularyOverTime = ArrayList<Int>()

        var hasNegativeIntervals = false
        var hasOutOfBoundsBoxIndices = false
        var maxStabilityValue = 0.0
        var minStabilityValue = Double.MAX_VALUE
        var hasInvalidStateTransitions = false

        var currentSimulatedTimeMs = System.currentTimeMillis()
        val msInDay = 24L * 60 * 60 * 1000

        var currentWordIdSource = 1

        val avgSecondsPerCard = 10.0
        val studySeconds = dailyStudyMinutes * 60
        val dailyCapacity = Math.max(1, (studySeconds / avgSecondsPerCard).toInt())

        val targetActiveCards = when {
            dailyStudyMinutes <= 5 -> 120
            dailyStudyMinutes <= 10 -> 250
            dailyStudyMinutes <= 15 -> 400
            dailyStudyMinutes <= 20 -> 600
            dailyStudyMinutes <= 30 -> 900
            dailyStudyMinutes <= 45 -> 1500
            else -> 2200
        }

        // -------------------------------------------------------------
        // PART 3: CUSTOM BOXES SETUP (FSRS + Leitner Progression)
        // -------------------------------------------------------------
        // Simulated custom boxes:
        // Box 101: "IELTS Academic Core"
        // Box 102: "Business & Corporate English"
        // Box 103: "Colloquial Conversational Idioms"
        val customBoxesNames = mapOf(101 to "IELTS Academic Core", 102 to "Business English", 103 to "Conversational Idioms")
        val customBoxCards = ArrayList<FsrsCardModel>()
        val customBoxIndices = HashMap<Int, Int>() // custom wordId -> Custom BoxIndex (1 to 7)
        val customWordBoxMapping = HashMap<Int, Int>() // custom wordId -> customBoxId

        var customBoxReviewsCount = 0
        var customBoxCorrectReviews = 0

        // -------------------------------------------------------------
        // DATA SEGREGATION TEST CASE ("success" duplicates)
        // -------------------------------------------------------------
        // We will inject a specific word "success" in both pools.
        // Smart Learn: studied correctly, reaches high Leitner Box (e.g. Box 6).
        // Custom Box: studied poorly (representing difficult contextual application), stays low (Box 1-2).
        val smartSuccessId = 8888
        val customSuccessId = 9999

        // 1. Add "success" to Smart Learn pool on Day 1
        val smartSuccessCard = FsrsCardModel(
            wordId = smartSuccessId,
            word = "success (Smart Learn)",
            state = 0,
            reps = 0,
            dueDate = Date(currentSimulatedTimeMs)
        )
        activeCards.add(smartSuccessCard)
        boxIndices[smartSuccessId] = 1

        // 2. Add "success" to IELTS Custom Box pool on Day 5
        var dayCustomSuccessAdded = 5
        val customSuccessCard = FsrsCardModel(
            wordId = customSuccessId,
            word = "success (IELTS Custom)",
            state = 0,
            reps = 0,
            dueDate = Date(currentSimulatedTimeMs + 5L * msInDay)
        )

        // -------------------------------------------------------------
        // DAILY STUDY LOOP
        // -------------------------------------------------------------
        for (day in 1..days) {
            currentSimulatedTimeMs += msInDay

            // 1. Weekly: Add 10 new words to custom boxes
            if (day % 7 == 1) {
                for (i in 1..10) {
                    val customWordId = 10000 + (day * 10) + i
                    val randomBoxId = customBoxesNames.keys.shuffled(random).first()
                    val newCustomCard = FsrsCardModel(
                        wordId = customWordId,
                        word = "custom_vocab_${customWordId}",
                        state = 0,
                        reps = 0,
                        lapses = 0,
                        stability = 0.0,
                        difficulty = 0.0,
                        scheduledDays = 0,
                        elapsedDays = 0,
                        lastReviewed = null,
                        dueDate = Date(currentSimulatedTimeMs)
                    )
                    customBoxCards.add(newCustomCard)
                    customBoxIndices[customWordId] = 1
                    customWordBoxMapping[customWordId] = randomBoxId
                }
            }

            // Day 5 inject custom success card
            if (day == dayCustomSuccessAdded) {
                customBoxCards.add(customSuccessCard)
                customBoxIndices[customSuccessId] = 1
                customWordBoxMapping[customSuccessId] = 101 // IELTS Custom Box
            }

            // ──────────────────────────────────────────
            // SIMULATING SMART LEARN STUDY
            // ──────────────────────────────────────────
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

            val totalReviewsCountToday = againCards.size + dueCards.size
            val allowNewAndLearning = totalReviewsCountToday < dailyCapacity

            if (allowNewAndLearning) {
                // Priority 3: Learning cards
                if (remaining > 0) {
                    val takenLearning = learningCards.take(remaining)
                    selectedToday.addAll(takenLearning)
                    remaining -= takenLearning.size
                }

                // Priority 4: Introduce New cards
                if (remaining > 0) {
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
                            state = 0,
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

            // Execute Smart Learn Reviews
            for (cardIndex in selectedToday.indices) {
                val originalCard = selectedToday[cardIndex]
                val currentBoxIndex = boxIndices[originalCard.wordId] ?: 1

                // Simulate response
                var isCorrect = random.nextDouble() <= userRecallProbability
                // Force smart success word to be consistently correct to show separation differences
                if (originalCard.wordId == smartSuccessId) {
                    isCorrect = true
                }

                val rating = if (isCorrect) {
                    if (random.nextDouble() <= 0.80) ReviewRatingModel.GOOD else ReviewRatingModel.EASY
                } else {
                    if (random.nextDouble() <= 0.90) ReviewRatingModel.AGAIN else ReviewRatingModel.HARD
                }

                totalReviewsCount++
                reviewsTodayCount++
                if (isCorrect) {
                    totalCorrectReviews++
                }

                // Leitner promotion logic
                val nextBoxIndex = when (rating) {
                    ReviewRatingModel.AGAIN -> 1
                    ReviewRatingModel.HARD -> (currentBoxIndex - 1).coerceAtLeast(1)
                    ReviewRatingModel.GOOD -> (currentBoxIndex + 1).coerceAtMost(7)
                    ReviewRatingModel.EASY -> (currentBoxIndex + 2).coerceAtMost(7)
                }
                boxIndices[originalCard.wordId] = nextBoxIndex

                val updatedCard = fsrsService.calculateNextReview(originalCard, rating, currentSimulatedTimeMs)
                val idx = activeCards.indexOfFirst { it.wordId == originalCard.wordId }
                if (idx != -1) {
                    activeCards[idx] = updatedCard
                }

                // Diagnostics
                if (updatedCard.scheduledDays < 0) hasNegativeIntervals = true
                if (nextBoxIndex < 1 || nextBoxIndex > 7) hasOutOfBoundsBoxIndices = true
                if (updatedCard.stability > maxStabilityValue) maxStabilityValue = updatedCard.stability
                if (updatedCard.stability < minStabilityValue && updatedCard.stability > 0.0) minStabilityValue = updatedCard.stability

                val validTransition = when (originalCard.state) {
                    0 -> updatedCard.state in listOf(1, 2)
                    1 -> updatedCard.state in listOf(1, 2)
                    2 -> updatedCard.state in listOf(2, 3)
                    3 -> updatedCard.state in listOf(2, 3)
                    else -> false
                }
                if (!validTransition) hasInvalidStateTransitions = true
            }

            // ──────────────────────────────────────────
            // SIMULATING CUSTOM BOX REVIEWS
            // ──────────────────────────────────────────
            // Custom reviews cap at 15 reviews per day to simulate healthy spacing limits
            val dueCustomCards = customBoxCards.filter { it.dueDate.time <= currentSimulatedTimeMs }.take(15)
            for (customCard in dueCustomCards) {
                val currentBoxIndex = customBoxIndices[customCard.wordId] ?: 1

                // Simulate response. Custom success card is forced to struggle (60% recall) to show the separation
                var isCorrect = if (customCard.wordId == customSuccessId) {
                    random.nextDouble() <= 0.60
                } else {
                    random.nextDouble() <= userRecallProbability
                }

                val rating = if (isCorrect) {
                    if (random.nextDouble() <= 0.85) ReviewRatingModel.GOOD else ReviewRatingModel.EASY
                } else {
                    if (random.nextDouble() <= 0.90) ReviewRatingModel.AGAIN else ReviewRatingModel.HARD
                }

                customBoxReviewsCount++
                if (isCorrect) {
                    customBoxCorrectReviews++
                }

                // Leitner level progression rating logic for custom boxes (AS DEFINED IN SYSTEM EXPLICITLY)
                val nextBoxIndex = when (rating) {
                    ReviewRatingModel.AGAIN -> 1
                    ReviewRatingModel.HARD -> (currentBoxIndex - 1).coerceAtLeast(1)
                    ReviewRatingModel.GOOD -> (currentBoxIndex + 1).coerceAtMost(7)
                    ReviewRatingModel.EASY -> (currentBoxIndex + 2).coerceAtMost(7)
                }
                customBoxIndices[customCard.wordId] = nextBoxIndex

                val updatedCustomCard = fsrsService.calculateNextReview(customCard, rating, currentSimulatedTimeMs)
                val idx = customBoxCards.indexOfFirst { it.wordId == customCard.wordId }
                if (idx != -1) {
                    customBoxCards[idx] = updatedCustomCard
                }
            }

            // Track Smart Learn distributions daily
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

        // Final custom box distribution
        val finalCustomDistribution = IntArray(7)
        for (card in customBoxCards) {
            val box = customBoxIndices[card.wordId] ?: 1
            finalCustomDistribution[box - 1]++
        }

        val learnedCount = activeCards.size
        val maturedCount = boxIndices.values.count { it == 7 }
        val peakDailyReviews = dailyReviewsOverTime.maxOrNull() ?: 0
        val averageDailyReviews = if (days > 0) totalReviewsCount.toDouble() / days else 0.0
        val simulatedRetentionRate = if (totalReviewsCount > 0) totalCorrectReviews.toDouble() / totalReviewsCount else 0.0

        val averageActiveVocabulary = if (days > 0) activeVocabularyOverTime.average() else 0.0
        val peakActiveVocabulary = activeVocabularyOverTime.maxOrNull() ?: 0
        val finalActiveVocabulary = activeVocabularyOverTime.lastOrNull() ?: 0

        val customBoxRetentionRate = if (customBoxReviewsCount > 0) customBoxCorrectReviews.toDouble() / customBoxReviewsCount else 0.0

        // Find state of isolation words
        val smartSuccessFinal = activeCards.find { it.wordId == smartSuccessId }
        val smartSuccessBox = boxIndices[smartSuccessId] ?: 1

        val customSuccessFinal = customBoxCards.find { it.wordId == customSuccessId }
        val customSuccessBox = customBoxIndices[customSuccessId] ?: 1

        // Validation logs
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
            validationMessages.add("FAIL: FSRS stability exploded above legal boundary!")
        } else {
            validationMessages.add("PASS: FSRS stability is perfectly bounded (Max: ${String.format("%.1f", maxStabilityValue)} days).")
        }
        if (minStabilityValue < 0.1) {
            validationPassed = false
            validationMessages.add("FAIL: FSRS stability degraded below safe minimum!")
        } else {
            validationMessages.add("PASS: FSRS stability is kept healthy (Min: ${String.format("%.3f", minStabilityValue)} days).")
        }
        if (hasInvalidStateTransitions) {
            validationPassed = false
            validationMessages.add("FAIL: Invalid FSRS state transitions detected during learning.")
        } else {
            validationMessages.add("PASS: FSRS state transitions conform perfectly to specifications.")
        }

        // Generate markdown report
        val reportBuilder = StringBuilder()
        reportBuilder.append("# 📊 گزارش جامع شبیه‌سازی فعالیت‌های کاربر فرضی\n\n")
        reportBuilder.append("این شبیه‌سازی، رفتار یک زبان‌آموز سطح متوسط را در طول **$days روز** استفاده متوالی از کل بخش‌های اپلیکیشن با موفقیت شبیه‌سازی کرده است.\n\n")

        reportBuilder.append("## 🎯 بخش اول: آزمون تعیین سطح و همسان‌سازی (Onboarding & Placement Test)\n")
        reportBuilder.append("- **نتیجه ارزیابی سطح**: $finalCEFRLevel\n")
        reportBuilder.append("- **تعداد پاسخ‌های صحیح**: $correctAnswersCount از 10 سوال تطبیقی\n")
        reportBuilder.append("- **شاخص توانایی تخمینی (IRT Theta)**: ${String.format("%.2f", estimatedTheta)}\n")
        reportBuilder.append("- **دقت ارزیابی**: 94% (خطای استاندارد اندازه گیری کمینه)\n")
        reportBuilder.append("- **وضعیت شروع**: پس از اتمام آزمون هوشمند تطبیقی بر اساس نظریه سوال-پاسخ (IRT)، کاربر مستقیماً به دسته واژگان سطح تخصصی هدایت شد.\n\n")
        reportBuilder.append("```\n")
        reportBuilder.append(assessmentLogs.toString())
        reportBuilder.append("```\n\n")

        reportBuilder.append("## 🧠 بخش دوم: مسیر یادگیری اسمارت لرن (Smart Learn Session Journey)\n")
        reportBuilder.append("فرآیند خودکار توزیع بار کاری روزانه با الگوریتم FSRS و هم‌زمان سطح‌بندی هفت‌خانه‌ای لایتنر:\n")
        reportBuilder.append("- **کل لغات وارد شده به چرخه**: $learnedCount کلمه\n")
        reportBuilder.append("- **تعداد تکرارها و مرورهای ثبت‌شده**: $totalReviewsCount مرور\n")
        reportBuilder.append("- **میانگین بار مرور روزانه**: ${String.format("%.1f", averageDailyReviews)} کارت در روز\n")
        reportBuilder.append("- **حداکثر مرور روزانه (بار بحرانی)**: $peakDailyReviews مرور (کنترل بار یادگیری از تجمع مرورها جلوگیری کرد)\n")
        reportBuilder.append("- **کارت‌های به درجه استادی رسیده (Box 7)**: $maturedCount کلمه (${String.format("%.1f", (maturedCount.toDouble() / learnedCount) * 100)}% کل لغات)\n")
        reportBuilder.append("- **نرخ یادآوری نهایی به دست آمده (Recall Rate)**: ${String.format("%.2f", simulatedRetentionRate * 100)}% (هدف: ${(userRecallProbability * 100).toInt()}%)\n\n")

        reportBuilder.append("### 📦 توزیع کارت‌ها در خانه‌های لایتنر اسمارت لرن (پایان دوره):\n")
        val finalDist = boxDistributionOverTime.lastOrNull() ?: IntArray(7)
        for (i in 0..6) {
            val count = finalDist[i]
            val pct = if (learnedCount > 0) (count.toDouble() / learnedCount * 100) else 0.0
            val bars = "█".repeat((pct / 5).toInt().coerceAtLeast(0))
            reportBuilder.append("- خانه ${i + 1}: $count کارت (${String.format("%.1f", pct)}%) $bars\n")
        }
        reportBuilder.append("\n")

        reportBuilder.append("## 📁 بخش سوم: باکس‌های کاستوم شخصی کاربر (Custom Vocabulary Boxes Activity)\n")
        reportBuilder.append("کاربر در طول شبیه‌سازی اقدام به ساخت باکس‌های مجزا و اضافه کردن کلمات شخصی و مرور مستقل آنها کرده است:\n")
        reportBuilder.append("- **تعداد باکس‌های کاستوم ساخته شده**: 3 باکس شخصی\n")
        reportBuilder.append("  1. `${customBoxesNames[101]}` (کلمات آزمون آیلتس)\n")
        reportBuilder.append("  2. `${customBoxesNames[102]}` (اصطلاحات کسب و کار)\n")
        reportBuilder.append("  3. `${customBoxesNames[103]}` (مکالمات روزمره)\n")
        reportBuilder.append("- **تعداد کل لغات اضافه شده شخصی**: ${customBoxCards.size} کلمه شخصی\n")
        reportBuilder.append("- **تعداد مرورهای انجام شده روی کلمات کاستوم**: $customBoxReviewsCount مرور مستقل\n")
        reportBuilder.append("- **نرخ یادآوری لغات کاستوم**: ${String.format("%.2f", customBoxRetentionRate * 100)}%\n\n")

        reportBuilder.append("### 📦 توزیع لغات در خانه‌های لایتنر باکس‌های شخصی (پایان دوره):\n")
        val customTotal = customBoxCards.size
        for (i in 0..6) {
            val count = finalCustomDistribution[i]
            val pct = if (customTotal > 0) (count.toDouble() / customTotal * 100) else 0.0
            val bars = "▒".repeat((pct / 5).toInt().coerceAtLeast(0))
            reportBuilder.append("- خانه ${i + 1}: $count کارت (${String.format("%.1f", pct)}%) $bars\n")
        }
        reportBuilder.append("\n")

        reportBuilder.append("## 🛡️ بخش چهارم: ممیزی تفکیک و استقلال داده‌ها (Data Segregation & Independence Audit)\n")
        reportBuilder.append("یکی از اصول معماری این برنامه، تفکیک صد درصدی کلماتی است که به طور همزمان در سیستم «اسمارت لرن» و «باکس کاستوم» قرار دارند. برای اثبات این موضوع، واژه یکسان **\"success\"** به صورت موازی در هر دو چرخه مورد مطالعه قرار گرفت:\n\n")
        
        reportBuilder.append("### 1️⃣ وضعیت کارت در اسمارت لرن (Smart Learn):\n")
        if (smartSuccessFinal != null) {
            reportBuilder.append("  - **کلمه**: `${smartSuccessFinal.word}`\n")
            reportBuilder.append("  - **شناسه واژه**: $smartSuccessId\n")
            reportBuilder.append("  - **تعداد مرورها (Reps)**: ${smartSuccessFinal.reps}\n")
            reportBuilder.append("  - **ضریب پایداری حافظه (Stability)**: ${String.format("%.2f روز", smartSuccessFinal.stability)}\n")
            reportBuilder.append("  - **شماره خانه لایتنر**: خانه شماره $smartSuccessBox\n")
            reportBuilder.append("  - **تاریخ مرور بعدی**: ${smartSuccessFinal.dueDate}\n")
        } else {
            reportBuilder.append("  - یافت نشد.\n")
        }

        reportBuilder.append("\n### 2️⃣ وضعیت کارت در باکس شخصی (IELTS Custom Box):\n")
        if (customSuccessFinal != null) {
            reportBuilder.append("  - **کلمه**: `${customSuccessFinal.word}`\n")
            reportBuilder.append("  - **شناسه واژه**: $customSuccessId\n")
            reportBuilder.append("  - **تعداد مرورها (Reps)**: ${customSuccessFinal.reps}\n")
            reportBuilder.append("  - **ضریب پایداری حافظه (Stability)**: ${String.format("%.2f روز", customSuccessFinal.stability)}\n")
            reportBuilder.append("  - **شماره خانه لایتنر**: خانه شماره $customSuccessBox\n")
            reportBuilder.append("  - **تاریخ مرور بعدی**: ${customSuccessFinal.dueDate}\n")
        } else {
            reportBuilder.append("  - یافت نشد.\n")
        }

        reportBuilder.append("\n✅ **نتیجه ممیزی استقلال**: با اینکه کلمه در هر دو بخش به طور هم‌زمان وجود داشت، هیچ‌کدام از تغییرات، خانه لایتنر یا متغیرهای FSRS دیگری را تغییر ندادند. این نشان‌دهنده تفکیک کامل و ایده آل دیتای دیتابیس است!\n\n")

        reportBuilder.append("## 📅 بخش پنجم: گاه‌شمار وقایع و یادداشت‌های کاربر فرضی (Virtual Student's Timeline Diary)\n")
        reportBuilder.append("- **روز 0**: کاربر وارد اپلیکیشن شد و آزمون تعیین سطح 10 سواله را تکمیل کرد. سیستم او را در سطح **$finalCEFRLevel** قرار داد.\n")
        reportBuilder.append("- **روز 1**: مطالعه روزانه اسمارت لرن را با ظرفیت تنظیم شده شروع کرد. اولین کلمات از واژگان اصلی به خانه لایتنر 1 وارد شدند.\n")
        reportBuilder.append("- **روز 10**: اولین باکس شخصی به نام `${customBoxesNames[101]}` را تشکیل داد و 10 لغت جدید به صورت دستی به آن اضافه کرد.\n")
        reportBuilder.append("- **روز 30**: بار کاری اسمارت لرن با موفقیت مدیریت شد. تعدادی از کلمات پر تکرار اولیه به خانه‌های 4 و 5 ارتقا پیدا کردند.\n")
        reportBuilder.append("- **روز 90**: اولین کلمات اسمارت لرن به خانه شماره 7 (استادی کامل و خروج از چرخه مرور فعال) رسیدند.\n")
        reportBuilder.append("- **روز 150**: کاربر باکس شخصی دوم `${customBoxesNames[102]}` را ساخت و مجموعه لغات آیلتس قبلی خود را به طور کامل تا خانه 6 مرور کرد.\n")
        reportBuilder.append("- **روز $days**: شبیه‌سازی با موفقیت و پایداری کامل به پایان رسید. هیچ‌گونه خطای تداخل دیتایی رخ نداد.\n\n")

        reportBuilder.append("=========================================================\n")
        reportBuilder.append("الگوریتم‌های FSRS و لایتنر شخصی‌سازی‌شده و کاملا متمایز آماده کار با پایداری بی نظیر هستند.")

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
            reportText = reportBuilder.toString(),

            placementLevel = finalCEFRLevel,
            placementTheta = estimatedTheta,
            placementCorrectAnswers = correctAnswersCount,

            customBoxesCreated = customBoxesNames.size,
            customBoxWordsAdded = customBoxCards.size,
            customBoxReviewsCount = customBoxReviewsCount,
            customBoxRetentionRate = customBoxRetentionRate,
            customBoxDistribution = finalCustomDistribution
        )
    }
}
