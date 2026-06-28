package com.example.features.tiki.memory

import com.example.features.tiki.behavior.BehaviorEvent

object MemoryAnalyzer {

    fun analyze(
        store: MemoryStore,
        session: SessionMemory,
        currentTimeMillis: Long = System.currentTimeMillis()
    ): MemorySnapshot {
        val events = store.getRecentEvents()

        var recentAgain = 0
        var recentHard = 0
        var recentGood = 0
        var recentEasy = 0

        for (event in events) {
            when (event) {
                is BehaviorEvent.CardAnsweredAgain -> recentAgain++
                is BehaviorEvent.CardAnsweredHard -> recentHard++
                is BehaviorEvent.CardAnsweredGood -> recentGood++
                is BehaviorEvent.CardAnsweredEasy -> recentEasy++
                else -> {}
            }
        }

        val mood = calculateMood(
            currentEasyStreak = session.currentEasyStreak,
            currentAgainStreak = session.currentAgainStreak,
            recentEasy = recentEasy,
            recentGood = recentGood,
            recentHard = recentHard,
            recentAgain = recentAgain,
            averageThinkingTime = session.averageThinkingTimeMillis,
            translationCount = session.translationUsageCount,
            moreDetailsCount = session.moreDetailsUsageCount,
            events = events
        )

        return MemorySnapshot(
            recentAgainCount = recentAgain,
            recentHardCount = recentHard,
            recentGoodCount = recentGood,
            recentEasyCount = recentEasy,
            currentStreak = session.currentStreak,
            currentEasyStreak = session.currentEasyStreak,
            longestEasyStreak = session.longestEasyStreak,
            currentAgainStreak = session.currentAgainStreak,
            longestAgainStreak = session.longestAgainStreak,
            averageThinkingTimeMillis = session.averageThinkingTimeMillis,
            cardsAnswered = session.cardsAnswered,
            sessionElapsedTimeMillis = session.getSessionElapsedTimeMillis(currentTimeMillis),
            translationUsageCount = session.translationUsageCount,
            moreDetailsUsageCount = session.moreDetailsUsageCount,
            numberOfFlips = session.numberOfFlips,
            totalEventsInStore = store.size,
            learningMood = mood
        )
    }

    private fun calculateMood(
        currentEasyStreak: Int,
        currentAgainStreak: Int,
        recentEasy: Int,
        recentGood: Int,
        recentHard: Int,
        recentAgain: Int,
        averageThinkingTime: Long,
        translationCount: Int,
        moreDetailsCount: Int,
        events: List<BehaviorEvent>
    ): LearningMood {
        // 1. Excellent Progress: Easy streak >= 5 or (recent answers mostly Easy)
        if (currentEasyStreak >= 5 || (recentEasy >= 5 && recentAgain == 0)) {
            return LearningMood.EXCELLENT_PROGRESS
        }

        // 2. Needs Encouragement: Again streak >= 3 or recentAgain >= 4
        if (currentAgainStreak >= 3 || recentAgain >= 4) {
            return LearningMood.NEEDS_ENCOURAGEMENT
        }

        // 3. Struggling: Many long thinking times + recentAgain / recentHard high
        val longThinkingCount = events.count { it is BehaviorEvent.CardThinkingFinished && it.durationMillis >= 8000L }
        if (longThinkingCount >= 2 && (recentAgain >= 2 || recentHard >= 2)) {
            return LearningMood.STRUGGLING
        }

        // 4. Focused: Many long thinking times + recentGood / recentEasy high (or low again)
        if (longThinkingCount >= 2 && recentAgain == 0 && (recentGood > 0 || recentEasy > 0)) {
            return LearningMood.FOCUSED
        }

        // 5. Recovering: Had Again recently (in store), but current streak is positive (consecutive Easy/Good >= 2)
        if (recentAgain > 0 && currentAgainStreak == 0 && (recentEasy + recentGood >= recentAgain)) {
            val lastAnswerIsPositive = events.lastOrNull { 
                it is BehaviorEvent.CardAnsweredEasy || it is BehaviorEvent.CardAnsweredGood || it is BehaviorEvent.CardAnsweredAgain
            }?.let { it is BehaviorEvent.CardAnsweredEasy || it is BehaviorEvent.CardAnsweredGood } ?: false

            if (lastAnswerIsPositive && (recentEasy + recentGood >= 2)) {
                return LearningMood.RECOVERING
            }
        }

        // 6. Distracted: Frequent translation or more details opened relative to answers
        if (translationCount + moreDetailsCount >= 5 && (recentAgain + recentHard >= recentEasy + recentGood)) {
            return LearningMood.DISTRACTED
        }

        // 7. Confident: Good/Easy dominate without long thinking or agains
        if (recentEasy + recentGood >= 3 && recentAgain == 0) {
            return LearningMood.CONFIDENT
        }

        // Default fallback based on ratio
        return if (recentAgain > recentEasy + recentGood) {
            LearningMood.STRUGGLING
        } else {
            LearningMood.CONFIDENT
        }
    }
}
