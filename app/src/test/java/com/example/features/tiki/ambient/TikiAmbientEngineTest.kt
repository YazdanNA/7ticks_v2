package com.example.features.tiki.ambient

import org.junit.Assert.*
import org.junit.Test

class TikiAmbientEngineTest {

    @Test
    fun `test idle detection and trigger interval`() {
        // Build engine with customized short interval (1s to 2s)
        val scheduler = AmbientScheduler(1000L, 2000L)
        val engine = AmbientEngine(scheduler = scheduler)
        
        var emittedAction: AmbientAction? = null
        engine.addActionListener { emittedAction = it }

        val startTime = System.currentTimeMillis()
        
        // At start, ticking immediately shouldn't trigger anything as time hasn't passed
        assertNull(engine.tick(startTime))
        assertNull(emittedAction)

        // Advance past the trigger time (2000ms later)
        val triggeredAction = engine.tick(startTime + 2500L)
        assertNotNull(triggeredAction)
        assertNotNull(emittedAction)
        assertEquals(triggeredAction, emittedAction)
    }

    @Test
    fun `test thinking progression states`() {
        val scheduler = AmbientScheduler(10000L, 20000L)
        val detector = IdleDetector()
        val engine = AmbientEngine(scheduler = scheduler, idleDetector = detector)

        val startTime = 1000000000L
        detector.reset(startTime)
        engine.setThinking(true, startTime)

        // 0 to 5 seconds thinking -> THINKING_0_5
        val action1 = engine.tick(startTime + 2000L)
        assertEquals(AmbientAction.THINKING_0_5, action1)

        // 5 to 10 seconds thinking -> THINKING_5_10
        val action2 = engine.tick(startTime + 7000L)
        assertEquals(AmbientAction.THINKING_5_10, action2)

        // 10 to 20 seconds thinking -> THINKING_10_20
        val action3 = engine.tick(startTime + 15000L)
        assertEquals(AmbientAction.THINKING_10_20, action3)

        // 20 to 40 seconds thinking -> THINKING_20_40
        val action4 = engine.tick(startTime + 30000L)
        assertEquals(AmbientAction.THINKING_20_40, action4)

        // 40+ seconds thinking -> THINKING_40_60
        val action5 = engine.tick(startTime + 50000L)
        assertEquals(AmbientAction.THINKING_40_60, action5)
    }

    @Test
    fun `test no idle during speech and priorities`() {
        val scheduler = AmbientScheduler(100L, 200L)
        val engine = AmbientEngine(scheduler = scheduler)

        // Set speaking state to true (Speech priority is highest)
        engine.setSpeaking(true)
        assertTrue(engine.state.value.isSpeaking)
        assertEquals(AmbientPriority.SPEECH, engine.state.value.currentPriority)

        // Ticking even after enough time should yield absolutely nothing because speaking is true
        val startTime = System.currentTimeMillis()
        assertNull(engine.tick(startTime + 1000L))

        // Disable speaking
        engine.setSpeaking(false)
        assertFalse(engine.state.value.isSpeaking)

        // Now ticking after enough time should trigger an idle animation successfully
        assertNotNull(engine.tick(startTime + 2000L))
    }

    @Test
    fun `test pause and resume behavior`() {
        val scheduler = AmbientScheduler(500L, 1000L)
        val engine = AmbientEngine(scheduler = scheduler)

        val startTime = System.currentTimeMillis()

        // Pause the engine
        engine.setPaused(true)
        assertTrue(engine.state.value.isPaused)

        // Advance time, shouldn't trigger anything because it is paused
        assertNull(engine.tick(startTime + 2000L))

        // Resume the engine
        engine.setPaused(false)
        assertFalse(engine.state.value.isPaused)

        // Advance time, should trigger because it has been resumed
        assertNotNull(engine.tick(startTime + 4000L))
    }

    @Test
    fun `test no immediate repeating loops`() {
        val scheduler = AmbientScheduler(10L, 20L)
        val engine = AmbientEngine(scheduler = scheduler)

        val actionsList = mutableListOf<AmbientAction>()
        engine.addActionListener { actionsList.add(it) }

        var tickTime = System.currentTimeMillis()
        for (i in 1..20) {
            tickTime += 100L
            engine.tick(tickTime)
        }

        // Verify that in the action log, no consecutive actions are identical
        for (i in 0 until actionsList.size - 1) {
            assertNotEquals(actionsList[i], actionsList[i + 1])
        }
    }

    @Test
    fun `test reset clears listeners and states`() {
        val engine = AmbientEngine()
        var listenerTriggered = false
        engine.addActionListener { listenerTriggered = true }

        engine.setThinking(true)
        assertTrue(engine.state.value.isThinking)

        engine.reset()
        assertFalse(engine.state.value.isThinking)
        assertEquals(AmbientPriority.IDLE, engine.state.value.currentPriority)

        // Verify listener was cleared during reset
        engine.setThinking(true)
        assertFalse(listenerTriggered)
    }
}
