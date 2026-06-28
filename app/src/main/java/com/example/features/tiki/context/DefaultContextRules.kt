package com.example.features.tiki.context

object DefaultContextRules {

    val firstMasterTodayRule = object : ContextRule {
        override val ruleName = "FirstMasterTodayRule"
        override val priority = 150 // Overrides normal dialogue

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.FirstMasterWord || (event is ContextEvent.CardAnswered && event.isMastered && snapshot.masterCountToday == 1)) {
                return ContextRecommendation(
                    reactionName = "ProudReaction",
                    priority = priority,
                    suggestedDialogueCategory = "Celebration"
                )
            }
            return null
        }
    }

    val reviewQueueEmptyRule = object : ContextRule {
        override val ruleName = "ReviewQueueEmptyRule"
        override val priority = 140

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.ReviewQueueEmpty || (event is ContextEvent.CardAnswered && snapshot.remainingCards == 0)) {
                return ContextRecommendation(
                    reactionName = "CelebrationReaction",
                    priority = priority,
                    suggestedDialogueCategory = "Celebration"
                )
            }
            return null
        }
    }

    val sessionFinishedRule = object : ContextRule {
        override val ruleName = "SessionFinishedRule"
        override val priority = 130 // Overrides Good/Easy

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.SessionFinished) {
                return ContextRecommendation(
                    reactionName = "FinishMotivationReaction",
                    priority = priority,
                    suggestedDialogueCategory = "SessionComplete"
                )
            }
            return null
        }
    }

    val lastCardRule = object : ContextRule {
        override val ruleName = "LastCardRule"
        override val priority = 120

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.LastCard || (snapshot.remainingCards == 0 && snapshot.sessionProgress >= 1f && event !is ContextEvent.SessionFinished)) {
                return ContextRecommendation(
                    reactionName = "FinishMotivationReaction",
                    priority = priority,
                    suggestedDialogueCategory = "Motivation"
                )
            }
            return null
        }
    }

    val firstCardRule = object : ContextRule {
        override val ruleName = "FirstCardRule"
        override val priority = 110

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.FirstCard || event is ContextEvent.SessionStarted) {
                return ContextRecommendation(
                    reactionName = "GreetingReaction",
                    priority = priority,
                    suggestedDialogueCategory = "Greeting"
                )
            }
            return null
        }
    }

    val halfSessionRule = object : ContextRule {
        override val ruleName = "HalfSessionRule"
        override val priority = 100

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.HalfSessionReached || (snapshot.sessionProgress in 0.48f..0.52f && event is ContextEvent.CardAnswered)) {
                return ContextRecommendation(
                    reactionName = "EncouragementReaction",
                    priority = priority,
                    suggestedDialogueCategory = "Encouragement"
                )
            }
            return null
        }
    }

    val sessionAbandonedRule = object : ContextRule {
        override val ruleName = "SessionAbandonedRule"
        override val priority = 90

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            if (event is ContextEvent.SessionAbandoned) {
                return ContextRecommendation(
                    reactionName = "NextTimeReaction",
                    priority = priority,
                    suggestedDialogueCategory = "Reminder"
                )
            }
            return null
        }
    }

    val longThinkingRule = object : ContextRule {
        override val ruleName = "LongThinkingRule"
        override val priority = 80

        override fun evaluate(event: ContextEvent, snapshot: ContextSnapshot): ContextRecommendation? {
            val isLongThinking = event is ContextEvent.ThinkingFinished && event.durationMillis >= 8000L || snapshot.thinkingDurationMillis >= 8000L
            if (isLongThinking) {
                return ContextRecommendation(
                    reactionName = "ThinkingReaction",
                    priority = priority,
                    suggestedDialogueCategory = "LongThinking"
                )
            }
            return null
        }
    }

    val all: List<ContextRule> = listOf(
        firstMasterTodayRule,
        reviewQueueEmptyRule,
        sessionFinishedRule,
        lastCardRule,
        firstCardRule,
        halfSessionRule,
        sessionAbandonedRule,
        longThinkingRule
    )
}
