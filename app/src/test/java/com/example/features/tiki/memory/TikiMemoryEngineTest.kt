package com.example.features.tiki.memory

import com.example.features.tiki.behavior.BehaviorEngine
import com.example.features.tiki.behavior.BehaviorEvent
import org.junit.Assert.*
import org.junit.Test

class TikiMemoryEngineTest {

    @Test
    fun `test memory resets correctly`() {
        val memoryEngine = MemoryEngine()
        memoryEngine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)
        assertEquals(1, memoryEngine.store.size)
        assertEquals(1, memoryEngine.session.cardsAnswered)

        memoryEngine.reset()
        assertEquals(0, memoryEngine.store.size)
        assertEquals(0, memoryEngine.session.cardsAnswered)
    }

    @Test
    fun `test max capacity 50 and old events discarded`() {
        val memoryEngine = MemoryEngine()
        for (i in 1..60) {
            memoryEngine.processEvent(BehaviorEvent.CardFlipped, i * 100L)
        }

        assertEquals(50, memoryEngine.store.size)
        assertEquals(1100L, memoryEngine.store.events.first().timestampMillis)
        assertEquals(6000L, memoryEngine.store.events.last().timestampMillis)
    }

    @Test
    fun `test easy streak`() {
        val memoryEngine = MemoryEngine()
        for (i in 1..5) {
            memoryEngine.processEvent(BehaviorEvent.CardAnsweredEasy, i * 1000L)
        }

        val snapshot = memoryEngine.getSnapshot()
        assertEquals(5, snapshot.currentEasyStreak)
        assertEquals(5, snapshot.longestEasyStreak)
        assertEquals(LearningMood.EXCELLENT_PROGRESS, snapshot.learningMood)
    }

    @Test
    fun `test again streak`() {
        val memoryEngine = MemoryEngine()
        for (i in 1..3) {
            memoryEngine.processEvent(BehaviorEvent.CardAnsweredAgain, i * 1000L)
        }

        val snapshot = memoryEngine.getSnapshot()
        assertEquals(3, snapshot.currentAgainStreak)
        assertEquals(3, snapshot.longestAgainStreak)
        assertEquals(LearningMood.NEEDS_ENCOURAGEMENT, snapshot.learningMood)
    }

    @Test
    fun `test mood calculation`() {
        val memoryEngine = MemoryEngine()

        // Confident
        memoryEngine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)
        memoryEngine.processEvent(BehaviorEvent.CardAnsweredGood, 2000L)
        memoryEngine.processEvent(BehaviorEvent.CardAnsweredEasy, 3000L)
        assertEquals(LearningMood.CONFIDENT, memoryEngine.getSnapshot().learningMood)

        // Struggling
        val strugglingEngine = MemoryEngine()
        strugglingEngine.processEvent(BehaviorEvent.CardThinkingFinished(10000L), 1000L)
        strugglingEngine.processEvent(BehaviorEvent.CardAnsweredAgain, 2000L)
        strugglingEngine.processEvent(BehaviorEvent.CardThinkingFinished(9000L), 3000L)
        strugglingEngine.processEvent(BehaviorEvent.CardAnsweredHard, 4000L)
        assertEquals(LearningMood.STRUGGLING, strugglingEngine.getSnapshot().learningMood)

        // Focused
        val focusedEngine = MemoryEngine()
        focusedEngine.processEvent(BehaviorEvent.CardThinkingFinished(10000L), 1000L)
        focusedEngine.processEvent(BehaviorEvent.CardAnsweredGood, 2000L)
        focusedEngine.processEvent(BehaviorEvent.CardThinkingFinished(12000L), 3000L)
        focusedEngine.processEvent(BehaviorEvent.CardAnsweredEasy, 4000L)
        assertEquals(LearningMood.FOCUSED, focusedEngine.getSnapshot().learningMood)

        // Distracted
        val distractedEngine = MemoryEngine()
        for (i in 1..3) distractedEngine.processEvent(BehaviorEvent.TranslationOpened, i * 100L)
        for (i in 1..3) distractedEngine.processEvent(BehaviorEvent.MoreDetailsOpened, i * 200L)
        distractedEngine.processEvent(BehaviorEvent.CardAnsweredAgain, 1000L)
        distractedEngine.processEvent(BehaviorEvent.CardAnsweredHard, 2000L)
        assertEquals(LearningMood.DISTRACTED, distractedEngine.getSnapshot().learningMood)
    }

    @Test
    fun `test average thinking time`() {
        val memoryEngine = MemoryEngine()
        memoryEngine.processEvent(BehaviorEvent.CardThinkingFinished(10000L), 1000L)
        memoryEngine.processEvent(BehaviorEvent.CardThinkingFinished(20000L), 2000L)

        val snapshot = memoryEngine.getSnapshot()
        assertEquals(15000L, snapshot.averageThinkingTimeMillis)
    }

    @Test
    fun `test session statistics`() {
        val memoryEngine = MemoryEngine()
        memoryEngine.processEvent(BehaviorEvent.SessionStarted, 1000L)
        memoryEngine.processEvent(BehaviorEvent.CardFlipped, 2000L)
        memoryEngine.processEvent(BehaviorEvent.CardFlipped, 3000L)
        memoryEngine.processEvent(BehaviorEvent.TranslationOpened, 4000L)
        memoryEngine.processEvent(BehaviorEvent.MoreDetailsOpened, 5000L)
        memoryEngine.processEvent(BehaviorEvent.CardAnsweredGood, 6000L)

        val snapshot = memoryEngine.getSnapshot(10000L)
        assertEquals(1, snapshot.cardsAnswered)
        assertEquals(2, snapshot.numberOfFlips)
        assertEquals(1, snapshot.translationUsageCount)
        assertEquals(1, snapshot.moreDetailsUsageCount)
        assertEquals(9000L, snapshot.sessionElapsedTimeMillis)
    }

    @Test
    fun `test integration with behavior engine`() {
        val memoryEngine = MemoryEngine()
        val behaviorEngine = BehaviorEngine(memoryEngine = memoryEngine)

        behaviorEngine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)
        behaviorEngine.processEvent(BehaviorEvent.CardFlipped, 2000L)

        val snapshot = memoryEngine.getSnapshot()
        assertEquals(1, snapshot.cardsAnswered)
        assertEquals(1, snapshot.numberOfFlips)
        assertEquals(2, snapshot.totalEventsInStore)

        behaviorEngine.processEvent(BehaviorEvent.SessionFinished, 3000L)
        assertEquals(0, memoryEngine.store.size)
        assertEquals(0, memoryEngine.session.cardsAnswered)
    }
}
