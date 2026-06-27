package com.example.core.learning

import kotlin.random.Random

/**
 * Phase 1 SevenTicks Companion (Tiki) Behavior System.
 */
enum class CompanionMood(val tikiState: String) {
    HAPPY("st-happy"),
    VERY_HAPPY("st-welcome"),
    CELEBRATION("st-streak-fire"),
    POSITIVE("st-happy"),
    THINKING("st-poker"),
    SAD("st-sad"),
    POKER("st-poker")
}

sealed class CompanionEvent {
    object Easy : CompanionEvent()
    object ThreeEasyStreak : CompanionEvent()
    object FiveEasyStreak : CompanionEvent()
    object Good : CompanionEvent()
    object Hard : CompanionEvent()
    object AgainFirst : CompanionEvent()
    object AgainMultiple : CompanionEvent()
    object Spent30Sec : CompanionEvent()
    object Spent50Sec : CompanionEvent()
    object LevelUp : CompanionEvent()
    object FirstBox7 : CompanionEvent()
}

class CompanionDialogueManager {
    private var lastMessage: String = ""

    // Dialogue Pools
    private val easyPool = listOf("Nice!", "Perfect!", "Too easy!", "Excellent!")
    private val threeEasyPool = listOf("You're on fire!", "Keep going!", "Amazing streak!")
    private val fiveEasyPool = listOf("You're unstoppable!", "Incredible!", "Master mode!")
    private val goodPool = listOf("Well done.", "Nice recall.", "Great job.")
    private val hardPool = listOf("We're close.", "One more review.", "You'll get it.")
    private val againFirstPool = listOf("Oops...", "Let's try again.", "No worries.")
    private val againMultiplePool = listOf("This one is stubborn.", "We'll beat it.", "Needs extra practice.")
    private val spent30SecPool = listOf("Thinking?", "Tough one?", "Take your time.")
    private val spent50SecPool = listOf("Still thinking?", "This one is difficult.")
    private val levelUpPool = listOf("Level up!", "You did it!", "Awesome!")
    private val firstBox7Pool = listOf("First master!", "Excellent!")

    /**
     * Resolves the companion mood based on the event.
     */
    fun resolveMood(event: CompanionEvent): CompanionMood {
        return when (event) {
            is CompanionEvent.Easy -> CompanionMood.HAPPY
            is CompanionEvent.ThreeEasyStreak -> CompanionMood.VERY_HAPPY
            is CompanionEvent.FiveEasyStreak -> CompanionMood.CELEBRATION
            is CompanionEvent.Good -> CompanionMood.POSITIVE
            is CompanionEvent.Hard -> CompanionMood.THINKING
            is CompanionEvent.AgainFirst -> CompanionMood.SAD
            is CompanionEvent.AgainMultiple -> CompanionMood.POKER
            is CompanionEvent.Spent30Sec -> CompanionMood.THINKING
            is CompanionEvent.Spent50Sec -> CompanionMood.POKER
            is CompanionEvent.LevelUp -> CompanionMood.CELEBRATION
            is CompanionEvent.FirstBox7 -> CompanionMood.CELEBRATION
        }
    }

    /**
     * Generates a context-aware message based on the event and mood,
     * ensuring no consecutive duplicates if possible.
     */
    fun generateMessage(event: CompanionEvent): String {
        val pool = when (event) {
            is CompanionEvent.Easy -> easyPool
            is CompanionEvent.ThreeEasyStreak -> threeEasyPool
            is CompanionEvent.FiveEasyStreak -> fiveEasyPool
            is CompanionEvent.Good -> goodPool
            is CompanionEvent.Hard -> hardPool
            is CompanionEvent.AgainFirst -> againFirstPool
            is CompanionEvent.AgainMultiple -> againMultiplePool
            is CompanionEvent.Spent30Sec -> spent30SecPool
            is CompanionEvent.Spent50Sec -> spent50SecPool
            is CompanionEvent.LevelUp -> levelUpPool
            is CompanionEvent.FirstBox7 -> firstBox7Pool
        }

        if (pool.isEmpty()) return ""
        if (pool.size == 1) {
            lastMessage = pool[0]
            return lastMessage
        }

        // Filter out the last message to avoid consecutive duplicates
        val available = pool.filter { it != lastMessage }
        val chosen = if (available.isNotEmpty()) {
            available[Random.nextInt(available.size)]
        } else {
            pool[Random.nextInt(pool.size)]
        }

        lastMessage = chosen
        return chosen
    }
}
