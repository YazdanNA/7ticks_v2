package com.example.features.tiki.personality

import com.example.features.tiki.dialogue.DialogueCategory
import com.example.features.tiki.model.EmotionState

object DefaultPersonalityRules {

    val toneReplacementRule = object : PersonalityRule {
        override val name = "ToneReplacementRule"
        override fun appliesTo(context: PersonalityContext): Boolean = true
        override fun modify(context: PersonalityContext): List<String> {
            val list = mutableListOf<String>()
            when (context.baseDialogueText) {
                "Wrong." -> list.add("We'll get it.")
                "You forgot again." -> list.add("One more try.")
                "Excellent." -> list.add("You're getting stronger.")
            }
            return list
        }
    }

    val happyEnergeticRule = object : PersonalityRule {
        override val name = "HappyEnergeticRule"
        override fun appliesTo(context: PersonalityContext): Boolean {
            return context.emotion == EmotionState.HAPPY ||
                   context.emotion == EmotionState.SMILE_BIG ||
                   context.emotion == EmotionState.ROFL ||
                   context.emotion == EmotionState.SMILE_HEARTS
        }
        override fun modify(context: PersonalityContext): List<String> {
            if (context.category is DialogueCategory.Easy ||
                context.category is DialogueCategory.Good ||
                context.category is DialogueCategory.Celebration ||
                context.category is DialogueCategory.EasyStreak) {
                return listOf("Nice!", "Great!", "Well done!", "Good one!", "Beautiful!", "You're getting stronger.")
            }
            return emptyList()
        }
    }

    val sadGentleRule = object : PersonalityRule {
        override val name = "SadGentleRule"
        override fun appliesTo(context: PersonalityContext): Boolean {
            return context.emotion == EmotionState.SAD ||
                   context.emotion == EmotionState.CRY ||
                   context.emotion == EmotionState.DISAPPOINTED
        }
        override fun modify(context: PersonalityContext): List<String> {
            if (context.category is DialogueCategory.Again ||
                context.category is DialogueCategory.Failure ||
                context.category is DialogueCategory.AgainStreak) {
                return listOf("We'll get it.", "One more try.", "We can do this.", "Step by step.")
            }
            return emptyList()
        }
    }

    val thinkingCuriousRule = object : PersonalityRule {
        override val name = "ThinkingCuriousRule"
        override fun appliesTo(context: PersonalityContext): Boolean {
            return context.emotion == EmotionState.THINKING ||
                   context.emotion == EmotionState.EYEBROW_RAISE
        }
        override fun modify(context: PersonalityContext): List<String> {
            if (context.category is DialogueCategory.Thinking ||
                context.category is DialogueCategory.LongThinking ||
                context.category is DialogueCategory.Hard) {
                return listOf("Curious card...", "Let's discover...", "Interesting card.")
            }
            return emptyList()
        }
    }

    val pokerNeutralRule = object : PersonalityRule {
        override val name = "PokerNeutralRule"
        override fun appliesTo(context: PersonalityContext): Boolean {
            return context.emotion == EmotionState.POKER
        }
        override fun modify(context: PersonalityContext): List<String> {
            return listOf("Calm and steady.", "Pure logic.", "Zero panic.")
        }
    }

    val all: List<PersonalityRule> = listOf(
        toneReplacementRule,
        happyEnergeticRule,
        sadGentleRule,
        thinkingCuriousRule,
        pokerNeutralRule
    )
}
