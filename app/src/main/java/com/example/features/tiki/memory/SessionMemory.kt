package com.example.features.tiki.memory

import com.example.features.tiki.behavior.BehaviorEvent

class SessionMemory {
    var sessionStartTimeMillis: Long = 0L
        private set

    var cardsAnswered: Int = 0
        private set

    var translationUsageCount: Int = 0
        private set

    var moreDetailsUsageCount: Int = 0
        private set

    var numberOfFlips: Int = 0
        private set

    var currentStreak: Int = 0
        private set

    var currentEasyStreak: Int = 0
        private set

    var longestEasyStreak: Int = 0
        private set

    var currentAgainStreak: Int = 0
        private set

    var longestAgainStreak: Int = 0
        private set

    var totalThinkingTimeMillis: Long = 0L
        private set

    var thinkingFinishedCount: Int = 0
        private set

    val averageThinkingTimeMillis: Long
        get() = if (thinkingFinishedCount > 0) totalThinkingTimeMillis / thinkingFinishedCount else 0L

    fun getSessionElapsedTimeMillis(currentTimeMillis: Long): Long {
        return if (sessionStartTimeMillis > 0L && currentTimeMillis >= sessionStartTimeMillis) {
            currentTimeMillis - sessionStartTimeMillis
        } else {
            0L
        }
    }

    fun startSession(startTimeMillis: Long) {
        reset()
        sessionStartTimeMillis = startTimeMillis
    }

    fun recordEvent(event: BehaviorEvent) {
        when (event) {
            is BehaviorEvent.SessionStarted -> {
                // Handled in startSession
            }
            is BehaviorEvent.CardAnsweredEasy -> {
                cardsAnswered++
                currentEasyStreak++
                if (longestEasyStreak < currentEasyStreak) {
                    longestEasyStreak = currentEasyStreak
                }
                currentAgainStreak = 0
                currentStreak = if (currentStreak < 0) 1 else currentStreak + 1
            }
            is BehaviorEvent.CardAnsweredGood -> {
                cardsAnswered++
                currentEasyStreak = 0
                currentAgainStreak = 0
                currentStreak = if (currentStreak < 0) 1 else currentStreak + 1
            }
            is BehaviorEvent.CardAnsweredHard -> {
                cardsAnswered++
                currentEasyStreak = 0
                currentAgainStreak = 0
                currentStreak = 0
            }
            is BehaviorEvent.CardAnsweredAgain -> {
                cardsAnswered++
                currentEasyStreak = 0
                currentAgainStreak++
                if (longestAgainStreak < currentAgainStreak) {
                    longestAgainStreak = currentAgainStreak
                }
                currentStreak = if (currentStreak > 0) -1 else currentStreak - 1
            }
            is BehaviorEvent.CardFlipped -> {
                numberOfFlips++
            }
            is BehaviorEvent.CardThinkingFinished -> {
                totalThinkingTimeMillis += event.durationMillis
                thinkingFinishedCount++
            }
            is BehaviorEvent.TranslationOpened -> {
                translationUsageCount++
            }
            is BehaviorEvent.MoreDetailsOpened -> {
                moreDetailsUsageCount++
            }
            else -> {}
        }
    }

    fun reset() {
        sessionStartTimeMillis = 0L
        cardsAnswered = 0
        translationUsageCount = 0
        moreDetailsUsageCount = 0
        numberOfFlips = 0
        currentStreak = 0
        currentEasyStreak = 0
        longestEasyStreak = 0
        currentAgainStreak = 0
        longestAgainStreak = 0
        totalThinkingTimeMillis = 0L
        thinkingFinishedCount = 0
    }
}
