package com.example.features.tiki.context

import org.junit.Assert.*
import org.junit.Test

class TikiContextEngineTest {

    @Test
    fun `test first session and first card`() {
        val engine = ContextEngine()
        var observedEvent: ContextEvent? = null
        var observedSnapshot: ContextSnapshot? = null

        engine.addObserver { event, snapshot, _ ->
            observedEvent = event
            observedSnapshot = snapshot
        }

        val rec = engine.onEvent(ContextEvent.SessionStarted(10), 1000L)
        assertNotNull(rec)
        assertEquals("GreetingReaction", rec?.reactionName)
        assertEquals("Greeting", rec?.suggestedDialogueCategory)
        assertEquals(ContextEvent.SessionStarted(10), observedEvent)
        assertEquals(1, observedSnapshot?.currentCardIndex)
        assertEquals(9, observedSnapshot?.remainingCards)
        assertEquals("SESSION", observedSnapshot?.currentScreen)
    }

    @Test
    fun `test half session`() {
        val engine = ContextEngine()
        engine.onEvent(ContextEvent.SessionStarted(10), 1000L)

        // Answer cards up to half session (index 5)
        engine.onEvent(ContextEvent.CardAnswered(), 2000L) // index 2
        engine.onEvent(ContextEvent.CardAnswered(), 3000L) // index 3
        engine.onEvent(ContextEvent.CardAnswered(), 4000L) // index 4

        val rec = engine.onEvent(ContextEvent.HalfSessionReached, 5000L)
        assertNotNull(rec)
        assertEquals("EncouragementReaction", rec?.reactionName)
        assertEquals(0.5f, engine.getSnapshot().sessionProgress, 0.01f)
    }

    @Test
    fun `test last card and review queue empty`() {
        val engine = ContextEngine()
        engine.onEvent(ContextEvent.SessionStarted(10), 1000L)

        val lastCardRec = engine.onEvent(ContextEvent.LastCard, 2000L)
        assertNotNull(lastCardRec)
        assertEquals("FinishMotivationReaction", lastCardRec?.reactionName)

        val emptyQueueRec = engine.onEvent(ContextEvent.ReviewQueueEmpty, 3000L)
        assertNotNull(emptyQueueRec)
        assertEquals("CelebrationReaction", emptyQueueRec?.reactionName)
        assertEquals(0, engine.getSnapshot().remainingCards)
    }

    @Test
    fun `test master word and priority override`() {
        val engine = ContextEngine()
        engine.onEvent(ContextEvent.SessionStarted(10), 1000L)

        // FirstMasterWord (priority 150) overrides FirstCard/Normal events
        val masterRec = engine.onEvent(ContextEvent.FirstMasterWord, 2000L)
        assertNotNull(masterRec)
        assertEquals("ProudReaction", masterRec?.reactionName)
        assertEquals(1, engine.getSnapshot().masterCountToday)

        // Subsequent MasterWord
        engine.onEvent(ContextEvent.MasterWord, 3000L)
        assertEquals(2, engine.getSnapshot().masterCountToday)
    }

    @Test
    fun `test more details and translation tracking`() {
        val engine = ContextEngine()
        assertFalse(engine.getSnapshot().isTranslationEnabled)
        assertFalse(engine.getSnapshot().isMoreDetailsOpened)

        engine.onEvent(ContextEvent.TranslationOpened, 1000L)
        assertTrue(engine.getSnapshot().isTranslationEnabled)

        engine.onEvent(ContextEvent.MoreDetailsOpened, 2000L)
        assertTrue(engine.getSnapshot().isMoreDetailsOpened)

        engine.onEvent(ContextEvent.TranslationClosed, 3000L)
        assertFalse(engine.getSnapshot().isTranslationEnabled)

        engine.onEvent(ContextEvent.MoreDetailsClosed, 4000L)
        assertFalse(engine.getSnapshot().isMoreDetailsOpened)
    }

    @Test
    fun `test session abandoned`() {
        val engine = ContextEngine()
        engine.onEvent(ContextEvent.SessionStarted(10), 1000L)

        val rec = engine.onEvent(ContextEvent.SessionAbandoned, 2000L)
        assertNotNull(rec)
        assertEquals("NextTimeReaction", rec?.reactionName)
        assertEquals("HOME", engine.getSnapshot().currentScreen)
    }

    @Test
    fun `test context updates and event ordering`() {
        val engine = ContextEngine()
        val eventsOrdered = mutableListOf<ContextEvent>()

        engine.addObserver { event, _, _ ->
            eventsOrdered.add(event)
        }

        engine.onEvent(ContextEvent.ApplicationStarted, 1000L)
        engine.onEvent(ContextEvent.SessionStarted(5), 2000L)
        engine.onEvent(ContextEvent.CardFlipped, 3000L)
        engine.onEvent(ContextEvent.CardAnswered(false), 4000L)
        engine.onEvent(ContextEvent.SessionFinished, 5000L)

        assertEquals(5, eventsOrdered.size)
        assertTrue(eventsOrdered[0] is ContextEvent.ApplicationStarted)
        assertTrue(eventsOrdered[1] is ContextEvent.SessionStarted)
        assertTrue(eventsOrdered[2] is ContextEvent.CardFlipped)
        assertTrue(eventsOrdered[3] is ContextEvent.CardAnswered)
        assertTrue(eventsOrdered[4] is ContextEvent.SessionFinished)

        val finalSnapshot = engine.getSnapshot()
        assertEquals("SUMMARY", finalSnapshot.currentScreen)
        assertEquals(3000L, finalSnapshot.sessionDurationMillis)
    }

    @Test
    fun `test reset clears state and observers`() {
        val engine = ContextEngine()
        var callCount = 0
        engine.addObserver { _, _, _ -> callCount++ }

        engine.onEvent(ContextEvent.ApplicationStarted, 1000L)
        assertEquals(1, callCount)

        engine.reset()
        assertEquals("IDLE", engine.getSnapshot().currentScreen)

        engine.onEvent(ContextEvent.ApplicationStarted, 2000L)
        assertEquals(1, callCount) // Observer was cleared
    }
}
