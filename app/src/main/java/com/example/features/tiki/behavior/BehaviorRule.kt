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

    val easyStreakRule = object : BehaviorRule {
        override val name = "EasyStreakRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredEasy) {
                val (streakClass, count) = history.getTrailingAnswerStreak()
                if (streakClass == BehaviorEvent.CardAnsweredEasy::class.java) {
                    val emotion = when (count) {
                        1 -> EmotionState.HAPPY
                        2 -> EmotionState.WINK
                        3 -> EmotionState.SMILE_HEARTS
                        4 -> EmotionState.KISS
                        5 -> EmotionState.HEART_EYES
                        6 -> EmotionState.LAUGH_TEARS
                        7 -> EmotionState.ROFL
                        else -> {
                            val extraEmotions = listOf(
                                EmotionState.KISS,
                                EmotionState.HEART_EYES,
                                EmotionState.SMILE_HEARTS,
                                EmotionState.ROFL,
                                EmotionState.LAUGH_BIG,
                                EmotionState.TEARS_OF_JOY,
                                EmotionState.SMILE_BIG
                            )
                            extraEmotions[(count - 8) % extraEmotions.size]
                        }
                    }
                    val priority = when (count) {
                        1 -> BehaviorPriorities.NORMAL
                        2 -> BehaviorPriorities.NORMAL + 5
                        3 -> BehaviorPriorities.STREAK_MEDIUM
                        4 -> BehaviorPriorities.STREAK_HIGH
                        5 -> BehaviorPriorities.STREAK_HIGH + 5
                        else -> BehaviorPriorities.STREAK_MAX
                    }
                    return BehaviorResult(emotion, priority, name)
                }
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

    val againStreakRule = object : BehaviorRule {
        override val name = "AgainStreakRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.CardAnsweredAgain) {
                val (streakClass, count) = history.getTrailingAnswerStreak()
                if (streakClass == BehaviorEvent.CardAnsweredAgain::class.java) {
                    val emotion = when (count) {
                        1 -> EmotionState.SAD
                        2 -> EmotionState.DISAPPOINTED
                        3 -> EmotionState.CRY
                        4 -> EmotionState.PLEADING
                        5 -> EmotionState.DIZZY
                        else -> {
                            val sadEmotions = listOf(
                                EmotionState.SAD,
                                EmotionState.DISAPPOINTED,
                                EmotionState.CRY,
                                EmotionState.PLEADING,
                                EmotionState.DIZZY,
                                EmotionState.SWEAT_COLD
                            )
                            sadEmotions[(count - 6) % sadEmotions.size]
                        }
                    }
                    val priority = when (count) {
                        1 -> BehaviorPriorities.NORMAL
                        2 -> BehaviorPriorities.STREAK_MEDIUM
                        3 -> BehaviorPriorities.STREAK_HIGH
                        else -> BehaviorPriorities.STREAK_MAX
                    }
                    return BehaviorResult(emotion, priority, name)
                }
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

    val dictionaryOpenedRule = object : BehaviorRule {
        override val name = "DictionaryOpenedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.DictionaryOpened) {
                return BehaviorResult(EmotionState.WINK, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val analysisOpenedRule = object : BehaviorRule {
        override val name = "AnalysisOpenedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.AnalysisOpened) {
                return BehaviorResult(EmotionState.EYEBROW_RAISE, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val boxesOpenedRule = object : BehaviorRule {
        override val name = "BoxesOpenedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.BoxesOpened) {
                return BehaviorResult(EmotionState.SMILE_SHY, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val profileOpenedRule = object : BehaviorRule {
        override val name = "ProfileOpenedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.ProfileOpened) {
                return BehaviorResult(EmotionState.WELCOME, BehaviorPriorities.NORMAL, name)
            }
            return null
        }
    }

    val wordSearchedRule = object : BehaviorRule {
        override val name = "WordSearchedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.WordSearched) {
                val emotion = if (event.found) EmotionState.LAUGH_BIG else EmotionState.SWEAT_SMILE
                return BehaviorResult(emotion, BehaviorPriorities.MEDIUM, name)
            }
            return null
        }
    }

    val wordStarredRule = object : BehaviorRule {
        override val name = "WordStarredRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.WordStarred) {
                val emotion = if (kotlin.random.Random.nextBoolean()) EmotionState.KISS else EmotionState.HEART_EYES
                return BehaviorResult(emotion, BehaviorPriorities.MEDIUM + 10, name)
            }
            return null
        }
    }

    val onboardingStepChangedRule = object : BehaviorRule {
        override val name = "OnboardingStepChangedRule"
        override fun evaluate(event: BehaviorEvent, history: BehaviorHistory): BehaviorResult? {
            if (event is BehaviorEvent.OnboardingStepChanged) {
                val emotion = when (event.step) {
                    0 -> EmotionState.WELCOME
                    1 -> EmotionState.NAME
                    2 -> EmotionState.HAPPY
                    3 -> EmotionState.NATIVE_LANG
                    4 -> EmotionState.TARGET_LANG
                    5 -> EmotionState.STUDY_TIME
                    6 -> EmotionState.REMIND_TIME
                    7 -> EmotionState.PLACEMENT
                    8 -> EmotionState.LOADING_DATA
                    else -> EmotionState.HAPPY
                }
                return BehaviorResult(emotion, BehaviorPriorities.OVERRIDE_MAX, name)
            }
            return null
        }
    }

    val all: List<BehaviorRule> = listOf(
        sessionStarted,
        sessionFinished,
        easyStreakRule,
        singleGood,
        againStreakRule,
        singleHard,
        veryLongThinking,
        longThinking,
        translationOpened,
        moreDetailsOpened,
        dictionaryOpenedRule,
        analysisOpenedRule,
        boxesOpenedRule,
        profileOpenedRule,
        wordSearchedRule,
        wordStarredRule,
        onboardingStepChangedRule
    )
}
