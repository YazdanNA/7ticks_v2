package com.example.features.tiki.behavior

import com.example.features.tiki.model.EmotionState

interface BehaviorRule {
    val name: String
    fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult?
}

object BehaviorPriorities {
    const val LOW = 15
    const val NORMAL = 20
    const val MEDIUM = 30
    const val STREAK_MEDIUM = 45
    const val STREAK_HIGH = 55
    const val STREAK_MAX = 65
    const val OVERRIDE_MAX = 100
}

object DefaultBehaviorRules {

    val sessionStarted = object : BehaviorRule {
        override val name = "SessionStartedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.SessionStarted) {
                return BehaviorResult(EmotionState.WELCOME, BehaviorPriorities.OVERRIDE_MAX, name)
            }
            return null
        }
    }

    val sessionFinished = object : BehaviorRule {
        override val name = "SessionFinishedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.SessionFinished) {
                return BehaviorResult(EmotionState.STREAK_FIRE, BehaviorPriorities.OVERRIDE_MAX, name)
            }
            return null
        }
    }

    val fiveEasyStreak = object : BehaviorRule {
        override val name = "FiveEasyStreakRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredEasy) {
                val (streakClass, count) = history.getTrailingAnswerStreak()
                if (streakClass == BehaviorEvent.CardAnsweredEasy::class.java && count >= 5) {
                    return BehaviorResult(EmotionState.ROFL, BehaviorPriorities.STREAK_MAX, name)
                }
            }
            return null
        }
    }

    val threeEasyStreak = object : BehaviorRule {
        override val name = "ThreeEasyStreakRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredEasy) {
                val (streakClass, count) = history.getTrailingAnswerStreak()
                if (streakClass == BehaviorEvent.CardAnsweredEasy::class.java && count in 3..4) {
                    return BehaviorResult(EmotionState.SMILE_HEARTS, BehaviorPriorities.STREAK_HIGH, name)
                }
            }
            return null
        }
    }

    val singleEasy = object : BehaviorRule {
        override val name = "SingleEasyRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredEasy) {
                return BehaviorResult(EmotionState.HAPPY, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val singleGood = object : BehaviorRule {
        override val name = "SingleGoodRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredGood) {
                return BehaviorResult(EmotionState.HAPPY, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val threeAgainStreak = object : BehaviorRule {
        override val name = "ThreeAgainStreakRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredAgain) {
                val (streakClass, count) = history.getTrailingAnswerStreak()
                if (streakClass == BehaviorEvent.CardAnsweredAgain::class.java && count >= 3) {
                    return BehaviorResult(EmotionState.DISAPPOINTED, BehaviorPriorities.STREAK_HIGH, name)
                }
            }
            return null
        }
    }

    val multipleAgainStreak = object : BehaviorRule {
        override val name = "MultipleAgainStreakRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredAgain) {
                val (streakClass, count) = history.getTrailingAnswerStreak()
                if (streakClass == BehaviorEvent.CardAnsweredAgain::class.java && count == 2) {
                    return BehaviorResult(EmotionState.CRY, BehaviorPriorities.STREAK_MEDIUM, name)
                }
            }
            return null
        }
    }

    val singleAgain = object : BehaviorRule {
        override val name = "SingleAgainRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredAgain) {
                return BehaviorResult(EmotionState.SAD, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val singleHard = object : BehaviorRule {
        override val name = "SingleHardRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredHard) {
                return BehaviorResult(EmotionState.THINKING, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val veryLongThinking = object : BehaviorRule {
        override val name = "VeryLongThinkingRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardThinkingFinished && event.durationMillis >= 15000L) {
                return BehaviorResult(EmotionState.POKER, BehaviorPriorities.MEDIUM + 5, name)
            }
            return null
        }
    }

    val longThinking = object : BehaviorRule {
        override val name = "LongThinkingRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardThinkingFinished && event.durationMillis in 8000L..14999L) {
                return BehaviorResult(EmotionState.THINKING, BehaviorPriorities.MEDIUM, name)
            }
            return null
        }
    }

    val translationOpened = object : BehaviorRule {
        override val name = "TranslationOpenedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.TranslationOpened) {
                return BehaviorResult(EmotionState.SMIRK, BehaviorPriorities.LOW, name)
            }
            return null
        }
    }

    val moreDetailsOpened = object : BehaviorRule {
        override val name = "MoreDetailsOpenedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.MoreDetailsOpened) {
                return BehaviorResult(EmotionState.EYEBROW_RAISE, BehaviorPriorities.LOW, name)
            }
            return null
        }
    }

    val all: List<BehaviorRule> = listOf(
        sessionStarted,
        sessionFinished,
        fiveEasyStreak,
        threeEasyStreak,
        singleEasy,
        singleGood,
        threeAgainStreak,
        multipleAgainStreak,
        singleAgain,
        singleHard,
        veryLongThinking,
        longThinking,
        translationOpened,
        moreDetailsOpened
    )
}
