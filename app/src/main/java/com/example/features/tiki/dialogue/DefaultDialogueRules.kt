package com.example.features.tiki.dialogue

import com.example.features.tiki.behavior.BehaviorEvent
import com.example.features.tiki.memory.LearningMood
import com.example.features.tiki.model.EmotionState

object DefaultDialogueRules {

    val sessionCompleteRule = object : DialogueRule {
        override val ruleName = "SessionCompleteRule"
        override val category = DialogueCategory.SessionComplete
        override val priority = DialoguePriority.SESSION_COMPLETE

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.SessionFinished || context.sessionProgress >= 1.0f) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Session complete!", 2),
                        WeightedDialogue("Awesome study flow!", 2),
                        WeightedDialogue("Daily goal reached!", 1),
                        WeightedDialogue("Spectacular job!", 1)
                    )
                )
            }
            return null
        }
    }

    val halfSessionRule = object : DialogueRule {
        override val ruleName = "HalfSessionRule"
        override val category = DialogueCategory.HalfSession
        override val priority = DialoguePriority.MILESTONE_HALF

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.sessionProgress in 0.48f..0.52f && context.behaviorEvent != null && context.behaviorEvent !is BehaviorEvent.SessionFinished) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Halfway there!", 2),
                        WeightedDialogue("Great momentum!", 2),
                        WeightedDialogue("Keep the pace!", 1)
                    )
                )
            }
            return null
        }
    }

    val silenceThreeEasyRule = object : DialogueRule {
        override val ruleName = "SilenceThreeEasyRule"
        override val category = DialogueCategory.Silence
        override val priority = DialoguePriority.STREAK + 10 // 60

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            val easyStreak = context.memorySnapshot?.currentEasyStreak ?: 0
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredEasy && easyStreak >= 3) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(WeightedDialogue("", 1))
                )
            }
            return null
        }
    }

    val fiveAgainStreakRule = object : DialogueRule {
        override val ruleName = "FiveAgainStreakRule"
        override val category = DialogueCategory.AgainStreak
        override val priority = DialoguePriority.STREAK + 5 // 55

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            val againStreak = context.memorySnapshot?.currentAgainStreak ?: 0
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredAgain && againStreak >= 5) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Take a breath.", 2),
                        WeightedDialogue("Tough card today.", 1),
                        WeightedDialogue("Stay strong!", 2),
                        WeightedDialogue("We will conquer this.", 1)
                    )
                )
            }
            return null
        }
    }

    val easyStreakRule = object : DialogueRule {
        override val ruleName = "EasyStreakRule"
        override val category = DialogueCategory.EasyStreak
        override val priority = DialoguePriority.STREAK // 50

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            val easyStreak = context.memorySnapshot?.currentEasyStreak ?: 0
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredEasy && easyStreak >= 4) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Unstoppable!", 2),
                        WeightedDialogue("On fire!", 2),
                        WeightedDialogue("Pure genius!", 1),
                        WeightedDialogue("Incredible streak!", 1)
                    )
                )
            }
            return null
        }
    }

    val pokerLongThinkingRule = object : DialogueRule {
        override val ruleName = "PokerLongThinkingRule"
        override val category = DialogueCategory.LongThinking
        override val priority = DialoguePriority.THINKING + 5 // 35

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            val isLongThinking = context.thinkingTimeMillis >= 8000L || context.behaviorEvent is BehaviorEvent.CardThinkingFinished && context.behaviorEvent.durationMillis >= 8000L
            if (context.currentEmotion == EmotionState.POKER && isLongThinking) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Let's stay neutral.", 2),
                        WeightedDialogue("Deep in thought.", 2),
                        WeightedDialogue("Steady pace.", 1)
                    )
                )
            }
            return null
        }
    }

    val longThinkingRule = object : DialogueRule {
        override val ruleName = "LongThinkingRule"
        override val category = DialogueCategory.LongThinking
        override val priority = DialoguePriority.THINKING // 30

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            val isLongThinking = context.thinkingTimeMillis >= 8000L || context.behaviorEvent is BehaviorEvent.CardThinkingFinished && context.behaviorEvent.durationMillis >= 8000L
            if (isLongThinking) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Take your time.", 2),
                        WeightedDialogue("No rush.", 2),
                        WeightedDialogue("Thinking deeply...", 1)
                    )
                )
            }
            return null
        }
    }

    val happyEasyRule = object : DialogueRule {
        override val ruleName = "HappyEasyRule"
        override val category = DialogueCategory.Easy
        override val priority = DialoguePriority.STANDARD + 2 // 22

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredEasy && context.currentEmotion == EmotionState.HAPPY) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Piece of cake!", 2),
                        WeightedDialogue("Too easy!", 2),
                        WeightedDialogue("Nice work!", 1),
                        WeightedDialogue("Awesome!", 1)
                    )
                )
            }
            return null
        }
    }

    val sadAgainRule = object : DialogueRule {
        override val ruleName = "SadAgainRule"
        override val category = DialogueCategory.Again
        override val priority = DialoguePriority.STANDARD + 2 // 22

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredAgain && context.currentEmotion == EmotionState.SAD) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Oops, not quite.", 2),
                        WeightedDialogue("Almost had it.", 2),
                        WeightedDialogue("We will learn this.", 1)
                    )
                )
            }
            return null
        }
    }

    val recoveryRule = object : DialogueRule {
        override val ruleName = "RecoveryRule"
        override val category = DialogueCategory.Recovery
        override val priority = DialoguePriority.STANDARD + 5 // 25

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            val isRecovering = context.memorySnapshot?.learningMood == LearningMood.RECOVERING
            val isPositiveAnswer = context.behaviorEvent is BehaviorEvent.CardAnsweredEasy || context.behaviorEvent is BehaviorEvent.CardAnsweredGood
            if (isRecovering && isPositiveAnswer) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Back on track!", 2),
                        WeightedDialogue("Great comeback!", 2),
                        WeightedDialogue("Recovering nicely!", 1)
                    )
                )
            }
            return null
        }
    }

    val singleEasyRule = object : DialogueRule {
        override val ruleName = "SingleEasyRule"
        override val category = DialogueCategory.Easy
        override val priority = DialoguePriority.STANDARD // 20

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredEasy) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Easy!", 2),
                        WeightedDialogue("Smooth!", 2),
                        WeightedDialogue("Great job!", 1)
                    )
                )
            }
            return null
        }
    }

    val singleAgainRule = object : DialogueRule {
        override val ruleName = "SingleAgainRule"
        override val category = DialogueCategory.Again
        override val priority = DialoguePriority.STANDARD // 20

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredAgain) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Try again.", 2),
                        WeightedDialogue("Don't worry.", 2),
                        WeightedDialogue("Keep trying.", 1)
                    )
                )
            }
            return null
        }
    }

    val singleGoodRule = object : DialogueRule {
        override val ruleName = "SingleGoodRule"
        override val category = DialogueCategory.Good
        override val priority = DialoguePriority.STANDARD // 20

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredGood) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Well done!", 2),
                        WeightedDialogue("Good recall!", 2),
                        WeightedDialogue("Spot on!", 1)
                    )
                )
            }
            return null
        }
    }

    val singleHardRule = object : DialogueRule {
        override val ruleName = "SingleHardRule"
        override val category = DialogueCategory.Hard
        override val priority = DialoguePriority.STANDARD // 20

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardAnsweredHard) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Tough one!", 2),
                        WeightedDialogue("Good effort.", 2)
                    )
                )
            }
            return null
        }
    }

    val thinkingRule = object : DialogueRule {
        override val ruleName = "ThinkingRule"
        override val category = DialogueCategory.Thinking
        override val priority = DialoguePriority.STANDARD // 20

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.CardThinkingStarted) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Hmm...", 2),
                        WeightedDialogue("Let's see...", 2)
                    )
                )
            }
            return null
        }
    }

    val greetingRule = object : DialogueRule {
        override val ruleName = "GreetingRule"
        override val category = DialogueCategory.Greeting
        override val priority = DialoguePriority.SESSION_COMPLETE // 100

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent is BehaviorEvent.SessionStarted || context.sessionProgress == 0f && context.behaviorEvent == null) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Welcome back!", 2),
                        WeightedDialogue("Ready to learn!", 2),
                        WeightedDialogue("Let's begin!", 1)
                    )
                )
            }
            return null
        }
    }

    val idleRule = object : DialogueRule {
        override val ruleName = "IdleRule"
        override val category = DialogueCategory.Idle
        override val priority = DialoguePriority.IDLE // 10

        override fun evaluate(context: DialogueContext): DialogueRuleResult? {
            if (context.behaviorEvent == null) {
                return DialogueRuleResult(
                    category = category,
                    priority = priority,
                    candidates = listOf(
                        WeightedDialogue("Tiki is ready.", 2),
                        WeightedDialogue("Let's study!", 2)
                    )
                )
            }
            return null
        }
    }

    val all: List<DialogueRule> = listOf(
        sessionCompleteRule,
        halfSessionRule,
        silenceThreeEasyRule,
        fiveAgainStreakRule,
        easyStreakRule,
        pokerLongThinkingRule,
        longThinkingRule,
        happyEasyRule,
        sadAgainRule,
        recoveryRule,
        singleEasyRule,
        singleAgainRule,
        singleGoodRule,
        singleHardRule,
        thinkingRule,
        greetingRule,
        idleRule
    )
}
