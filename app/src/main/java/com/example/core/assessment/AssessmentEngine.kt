package com.example.core.assessment

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

// ============================================================
// CEFR Level Assessment — IRT + CAT Algorithm
// Based on: Item Response Theory (3PL Model) + Computerized Adaptive Testing
// Target: 10 questions, high accuracy, fully offline
// Focus: Vocabulary MEANING comprehension (flashcard app context)
// ============================================================

// ─────────────────────────────────────────────────────────────
// DATA MODELS
// ─────────────────────────────────────────────────────────────

enum class CEFRLevel(val label: String, val description: String, val theta: Double) {
    A1("A1", "Beginner",         -3.0),
    A2("A2", "Elementary",       -1.8),
    B1("B1", "Intermediate",     -0.6),
    B2("B2", "Upper-Intermediate", 0.6),
    C1("C1", "Advanced",          1.8),
    C2("C2", "Mastery",           3.0)
}

/**
 * IRT 3-Parameter Logistic Model item.
 *
 * @param id           Unique identifier
 * @param cefrLevel    Target CEFR level of this item
 * @param difficulty   β (b) — item difficulty on theta scale [-3, +3]
 * @param discrimination α (a) — how well item separates ability levels [0.5, 2.5]
 * @param guessing     γ (c) — probability of correct answer by guessing [0.0, 0.25]
 * @param question     The question text shown to user
 * @param options      Exactly 4 options (A/B/C/D)
 * @param correctIndex Index of correct answer in options list (0-based)
 * @param distractorQuality  0.0-1.0, how plausible wrong answers are (affects scoring weight)
 */
data class AssessmentItem(
    val id: String,
    val cefrLevel: CEFRLevel,
    val difficulty: Double,        // b parameter
    val discrimination: Double,    // a parameter
    val guessing: Double,          // c parameter
    val question: String,
    val options: List<String>,     // exactly 4
    val correctIndex: Int,
    val distractorQuality: Double = 0.7
)

data class ItemResponse(
    val item: AssessmentItem,
    val selectedIndex: Int,
    val isCorrect: Boolean,
    val responseTimeMs: Long = 0L
)

data class AssessmentResult(
    val cefrLevel: CEFRLevel,
    val theta: Double,              // Ability estimate
    val standardError: Double,      // Measurement precision
    val confidencePercent: Int,     // 0-100
    val levelScores: Map<CEFRLevel, Double>,  // Probability per level
    val totalQuestions: Int,
    val correctAnswers: Int,
    val detailedBreakdown: String
)

// ─────────────────────────────────────────────────────────────
// IRT ENGINE
// ─────────────────────────────────────────────────────────────

object IRTEngine {

    /**
     * 3-Parameter Logistic (3PL) probability of correct response.
     * P(θ) = c + (1-c) / (1 + e^(-1.7 * a * (θ - b)))
     */
    fun probability(theta: Double, item: AssessmentItem): Double {
        val exponent = -1.7 * item.discrimination * (theta - item.difficulty)
        return item.guessing + (1.0 - item.guessing) / (1.0 + exp(exponent))
    }

    /**
     * Fisher Information for an item at ability level theta.
     * Higher = more precise measurement at that theta.
     */
    fun information(theta: Double, item: AssessmentItem): Double {
        val p = probability(theta, item)
        val q = 1.0 - p
        val pStar = (p - item.guessing) / (1.0 - item.guessing)
        return (1.7 * item.discrimination).let { aScaled ->
            (aScaled * aScaled * q / p) * (pStar * pStar)
        }
    }

    /**
     * MLE (Maximum Likelihood Estimation) of theta given response history.
     * Uses Newton-Raphson iteration for convergence.
     */
    fun estimateTheta(responses: List<ItemResponse>, priorTheta: Double = 0.0): Double {
        if (responses.isEmpty()) return priorTheta

        // Bounds for theta estimation
        val thetaMin = -4.0
        val thetaMax = 4.0

        // Check for all-correct or all-incorrect (MLE undefined → use Bayesian fallback)
        val allCorrect = responses.all { it.isCorrect }
        val allWrong = responses.none { it.isCorrect }

        if (allCorrect) return minOf(thetaMax, priorTheta + 1.5)
        if (allWrong)   return maxOf(thetaMin, priorTheta - 1.5)

        // Newton-Raphson maximization of log-likelihood
        var theta = priorTheta
        repeat(50) { // max iterations
            var firstDerivative = 0.0
            var secondDerivative = 0.0

            for (response in responses) {
                val p = probability(theta, response.item)
                val q = 1.0 - p
                val u = if (response.isCorrect) 1.0 else 0.0
                val pStar = (p - response.item.guessing) / (1.0 - response.item.guessing)
                val w = p * q
                val dP = 1.7 * response.item.discrimination * pStar * q

                firstDerivative  += dP * (u - p) / (p * q)
                secondDerivative -= (dP * dP) / w
            }

            if (abs(secondDerivative) < 1e-10) return@repeat
            val step = firstDerivative / secondDerivative
            theta -= step.coerceIn(-1.0, 1.0) // Bound step size
            theta = theta.coerceIn(thetaMin, thetaMax)
        }

        return theta
    }

    /**
     * Standard Error of Measurement = 1 / sqrt(Total Information)
     */
    fun standardError(theta: Double, responses: List<ItemResponse>): Double {
        val totalInfo = responses.sumOf { information(theta, it.item) }
        return if (totalInfo > 0) 1.0 / sqrt(totalInfo) else 2.0
    }
}

// ─────────────────────────────────────────────────────────────
// CAT ENGINE — Adaptive Item Selection
// ─────────────────────────────────────────────────────────────

object CATEngine {

    /**
     * Select next best item using Maximum Fisher Information criterion.
     * Avoids already-used items and ensures CEFR level coverage.
     */
    fun selectNextItem(
        currentTheta: Double,
        usedItemIds: Set<String>,
        itemBank: List<AssessmentItem>,
        responses: List<ItemResponse>,
        questionNumber: Int
    ): AssessmentItem? {
        val available = itemBank.filter { it.id !in usedItemIds }
        if (available.isEmpty()) return null

        // Force coverage: ensure we test adjacent CEFR levels
        val levelExposure = responses.groupBy { it.item.cefrLevel }
        val underTestedLevel = findUnderTestedLevel(currentTheta, levelExposure, questionNumber)

        val candidates = if (underTestedLevel != null && questionNumber < 7) {
            // Force an item from under-tested level (stratified sampling)
            available.filter { it.cefrLevel == underTestedLevel }
                .takeIf { it.isNotEmpty() } ?: available
        } else {
            available
        }

        // Select item with maximum information at current theta estimate
        return candidates.maxByOrNull { IRTEngine.information(currentTheta, it) }
    }

    private fun findUnderTestedLevel(
        theta: Double,
        exposure: Map<CEFRLevel, List<ItemResponse>>,
        questionNumber: Int
    ): CEFRLevel? {
        // Determine which levels should be tested based on current estimate
        val estimatedLevel = thetaToCEFR(theta)
        val levelsToTest = buildList {
            add(estimatedLevel)
            val ordinal = CEFRLevel.values().indexOf(estimatedLevel)
            if (ordinal > 0) add(CEFRLevel.values()[ordinal - 1])
            if (ordinal < CEFRLevel.values().size - 1) add(CEFRLevel.values()[ordinal + 1])
        }

        return levelsToTest.firstOrNull { level ->
            (exposure[level]?.size ?: 0) < maxOf(1, questionNumber / 3)
        }
    }

    fun thetaToCEFR(theta: Double): CEFRLevel {
        return when {
            theta < -2.4 -> CEFRLevel.A1
            theta < -1.2 -> CEFRLevel.A2
            theta <  0.0 -> CEFRLevel.B1
            theta <  1.2 -> CEFRLevel.B2
            theta <  2.4 -> CEFRLevel.C1
            else         -> CEFRLevel.C2
        }
    }

    /**
     * Posterior probability of each CEFR level given theta and SE.
     * Models theta as normally distributed around estimate.
     */
    fun levelProbabilities(theta: Double, se: Double): Map<CEFRLevel, Double> {
        val levels = CEFRLevel.values()
        val rawScores = levels.map { level ->
            val dist = abs(theta - level.theta)
            val spread = maxOf(se, 0.5)
            exp(-0.5 * (dist / spread) * (dist / spread))
        }
        val total = rawScores.sum()
        return levels.zip(rawScores.map { it / total }).toMap()
    }
}

// ─────────────────────────────────────────────────────────────
// ASSESSMENT SESSION MANAGER
// ─────────────────────────────────────────────────────────────

class AssessmentSession(
    private val itemBank: List<AssessmentItem> = VocabularyItemBank.items,
    private val maxQuestions: Int = 10,
    private val stoppingThreshold: Double = 0.35  // SE threshold for early stop
) {
    private val responses = mutableListOf<ItemResponse>()
    private val usedIds = mutableSetOf<String>()
    private var currentTheta = 0.0  // Start at B1 (neutral prior)
    private var currentSE = 2.0
    var isComplete = false
        private set

    var currentItem: AssessmentItem? = null
        private set

    init {
        advanceToNextItem()
    }

    val questionNumber: Int get() = responses.size + 1
    val progressPercent: Int get() = (responses.size * 100) / maxQuestions

    fun getCurrentQuestion(): AssessmentItem? = currentItem

    fun submitAnswer(selectedIndex: Int, responseTimeMs: Long = 0L): Boolean {
        val item = currentItem ?: return false
        val isCorrect = selectedIndex == item.correctIndex

        val response = ItemResponse(item, selectedIndex, isCorrect, responseTimeMs)
        responses.add(response)
        usedIds.add(item.id)

        // Update theta estimate
        currentTheta = IRTEngine.estimateTheta(responses, currentTheta)
        currentSE    = IRTEngine.standardError(currentTheta, responses)

        // Check stopping criteria
        val shouldStop = responses.size >= maxQuestions ||
                (responses.size >= 6 && currentSE <= stoppingThreshold)

        if (shouldStop) {
            isComplete = true
            currentItem = null
        } else {
            advanceToNextItem()
        }

        return isCorrect
    }

    private fun advanceToNextItem() {
        currentItem = CATEngine.selectNextItem(
            currentTheta, usedIds, itemBank, responses, responses.size + 1
        )
        if (currentItem == null) isComplete = true
    }

    fun getResult(): AssessmentResult? {
        if (!isComplete && responses.size < 5) return null

        val finalTheta = currentTheta
        val finalSE    = currentSE
        val cefrLevel  = CATEngine.thetaToCEFR(finalTheta)
        val levelProbs = CATEngine.levelProbabilities(finalTheta, finalSE)
        val confidence = calculateConfidence(finalSE, responses.size)

        val correctCount = responses.count { it.isCorrect }

        val breakdown = buildString {
            appendLine("=== Assessment Breakdown ===")
            appendLine("Ability Estimate (θ): ${"%.2f".format(finalTheta)}")
            appendLine("Standard Error: ${"%.2f".format(finalSE)}")
            appendLine("Questions answered: ${responses.size}")
            appendLine("Correct: $correctCount / ${responses.size}")
            appendLine()
            appendLine("Level Probabilities:")
            levelProbs.forEach { (level, prob) ->
                val bar = "█".repeat((prob * 20).toInt())
                appendLine("  ${level.label}: $bar ${"%.1f".format(prob * 100)}%")
            }
            appendLine()
            appendLine("Detected Level: ${cefrLevel.label} — ${cefrLevel.description}")
        }

        return AssessmentResult(
            cefrLevel        = cefrLevel,
            theta            = finalTheta,
            standardError    = finalSE,
            confidencePercent = confidence,
            levelScores      = levelProbs,
            totalQuestions   = responses.size,
            correctAnswers   = correctCount,
            detailedBreakdown = breakdown
        )
    }

    private fun calculateConfidence(se: Double, questionCount: Int): Int {
        // Lower SE = higher confidence; more questions = higher confidence
        val seScore    = (1.0 - (se / 2.0).coerceIn(0.0, 1.0)) * 70
        val countScore = (questionCount.toDouble() / maxQuestions) * 30
        return (seScore + countScore).toInt().coerceIn(0, 100)
    }
}

// ─────────────────────────────────────────────────────────────
// VOCABULARY ITEM BANK
// High-quality mock data based on Oxford 5000 + academic word lists
// All items test MEANING comprehension (not spelling/grammar)
// ─────────────────────────────────────────────────────────────

object VocabularyItemBank {

    val items: List<AssessmentItem> = listOf(

        // ── A1 Items (θ ≈ -3.0 to -2.0) ─────────────────────
        AssessmentItem(
            id = "a1_01", cefrLevel = CEFRLevel.A1,
            difficulty = -2.8, discrimination = 1.2, guessing = 0.25,
            question = "What does 'happy' mean?",
            options = listOf("Feeling good and pleased", "Very tired", "Feeling sick", "Moving fast"),
            correctIndex = 0, distractorQuality = 0.5
        ),
        AssessmentItem(
            id = "a1_02", cefrLevel = CEFRLevel.A1,
            difficulty = -2.5, discrimination = 1.1, guessing = 0.25,
            question = "Which word means 'a place where you live'?",
            options = listOf("School", "Home", "Shop", "Park"),
            correctIndex = 1, distractorQuality = 0.6
        ),
        AssessmentItem(
            id = "a1_03", cefrLevel = CEFRLevel.A1,
            difficulty = -2.2, discrimination = 1.3, guessing = 0.25,
            question = "What does 'eat' mean?",
            options = listOf("To sleep", "To run", "To put food in your mouth", "To talk"),
            correctIndex = 2, distractorQuality = 0.5
        ),

        // ── A2 Items (θ ≈ -2.0 to -1.0) ─────────────────────
        AssessmentItem(
            id = "a2_01", cefrLevel = CEFRLevel.A2,
            difficulty = -1.9, discrimination = 1.4, guessing = 0.25,
            question = "What does 'borrow' mean?",
            options = listOf("To take something and keep it", "To take something temporarily and return it", "To buy something", "To sell something"),
            correctIndex = 1, distractorQuality = 0.75
        ),
        AssessmentItem(
            id = "a2_02", cefrLevel = CEFRLevel.A2,
            difficulty = -1.6, discrimination = 1.5, guessing = 0.25,
            question = "Which word means 'to feel pain or discomfort'?",
            options = listOf("Enjoy", "Hurt", "Laugh", "Rest"),
            correctIndex = 1, distractorQuality = 0.7
        ),
        AssessmentItem(
            id = "a2_03", cefrLevel = CEFRLevel.A2,
            difficulty = -1.3, discrimination = 1.6, guessing = 0.25,
            question = "What does 'suggest' mean?",
            options = listOf("To demand forcefully", "To forget about something", "To put forward an idea", "To agree completely"),
            correctIndex = 2, distractorQuality = 0.75
        ),

        // ── B1 Items (θ ≈ -1.0 to 0.0) ──────────────────────
        AssessmentItem(
            id = "b1_01", cefrLevel = CEFRLevel.B1,
            difficulty = -0.9, discrimination = 1.7, guessing = 0.20,
            question = "What does 'reluctant' mean?",
            options = listOf("Very excited and eager", "Unwilling or hesitant to do something", "Completely unable to do something", "Fully prepared and ready"),
            correctIndex = 1, distractorQuality = 0.80
        ),
        AssessmentItem(
            id = "b1_02", cefrLevel = CEFRLevel.B1,
            difficulty = -0.6, discrimination = 1.8, guessing = 0.20,
            question = "If something is 'inevitable', it means...",
            options = listOf("It can be easily avoided", "It is impossible to understand", "It is certain to happen", "It happens very rarely"),
            correctIndex = 2, distractorQuality = 0.82
        ),
        AssessmentItem(
            id = "b1_03", cefrLevel = CEFRLevel.B1,
            difficulty = -0.3, discrimination = 1.7, guessing = 0.20,
            question = "What does 'persevere' mean?",
            options = listOf("To give up quickly when things get hard", "To continue despite difficulties", "To work very slowly", "To ask others for help"),
            correctIndex = 1, distractorQuality = 0.83
        ),

        // ── B2 Items (θ ≈ 0.0 to 1.2) ───────────────────────
        AssessmentItem(
            id = "b2_01", cefrLevel = CEFRLevel.B2,
            difficulty = 0.2, discrimination = 1.9, guessing = 0.15,
            question = "What does 'ambiguous' mean?",
            options = listOf("Perfectly clear and obvious", "Open to more than one interpretation", "Completely false and misleading", "Extremely complex and detailed"),
            correctIndex = 1, distractorQuality = 0.85
        ),
        AssessmentItem(
            id = "b2_02", cefrLevel = CEFRLevel.B2,
            difficulty = 0.5, discrimination = 2.0, guessing = 0.15,
            question = "If someone is 'meticulous', they are...",
            options = listOf("Careless and disorganized", "Very slow and inefficient", "Paying great attention to detail", "Overly aggressive and forceful"),
            correctIndex = 2, distractorQuality = 0.87
        ),
        AssessmentItem(
            id = "b2_03", cefrLevel = CEFRLevel.B2,
            difficulty = 0.8, discrimination = 1.9, guessing = 0.15,
            question = "What does 'mitigate' mean?",
            options = listOf("To make something worse", "To completely eliminate a problem", "To reduce the severity of something", "To transfer responsibility to others"),
            correctIndex = 2, distractorQuality = 0.88
        ),
        AssessmentItem(
            id = "b2_04", cefrLevel = CEFRLevel.B2,
            difficulty = 1.0, discrimination = 2.0, guessing = 0.15,
            question = "What does 'corroborate' mean?",
            options = listOf("To disprove with evidence", "To confirm or support with evidence", "To misinterpret information", "To withhold important details"),
            correctIndex = 1, distractorQuality = 0.88
        ),

        // ── C1 Items (θ ≈ 1.2 to 2.4) ───────────────────────
        AssessmentItem(
            id = "c1_01", cefrLevel = CEFRLevel.C1,
            difficulty = 1.3, discrimination = 2.1, guessing = 0.10,
            question = "What does 'equivocate' mean?",
            options = listOf("To speak very loudly and clearly", "To use vague language to avoid committing to a position", "To translate between two languages", "To repeat the same argument many times"),
            correctIndex = 1, distractorQuality = 0.90
        ),
        AssessmentItem(
            id = "c1_02", cefrLevel = CEFRLevel.C1,
            difficulty = 1.6, discrimination = 2.2, guessing = 0.10,
            question = "If something is 'ephemeral', it is...",
            options = listOf("Extremely durable and long-lasting", "Lasting for only a very short time", "Difficult to explain clearly", "Present everywhere at once"),
            correctIndex = 1, distractorQuality = 0.90
        ),
        AssessmentItem(
            id = "c1_03", cefrLevel = CEFRLevel.C1,
            difficulty = 1.9, discrimination = 2.1, guessing = 0.10,
            question = "What does 'obfuscate' mean?",
            options = listOf("To make something very clear and transparent", "To deliberately make something confusing or unclear", "To organize information systematically", "To exaggerate the importance of something"),
            correctIndex = 1, distractorQuality = 0.91
        ),
        AssessmentItem(
            id = "c1_04", cefrLevel = CEFRLevel.C1,
            difficulty = 2.1, discrimination = 2.2, guessing = 0.10,
            question = "What does 'sycophantic' describe?",
            options = listOf("Extremely knowledgeable and wise", "Overly critical and harsh", "Using flattery to gain favor", "Completely independent and self-reliant"),
            correctIndex = 2, distractorQuality = 0.90
        ),

        // ── C2 Items (θ ≈ 2.4 to 3.0) ───────────────────────
        AssessmentItem(
            id = "c2_01", cefrLevel = CEFRLevel.C2,
            difficulty = 2.4, discrimination = 2.3, guessing = 0.05,
            question = "What does 'recondite' mean?",
            options = listOf("Easily understood by everyone", "Not well known; obscure and specialized", "Recently discovered or invented", "Widely accepted and conventional"),
            correctIndex = 1, distractorQuality = 0.93
        ),
        AssessmentItem(
            id = "c2_02", cefrLevel = CEFRLevel.C2,
            difficulty = 2.6, discrimination = 2.4, guessing = 0.05,
            question = "If a policy is 'inimical' to progress, it means the policy is...",
            options = listOf("Essential for achieving progress", "Neutral and having no effect on progress", "Actively harmful or opposed to progress", "Intended to measure progress"),
            correctIndex = 2, distractorQuality = 0.93
        ),
        AssessmentItem(
            id = "c2_03", cefrLevel = CEFRLevel.C2,
            difficulty = 2.8, discrimination = 2.5, guessing = 0.05,
            question = "What does 'tendentious' mean when describing writing?",
            options = listOf("Extremely concise and precise", "Promoting a particular point of view in a biased way", "Covering many different topics broadly", "Based purely on factual evidence"),
            correctIndex = 1, distractorQuality = 0.94
        ),
        AssessmentItem(
            id = "c2_04", cefrLevel = CEFRLevel.C2,
            difficulty = 3.0, discrimination = 2.4, guessing = 0.05,
            question = "What does 'apophenia' refer to?",
            options = listOf("The ability to recognize genuine patterns in complex data", "The tendency to perceive meaningful connections in unrelated things", "A form of extremely precise logical reasoning", "The process of forgetting previously learned information"),
            correctIndex = 1, distractorQuality = 0.95
        )
    )
}
