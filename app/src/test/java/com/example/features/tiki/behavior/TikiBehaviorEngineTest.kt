package com.example.features.tiki.behavior

import com.example.features.tiki.model.EmotionState
import org.junit.Assert.*
import org.junit.Test

class TikiBehaviorEngineTest {

    @Test
    fun `test single easy triggers happy`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)
        val result = engine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)

        assertNotNull(result)
        assertEquals(EmotionState.HAPPY, result?.emotion)
        assertEquals("SingleEasyRule", result?.ruleName)
    }

    @Test
    fun `test single again triggers sad`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)
        val result = engine.processEvent(BehaviorEvent.CardAnsweredAgain, 1000L)

        assertNotNull(result)
        assertEquals(EmotionState.SAD, result?.emotion)
        assertEquals("SingleAgainRule", result?.ruleName)
    }

    @Test
    fun `test again streak triggers cry then disappointed`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)

        // 1st Again
        var result = engine.processEvent(BehaviorEvent.CardAnsweredAgain, 1000L)
        assertEquals(EmotionState.SAD, result?.emotion)

        // 2nd Again (after cooldown)
        result = engine.processEvent(BehaviorEvent.CardAnsweredAgain, 5000L)
        assertEquals(EmotionState.CRY, result?.emotion)

        // 3rd Again (after cooldown)
        result = engine.processEvent(BehaviorEvent.CardAnsweredAgain, 10000L)
        assertEquals(EmotionState.DISAPPOINTED, result?.emotion)
    }

    @Test
    fun `test easy streak triggers smile_hearts then rofl`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)

        engine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)
        engine.processEvent(BehaviorEvent.CardAnsweredEasy, 5000L)

        // 3rd Easy
        var result = engine.processEvent(BehaviorEvent.CardAnsweredEasy, 10000L)
        assertEquals(EmotionState.SMILE_HEARTS, result?.emotion)

        engine.processEvent(BehaviorEvent.CardAnsweredEasy, 15000L)

        // 5th Easy
        result = engine.processEvent(BehaviorEvent.CardAnsweredEasy, 20000L)
        assertEquals(EmotionState.ROFL, result?.emotion)
    }

    @Test
    fun `test long thinking time triggers thinking and poker`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)

        // 10 seconds thinking
        var result = engine.processEvent(BehaviorEvent.CardThinkingFinished(10000L), 1000L)
        assertEquals(EmotionState.THINKING, result?.emotion)

        // 20 seconds thinking (after cooldown)
        result = engine.processEvent(BehaviorEvent.CardThinkingFinished(20000L), 6000L)
        assertEquals(EmotionState.POKER, result?.emotion)
    }

    @Test
    fun `test session finished triggers proud streak_fire`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)
        val result = engine.processEvent(BehaviorEvent.SessionFinished, 1000L)

        assertEquals(EmotionState.STREAK_FIRE, result?.emotion)
    }

    @Test
    fun `test cooldown prevents flickering on equal or lower priority`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)

        // Trigger Easy at T=1000 (Priority 20)
        val r1 = engine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)
        assertNotNull(r1)

        // Trigger Good at T=2000 (within 3s cooldown, Priority 20 <= 20)
        val r2 = engine.processEvent(BehaviorEvent.CardAnsweredGood, 2000L)
        assertNull("Should be blocked by active cooldown", r2)

        // Trigger Easy at T=5000 (cooldown expired)
        val r3 = engine.processEvent(BehaviorEvent.CardAnsweredEasy, 5000L)
        assertNotNull("Should succeed after cooldown", r3)
    }

    @Test
    fun `test priority override bypasses active cooldown`() {
        val engine = BehaviorEngine(cooldownMillis = 3000L)

        // Normal Easy at T=1000 (Priority 20)
        val r1 = engine.processEvent(BehaviorEvent.CardAnsweredEasy, 1000L)
        assertEquals(BehaviorPriorities.NORMAL, r1?.priority)

        // SessionFinished at T=1500 (within 3s cooldown, but Priority 100 > 20)
        val r2 = engine.processEvent(BehaviorEvent.SessionFinished, 1500L)
        assertNotNull("Priority 100 should override Priority 20 despite cooldown", r2)
        assertEquals(EmotionState.STREAK_FIRE, r2?.emotion)
    }

    @Test
    fun `test history updates correctly and caps at 20 events`() {
        val engine = BehaviorEngine()

        for (i in 1..25) {
            engine.processEvent(BehaviorEvent.CardFlipped, i * 100L)
        }

        assertEquals(20, engine.history.events.size)
        // Verify oldest event timestamp is 600L (since 100..500 were evicted)
        assertEquals(600L, engine.history.events.first().timestampMillis)
        assertEquals(2500L, engine.history.events.last().timestampMillis)
    }
}
