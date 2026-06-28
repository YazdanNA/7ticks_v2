package com.example.features.tiki.context

class ContextState {
    var currentScreen: String = "IDLE"
        private set

    var sessionProgress: Float = 0f
        private set

    var currentCardIndex: Int = 0
        private set

    var totalCardsInSession: Int = 0
        private set

    var remainingCards: Int = 0
        private set

    var sessionStartTimeMillis: Long = 0L
        private set

    var sessionDurationMillis: Long = 0L
        private set

    var thinkingStartTimeMillis: Long = 0L
        private set

    var thinkingDurationMillis: Long = 0L
        private set

    var currentStreak: Int = 0
        private set

    var todayStudyTimeMillis: Long = 0L
        private set

    var todayReviewedCards: Int = 0
        private set

    var masterCountToday: Int = 0
        private set

    var currentCefrLevel: String = "A1"
        private set

    var isTranslationOpened: Boolean = false
        private set

    var isMoreDetailsOpened: Boolean = false
        private set

    var lastEvent: ContextEvent? = null
        private set

    fun onEvent(event: ContextEvent, currentTimeMillis: Long) {
        lastEvent = event
        when (event) {
            is ContextEvent.ApplicationStarted -> {
                currentScreen = "HOME"
            }
            is ContextEvent.ApplicationClosed -> {
                currentScreen = "CLOSED"
            }
            is ContextEvent.SessionStarted -> {
                currentScreen = "SESSION"
                sessionStartTimeMillis = currentTimeMillis
                sessionDurationMillis = 0L
                totalCardsInSession = if (event.totalCards > 0) event.totalCards else 10
                currentCardIndex = 1
                remainingCards = totalCardsInSession - currentCardIndex
                sessionProgress = currentCardIndex.toFloat() / totalCardsInSession.toFloat()
            }
            is ContextEvent.FirstCard -> {
                currentCardIndex = 1
                remainingCards = if (totalCardsInSession > 0) totalCardsInSession - 1 else 0
                sessionProgress = if (totalCardsInSession > 0) 1f / totalCardsInSession else 0f
            }
            is ContextEvent.LastCard -> {
                if (totalCardsInSession > 0) {
                    currentCardIndex = totalCardsInSession
                    remainingCards = 0
                    sessionProgress = 1f
                }
            }
            is ContextEvent.HalfSessionReached -> {
                if (totalCardsInSession > 0) {
                    currentCardIndex = (totalCardsInSession / 2).coerceAtLeast(1)
                    remainingCards = totalCardsInSession - currentCardIndex
                    sessionProgress = currentCardIndex.toFloat() / totalCardsInSession.toFloat()
                }
            }
            is ContextEvent.CardFlipped -> {
                // Observed card flipped
            }
            is ContextEvent.TranslationOpened -> {
                isTranslationOpened = true
            }
            is ContextEvent.TranslationClosed -> {
                isTranslationOpened = false
            }
            is ContextEvent.MoreDetailsOpened -> {
                isMoreDetailsOpened = true
            }
            is ContextEvent.MoreDetailsClosed -> {
                isMoreDetailsOpened = false
            }
            is ContextEvent.ThinkingStarted -> {
                thinkingStartTimeMillis = currentTimeMillis
                thinkingDurationMillis = 0L
            }
            is ContextEvent.ThinkingFinished -> {
                thinkingDurationMillis = event.durationMillis
                if (sessionStartTimeMillis > 0L && currentTimeMillis >= sessionStartTimeMillis) {
                    sessionDurationMillis = currentTimeMillis - sessionStartTimeMillis
                    todayStudyTimeMillis += event.durationMillis
                }
            }
            is ContextEvent.CardAnswered -> {
                todayReviewedCards++
                if (sessionStartTimeMillis > 0L && currentTimeMillis >= sessionStartTimeMillis) {
                    sessionDurationMillis = currentTimeMillis - sessionStartTimeMillis
                }
                if (event.isMastered) {
                    masterCountToday++
                }
                if (totalCardsInSession > 0 && currentCardIndex < totalCardsInSession) {
                    currentCardIndex++
                    remainingCards = totalCardsInSession - currentCardIndex
                    sessionProgress = currentCardIndex.toFloat() / totalCardsInSession.toFloat()
                }
            }
            is ContextEvent.FirstMasterWord -> {
                masterCountToday = (masterCountToday + 1).coerceAtLeast(1)
            }
            is ContextEvent.MasterWord -> {
                masterCountToday++
            }
            is ContextEvent.ReviewQueueEmpty -> {
                remainingCards = 0
                if (totalCardsInSession > 0) {
                    currentCardIndex = totalCardsInSession
                    sessionProgress = 1f
                }
            }
            is ContextEvent.StreakExtended -> {
                currentStreak++
            }
            is ContextEvent.SessionFinished -> {
                currentScreen = "SUMMARY"
                if (sessionStartTimeMillis > 0L && currentTimeMillis >= sessionStartTimeMillis) {
                    sessionDurationMillis = currentTimeMillis - sessionStartTimeMillis
                }
                remainingCards = 0
                sessionProgress = 1f
            }
            is ContextEvent.SessionAbandoned -> {
                currentScreen = "HOME"
                if (sessionStartTimeMillis > 0L && currentTimeMillis >= sessionStartTimeMillis) {
                    sessionDurationMillis = currentTimeMillis - sessionStartTimeMillis
                }
            }
            is ContextEvent.NewWordUnlocked -> {}
            is ContextEvent.LevelCompleted -> {}
            is ContextEvent.AchievementUnlocked -> {}
            is ContextEvent.Custom -> {}
        }
    }

    fun toSnapshot(): ContextSnapshot {
        return ContextSnapshot(
            sessionProgress = sessionProgress,
            currentCardIndex = currentCardIndex,
            remainingCards = remainingCards,
            sessionDurationMillis = sessionDurationMillis,
            thinkingDurationMillis = thinkingDurationMillis,
            currentStreak = currentStreak,
            todayStudyTimeMillis = todayStudyTimeMillis,
            todayReviewedCards = todayReviewedCards,
            masterCountToday = masterCountToday,
            currentCefrLevel = currentCefrLevel,
            isTranslationEnabled = isTranslationOpened,
            isMoreDetailsOpened = isMoreDetailsOpened,
            currentScreen = currentScreen,
            lastEvent = lastEvent
        )
    }

    fun setCefrLevel(level: String) {
        currentCefrLevel = level
    }

    fun reset() {
        currentScreen = "IDLE"
        sessionProgress = 0f
        currentCardIndex = 0
        totalCardsInSession = 0
        remainingCards = 0
        sessionStartTimeMillis = 0L
        sessionDurationMillis = 0L
        thinkingStartTimeMillis = 0L
        thinkingDurationMillis = 0L
        currentStreak = 0
        todayStudyTimeMillis = 0L
        todayReviewedCards = 0
        masterCountToday = 0
        isTranslationOpened = false
        isMoreDetailsOpened = false
        lastEvent = null
    }
}
