package com.example.features.tiki.director

import com.example.features.tiki.model.EmotionState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class TikiDirectorEngineTest {

    @Test
    fun `test priority selection and conflict resolution`() {
        val engine = DirectorEngine()

        // Test simultaneous events
        val list = listOf(
            Pair(DirectorDecision.ShowEmotion(EmotionState.HAPPY), DirectorPriority.BEHAVIOR_REACTION),
            Pair(DirectorDecision.PlayCelebration("Confetti"), DirectorPriority.CELEBRATION),
            Pair(DirectorDecision.PlayIdleAnimation("Yawn"), DirectorPriority.AMBIENT)
        )

        // Celebration (Priority 90) must defeat Happy (Priority 60) and Ambient (Priority 10)
        val decision = engine.submitSimultaneousEvents(list, System.currentTimeMillis())
        assertNotNull(decision)
        assertTrue(decision is DirectorDecision.PlayCelebration)
        assertEquals("Confetti", (decision as DirectorDecision.PlayCelebration).celebrationName)

        val state = engine.state.value
        assertEquals(DirectorPriority.CELEBRATION.value, state.activePriority)
    }

    @Test
    fun `test cooldown prevents low priority spam but allows high priority`() {
        val engine = DirectorEngine()
        val startTime = 1000000L

        // Low priority behavior reaction (+4000ms cooldown duration)
        val firstDecision = engine.submitEvent(
            DirectorDecision.ShowEmotion(EmotionState.HAPPY),
            DirectorPriority.BEHAVIOR_REACTION,
            startTime
        )
        assertNotNull(firstDecision)
        assertEquals(startTime + 4000L, engine.state.value.cooldownExpirationMillis)

        // Submit low priority during active cooldown -> must be discarded
        val spamDecision = engine.submitEvent(
            DirectorDecision.ShowEmotion(EmotionState.CURIOUS),
            DirectorPriority.BEHAVIOR_REACTION,
            startTime + 1000L
        )
        assertNull(spamDecision)

        // Submit high priority (Celebration) during active cooldown -> must bypass and succeed
        val highPriorityDecision = engine.submitEvent(
            DirectorDecision.PlayCelebration("Dance"),
            DirectorPriority.CELEBRATION,
            startTime + 1000L
        )
        assertNotNull(highPriorityDecision)
        assertEquals(DirectorPriority.CELEBRATION.value, engine.state.value.activePriority)
    }

    @Test
    fun `test preemption and cancellation`() {
        val engine = DirectorEngine()

        // Set low priority speaking or animation active
        engine.submitEvent(
            DirectorDecision.ShowEmotion(EmotionState.HAPPY),
            DirectorPriority.BEHAVIOR_REACTION
        )
        assertEquals(DirectorPriority.BEHAVIOR_REACTION.value, engine.state.value.activePriority)

        // Preempt with a higher priority speech event -> clears lower priority queue/scheduler and takes over
        val preemptDecision = engine.submitEvent(
            DirectorDecision.PlayDialogue("Hello World!"),
            DirectorPriority.SPEECH
        )
        assertNotNull(preemptDecision)
        assertEquals(DirectorPriority.SPEECH.value, engine.state.value.activePriority)
        assertTrue(engine.state.value.isSpeaking)
    }

    @Test
    fun `test delayed decision and sequencing`() = runTest {
        val testDispatcher = StandardTestDispatcher(testScheduler)
        val testScope = TestScope(testDispatcher)
        val scheduler = DirectorScheduler(testScope)
        val engine = DirectorEngine(scheduler = scheduler)

        val delayedDecision = DirectorDecision.DelayedDecision(
            delayMillis = 500L,
            nextDecision = DirectorDecision.SpeakAndShowEmotion("Delayed hello", EmotionState.HAPPY)
        )

        val result = engine.submitEvent(delayedDecision, DirectorPriority.SPEECH)
        assertNotNull(result)
        assertEquals(delayedDecision, engine.state.value.lastDecision)

        // Wait for coroutine delay to finish
        testScope.advanceTimeBy(600L)

        // The inner SpeakAndShowEmotion decision should have executed
        val finalState = engine.state.value
        assertTrue(finalState.lastDecision is DirectorDecision.SpeakAndShowEmotion)
        assertEquals("Delayed hello", (finalState.lastDecision as DirectorDecision.SpeakAndShowEmotion).text)
        assertEquals(EmotionState.HAPPY, finalState.currentEmotion)
    }

    @Test
    fun `test natural silence chance`() {
        val customRules = DirectorRules
        // Low priority (AMBIENT / BEHAVIOR_REACTION) triggers refinement with silence chance
        val decision = DirectorDecision.ShowEmotion(EmotionState.SAD)
        
        // Force the seed of random to hit silence
        val refinedToSilence = customRules.refineDecision(
            decision, 
            DirectorPriority.AMBIENT, 
            Random(12345L) // Deterministic random seed that falls below the silence threshold
        )
        assertEquals(DirectorDecision.RemainSilent, refinedToSilence)
    }

    @Test
    fun `test cancel current and reset`() {
        val engine = DirectorEngine()
        engine.submitEvent(DirectorDecision.PlayDialogue("Tiki talking"), DirectorPriority.SPEECH)
        assertTrue(engine.state.value.isSpeaking)

        engine.cancelCurrent()
        assertFalse(engine.state.value.isSpeaking)
        assertEquals(0, engine.state.value.activePriority)

        engine.reset()
        assertEquals(EmotionState.POKER, engine.state.value.currentEmotion)
        assertEquals(0, engine.state.value.activePriority)
    }
}
