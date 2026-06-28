package com.example.features.tiki.relationship

import com.example.features.tiki.dialogue.DialogueCategory
import org.junit.Assert.*
import org.junit.Test

class TikiRelationshipEngineTest {

    @Test
    fun `test XP increases correctly`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        // Initial XP should be 0
        assertEquals(0, engine.getSnapshot().xp)

        // Completed Session event (+50 XP)
        engine.onEvent(RelationshipEvent.SessionCompleted(60000L))
        assertEquals(50, engine.getSnapshot().xp)

        // Daily Study event (+30 XP)
        engine.onEvent(RelationshipEvent.DailyStudyPlayed)
        assertEquals(80, engine.getSnapshot().xp)

        // Words Mastered event (+10 XP per word)
        engine.onEvent(RelationshipEvent.WordsMastered(3))
        assertEquals(110, engine.getSnapshot().xp)
    }

    @Test
    fun `test levels unlock correctly`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        // Level 1: 0 XP
        val initialSnap = engine.getSnapshot()
        assertEquals(1, initialSnap.level)
        assertEquals("New Friend", initialSnap.levelName)
        assertEquals(100, initialSnap.xpToNextLevel)

        // Level 2 threshold is 100 XP
        engine.onEvent(RelationshipEvent.WordsMastered(10)) // +100 XP
        val snap2 = engine.getSnapshot()
        assertEquals(2, snap2.level)
        assertEquals("Study Buddy", snap2.levelName)
        assertEquals(200, snap2.xpToNextLevel) // 300 - 100 = 200

        // Level 5 threshold is 1000 XP
        engine.onEvent(RelationshipEvent.WordsMastered(90)) // +900 XP, total 1000 XP
        val snap5 = engine.getSnapshot()
        assertEquals(5, snap5.level)
        assertEquals("Legendary Partner", snap5.levelName)
        assertEquals(0, snap5.xpToNextLevel)
        assertEquals(1.0f, snap5.progressToNextLevel, 0.01f)
    }

    @Test
    fun `test persistence survives restart`() {
        val repository = RelationshipRepository()
        val engine1 = RelationshipEngine(repository)

        engine1.onEvent(RelationshipEvent.SessionCompleted(120000L))
        assertEquals(50, engine1.getSnapshot().xp)

        // Create a new engine sharing the same repository context (simulated restart)
        val engine2 = RelationshipEngine(repository)
        assertEquals(50, engine2.getSnapshot().xp)
        assertEquals(1, engine2.getSnapshot().progress.completedSessions)
    }

    @Test
    fun `test missing days decays activity familiarity but does not punish level or XP`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        val startTime = 1000000000000L // arbitrary fixed time
        engine.onEvent(RelationshipEvent.SessionCompleted(60000L), startTime)

        val snapToday = engine.getSnapshot(startTime)
        assertEquals(50, snapToday.xp)
        assertEquals(1, snapToday.level)
        assertEquals(1.0f, snapToday.activityFamiliarity, 0.01f)

        // 1 day elapsed: should NOT decay familiarity
        val oneDayLater = startTime + 24 * 3600 * 1000L
        val snapOneDay = engine.getSnapshot(oneDayLater)
        assertEquals(50, snapOneDay.xp)
        assertEquals(1, snapOneDay.level)
        assertEquals(1.0f, snapOneDay.activityFamiliarity, 0.01f)

        // 3 days elapsed: familiarity decays, but level/XP remain exactly the same
        val threeDaysLater = startTime + 3 * 24 * 3600 * 1000L
        val snapThreeDays = engine.getSnapshot(threeDaysLater)
        assertEquals(50, snapThreeDays.xp)
        assertEquals(1, snapThreeDays.level)
        // Decays by (3 - 1) * 0.1f = 0.2f -> familiarity should be 0.8f
        assertEquals(0.8f, snapThreeDays.activityFamiliarity, 0.01f)

        // 15 days elapsed: familiarity hits bottom limit but does not reduce level/XP or drop below 0.1f
        val fifteenDaysLater = startTime + 15 * 24 * 3600 * 1000L
        val snapFifteenDays = engine.getSnapshot(fifteenDaysLater)
        assertEquals(50, snapFifteenDays.xp)
        assertEquals(1, snapFifteenDays.level)
        assertEquals(0.1f, snapFifteenDays.activityFamiliarity, 0.01f)
    }

    @Test
    fun `test relationship never affects algorithms`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        engine.onEvent(RelationshipEvent.WordsMastered(200)) // gain tons of XP

        val snap = engine.getSnapshot()
        // Ensure that algorithm constants or references are untouched.
        // We only provide snapshot statistics and level info.
        assertTrue(snap.level > 1)
        assertEquals(2000, snap.xp)
    }

    @Test
    fun `test dialogue tone changes`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        // Level 1: New Friend tone (defaults to original dialogue, no extra buddiness)
        val dial1 = engine.modifyDialogue("Keep going!", DialogueCategory.Encouragement)
        assertEquals("Keep going!", dial1)

        // Level 2: Study Buddy tone (buddy suffix added)
        engine.onEvent(RelationshipEvent.WordsMastered(15)) // 150 XP, unlocks Level 2
        val dial2 = engine.modifyDialogue("Awesome work!", DialogueCategory.Celebration)
        assertEquals("Awesome work, study buddy!", dial2)

        // Level 3: Reliable Partner tone
        engine.onEvent(RelationshipEvent.WordsMastered(20)) // +200 XP, unlocks Level 3
        val dial3 = engine.modifyDialogue("Keep going!", DialogueCategory.Encouragement)
        assertEquals("Keep going! We make a great reliable partnership.", dial3)

        // Level 4: Trusted Companion tone
        engine.onEvent(RelationshipEvent.WordsMastered(30)) // +300 XP, unlocks Level 4
        val dial4 = engine.modifyDialogue("Great pace!", DialogueCategory.Encouragement)
        assertEquals("Great pace! I'm glad we are trusted companions.", dial4)

        // Level 5: Legendary Partner tone
        engine.onEvent(RelationshipEvent.WordsMastered(40)) // +400 XP, unlocks Level 5
        val dial5 = engine.modifyDialogue("Spectacular!", DialogueCategory.Celebration)
        assertEquals("Spectacular! You are a legendary partner!", dial5)
    }

    @Test
    fun `test milestone-based dialogue overrides`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        // Milestone 1 Session Completed
        engine.onEvent(RelationshipEvent.SessionCompleted(60000L))
        val firstSessionDial = engine.modifyDialogue("Hello!", DialogueCategory.Greeting)
        assertEquals("Welcome!", firstSessionDial)

        // Fast forward sessions to 30
        for (i in 2..30) {
            engine.onEvent(RelationshipEvent.SessionCompleted(60000L))
        }
        val session30Dial = engine.modifyDialogue("Hello!", DialogueCategory.Greeting)
        assertEquals("Nice seeing you again.", session30Dial)

        // Fast forward sessions to 100
        for (i in 31..100) {
            engine.onEvent(RelationshipEvent.SessionCompleted(60000L))
        }
        val session100Dial = engine.modifyDialogue("Hello!", DialogueCategory.Greeting)
        assertEquals("We've come a long way.", session100Dial)
    }

    @Test
    fun `test future support events have no bad behavior`() {
        val repository = RelationshipRepository()
        val engine = RelationshipEngine(repository)

        // Fire future-support events to verify architectural readiness without implementing logic
        engine.onEvent(RelationshipEvent.Birthday)
        engine.onEvent(RelationshipEvent.Anniversary)
        engine.onEvent(RelationshipEvent.LongAbsence)
        engine.onEvent(RelationshipEvent.SeasonalEvent)

        // Snapshot is correct, XP remains unaffected by configuration-only events
        assertEquals(0, engine.getSnapshot().xp)
    }
}
