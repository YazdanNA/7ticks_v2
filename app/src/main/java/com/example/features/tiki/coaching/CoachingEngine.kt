package com.example.features.tiki.coaching

import com.example.features.tiki.director.DirectorEngine
import com.example.features.tiki.director.DirectorDecision
import com.example.features.tiki.director.DirectorPriority
import com.example.features.tiki.model.EmotionState
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.random.Random

class CoachingEngine(
    val goalRepository: GoalRepository = GoalRepository(),
    private val directorEngine: DirectorEngine = DirectorEngine.getInstance(),
    initialRules: List<CoachingRule> = emptyList()
) {
    private val rules = CopyOnWriteArrayList<CoachingRule>()
    private var isStudying: Boolean = false

    init {
        if (initialRules.isEmpty()) {
            // Load default coaching rules
            rules.addAll(getDefaultRules())
        } else {
            rules.addAll(initialRules)
        }

        // Register default goals so they are ready out-of-the-box
        registerDefaultGoals()

        // Hook up goal completed listener
        goalRepository.addGoalCompletedListener { goal ->
            onGoalCompleted(goal)
        }
    }

    fun addRule(rule: CoachingRule) {
        rules.add(rule)
    }

    fun removeAllRules() {
        rules.clear()
    }

    fun setStudyingState(studying: Boolean) {
        isStudying = studying
    }

    fun getStudyingState(): Boolean = isStudying

    /**
     * Registers all standard goals supported by SevenTicks Coaching V1.0.
     */
    private fun registerDefaultGoals() {
        goalRepository.registerGoal(LearningGoal.DailyStudy(targetMinutes = 15))
        goalRepository.registerGoal(LearningGoal.DailyReview(targetReviewCount = 20))
        goalRepository.registerGoal(LearningGoal.DailyNewWords(targetNewWords = 5))
        goalRepository.registerGoal(LearningGoal.WeeklyConsistency(targetDays = 5))
        goalRepository.registerGoal(LearningGoal.MasterWords(targetMasteredWordsCount = 100))
        goalRepository.registerGoal(LearningGoal.LongestStreak(targetStreakDays = 7))
        goalRepository.registerGoal(LearningGoal.SessionCompletion(targetSessionsCount = 1))
        goalRepository.registerGoal(LearningGoal.ReviewQueueCleanup(targetQueueSize = 0))
    }

    /**
     * Generates a list of coaching suggestions depending on context, progress, and timing.
     * Respects the absolute boundary of never interrupting while studying.
     */
    fun getSuggestions(context: CoachingContext): List<CoachingSuggestion> {
        // Safe check: Never interrupt while studying
        if (isStudying) {
            return emptyList()
        }

        // Timing check: Suggestions appear only before/after session, home screen, inactivity, or goal completion
        val safeTiming = context.timing == SuggestionTiming.BEFORE_SESSION ||
                context.timing == SuggestionTiming.AFTER_SESSION ||
                context.timing == SuggestionTiming.HOME_SCREEN ||
                context.timing == SuggestionTiming.LONG_INACTIVITY ||
                context.timing == SuggestionTiming.GOAL_COMPLETION

        if (!safeTiming) {
            return emptyList()
        }

        return rules.mapNotNull { it.evaluate(context) }
            .sortedByDescending { it.priority }
    }

    /**
     * Handles goal completion. Sends corresponding event decisions to DirectorEngine.
     * Director decides whether to Speak, Celebrate, Smile, or Remain silent.
     */
    fun onGoalCompleted(goal: LearningGoal, random: Random = Random(System.currentTimeMillis())) {
        val decision = when (random.nextInt(4)) {
            0 -> {
                // Speak and show happy emotion
                val msg = when (goal) {
                    is LearningGoal.DailyStudy -> "Outstanding! You completed your Daily Study goal!"
                    is LearningGoal.DailyReview -> "Wonderful review session! Your Daily Review goal is complete!"
                    is LearningGoal.DailyNewWords -> "Awesome progress! You mastered your new words goal!"
                    is LearningGoal.WeeklyConsistency -> "Magnificent consistency this week! Keep it up!"
                    is LearningGoal.MasterWords -> "Amazing achievement! You reached your master words goal!"
                    is LearningGoal.LongestStreak -> "What a legendary streak! Goal completed!"
                    is LearningGoal.SessionCompletion -> "Superb job finishing that learning session!"
                    is LearningGoal.ReviewQueueCleanup -> "Spectacular! Your review queue is entirely cleared!"
                    is LearningGoal.CustomGoal -> "Incredible job completing your personal goal: ${goal.title}!"
                }
                DirectorDecision.SpeakAndShowEmotion(msg, EmotionState.HAPPY)
            }
            1 -> {
                // Celebrate
                DirectorDecision.PlayCelebration("goal_celebration_${goal.id}")
            }
            2 -> {
                // Smile (Show emotion)
                DirectorDecision.ShowEmotion(EmotionState.HAPPY)
            }
            else -> {
                // Remain silent
                DirectorDecision.RemainSilent
            }
        }

        // Submit to DirectorEngine with ACHIEVEMENT priority
        directorEngine.submitEvent(decision, DirectorPriority.ACHIEVEMENT)
    }

    private fun getDefaultRules(): List<CoachingRule> {
        return listOf(
            // Rule 1: Daily Session recommendation
            CoachingRule { context ->
                val studyProgress = context.progresses["daily_study"]
                if (studyProgress != null && !studyProgress.isCompleted && context.timing == SuggestionTiming.BEFORE_SESSION) {
                    val text = when (context.preferredStyle) {
                        CoachingStyle.Encouraging -> "Let's kick off today's study session together! You're making beautiful progress."
                        CoachingStyle.Gentle -> "Whenever you're ready, we can start with a short lesson at your own pace."
                        CoachingStyle.Playful -> "Ready to train your brain? Today's session is going to be a fun one!"
                        CoachingStyle.Calm -> "Let's take a peaceful moment to focus and start our study session."
                    }
                    CoachingSuggestion("start_session", text, 80, CoachingActionType.START_TODAY_SESSION, context.preferredStyle)
                } else null
            },
            // Rule 2: Review Cleanup recommendations
            CoachingRule { context ->
                if (context.remainingReviews > 15 && (context.timing == SuggestionTiming.HOME_SCREEN || context.timing == SuggestionTiming.BEFORE_SESSION)) {
                    val text = when (context.preferredStyle) {
                        CoachingStyle.Encouraging -> "You've got ${context.remainingReviews} reviews ready! Let's clear them and strengthen your memory."
                        CoachingStyle.Gentle -> "Let's gently review a few cards when you have some quiet time."
                        CoachingStyle.Playful -> "Time to defeat those ${context.remainingReviews} review cards! Let's clear the board!"
                        CoachingStyle.Calm -> "Let's calmly review our card collection to keep things clear and fresh."
                    }
                    CoachingSuggestion("finish_reviews", text, 70, CoachingActionType.FINISH_TODAY_REVIEWS, context.preferredStyle)
                } else null
            },
            // Rule 3: Break recommendation when studying too long
            CoachingRule { context ->
                if (context.studyDurationMinutes >= 30) {
                    val text = when (context.preferredStyle) {
                        CoachingStyle.Encouraging -> "You've been studying hard for ${context.studyDurationMinutes} minutes! Let's take a short, well-earned break."
                        CoachingStyle.Gentle -> "You're doing great. Maybe rest your eyes and take a soft breath."
                        CoachingStyle.Playful -> "Brain power running hot! Time to step away for a quick refresh."
                        CoachingStyle.Calm -> "Let's pause, stretch, and rest before we continue."
                    }
                    CoachingSuggestion("take_break", text, 95, CoachingActionType.TAKE_A_SHORT_BREAK, context.preferredStyle)
                } else null
            },
            // Rule 4: Continue Streak recommendation
            CoachingRule { context ->
                if (context.currentStreak > 0 && context.timing == SuggestionTiming.BEFORE_SESSION) {
                    val text = when (context.preferredStyle) {
                        CoachingStyle.Encouraging -> "You have a fantastic ${context.currentStreak}-day streak! Let's keep the learning momentum strong."
                        CoachingStyle.Gentle -> "We can easily keep your ${context.currentStreak}-day rhythm going with a quick visit today."
                        CoachingStyle.Playful -> "A wild ${context.currentStreak}-day streak appears! Let's keep that fire burning!"
                        CoachingStyle.Calm -> "Your consistent ${context.currentStreak}-day rhythm is a peaceful way to learn."
                    }
                    CoachingSuggestion("continue_streak", text, 85, CoachingActionType.CONTINUE_YOUR_STREAK, context.preferredStyle)
                } else null
            },
            // Rule 5: Difficult Words review recommendation
            CoachingRule { context ->
                if (context.recentPerformanceScore < 0.7f && context.timing == SuggestionTiming.AFTER_SESSION) {
                    val text = when (context.preferredStyle) {
                        CoachingStyle.Encouraging -> "No worries at all! Let's try reviewing some of those challenging words together."
                        CoachingStyle.Gentle -> "Mistakes are just part of learning. We can look at the tricky words together."
                        CoachingStyle.Playful -> "Some tricky cards are trying to challenge us! Let's review them and win."
                        CoachingStyle.Calm -> "Let's peacefully revisit the words we found slightly difficult."
                    }
                    CoachingSuggestion("difficult_words", text, 65, CoachingActionType.REVIEW_DIFFICULT_WORDS, context.preferredStyle)
                } else null
            }
        )
    }
}
