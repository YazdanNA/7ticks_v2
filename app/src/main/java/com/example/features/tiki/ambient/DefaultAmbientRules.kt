package com.example.features.tiki.ambient

object DefaultAmbientRules {

    val thinkingRule = object : AmbientRule {
        override val ruleName = "ThinkingProgressionRule"
        override val priority = AmbientPriority.AMBIENT_ANIMATION

        override fun evaluate(state: AmbientState): AmbientAction? {
            if (!state.isThinking || state.isSpeaking || state.isPaused) return null
            
            val duration = state.thinkingDurationMillis
            return when {
                duration < 5000L -> AmbientAction.THINKING_0_5
                duration < 10000L -> AmbientAction.THINKING_5_10
                duration < 20000L -> AmbientAction.THINKING_10_20
                duration < 40000L -> AmbientAction.THINKING_20_40
                else -> AmbientAction.THINKING_40_60
            }
        }
    }

    val idleAnimationRule = object : AmbientRule {
        override val ruleName = "IdleAnimationRule"
        override val priority = AmbientPriority.IDLE

        // Allowed passive actions as specified in the instructions
        private val allowedIdles = listOf(
            AmbientAction.SMILE,
            AmbientAction.WINK,
            AmbientAction.LOOK_UP,
            AmbientAction.YAWN,
            AmbientAction.SLEEPY_EYES,
            AmbientAction.LOOK_SIDEWAYS,
            AmbientAction.SMALL_NERVOUS_SMILE,
            AmbientAction.NEUTRAL_FACE,
            AmbientAction.HAPPY_EYES,
            AmbientAction.CURIOUS_FACE,
            AmbientAction.BLINK,
            AmbientAction.LOOK_AROUND,
            AmbientAction.STRETCH,
            AmbientAction.SMALL_LAUGH,
            AmbientAction.TINY_BOUNCE
        )

        override fun evaluate(state: AmbientState): AmbientAction? {
            if (state.isSpeaking || state.isPaused || state.isThinking) return null
            if (state.currentPriority.value > AmbientPriority.AMBIENT_ANIMATION.value) return null

            // Filter out recent actions to prevent loops and direct repeats
            val availableIdles = allowedIdles.filter { it !in state.recentActions }
            
            // Fallback to all allowed if somehow everything was filtered
            val candidates = if (availableIdles.isNotEmpty()) availableIdles else allowedIdles
            
            // Return a random selection to ensure irregular/unpredictable behavior
            return candidates.randomOrNull()
        }
    }

    val all: List<AmbientRule> = listOf(
        thinkingRule,
        idleAnimationRule
    )
}
