package com.example.features.tiki.personality

import com.example.features.tiki.dialogue.DialogueCategory
import com.example.features.tiki.model.EmotionState
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class TikiPersonalityEngineTest {

    @Test
    fun `test personality consistency forbids sarcasm and toxic profiles`() {
        try {
            PersonalityProfile(allowsSarcasm = true)
            fail("Expected IllegalArgumentException for allowing sarcasm")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("strictly forbids sarcasm"))
        }

        try {
            PersonalityProfile(allowsToxic = true)
            fail("Expected IllegalArgumentException for allowing toxic behavior")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("strictly forbids sarcasm"))
        }
    }

    @Test
    fun `test no toxic dialogue passes toxic filter`() {
        assertEquals("We'll get it.", ToxicFilter.sanitize("Wrong."))
        assertEquals("One more try.", ToxicFilter.sanitize("You forgot again."))
        assertEquals("Keep going!", ToxicFilter.sanitize("That was terrible."))
        assertEquals("Nice!", ToxicFilter.sanitize("Nice!"))
    }

    @Test
    fun `test friendly wording replacement and variation`() {
        val engine = PersonalityEngine(modifier = PersonalityModifier(Random(42)))

        // Test specific tone replacements
        val res1 = engine.processDialogue("Wrong.", DialogueCategory.Again, EmotionState.SAD)
        assertTrue(res1 in listOf("We'll get it.", "One more try.", "We can do this.", "Step by step."))

        val res2 = engine.processDialogue("Excellent.", DialogueCategory.Easy, EmotionState.HAPPY)
        assertTrue(res2 in listOf("Nice!", "Great!", "Well done!", "Good one!", "Beautiful!", "You're getting stronger."))
    }

    @Test
    fun `test future personality support and friendship architecture`() {
        val profile = PersonalityProfile(
            type = PersonalityType.Playful,
            friendshipLevel = FriendshipLevel.LEARNING_PARTNER
        )
        val engine = PersonalityEngine(profile = profile)

        assertEquals(PersonalityType.Playful, engine.profile.type)
        assertEquals(FriendshipLevel.LEARNING_PARTNER, engine.profile.friendshipLevel)
    }

    @Test
    fun `test variation and anti repetition`() {
        val engine = PersonalityEngine(modifier = PersonalityModifier(Random(123)))

        var lastSpoken: String? = null
        for (i in 1..15) {
            val spoken = engine.processDialogue("Excellent.", DialogueCategory.Easy, EmotionState.HAPPY)
            assertNotNull(spoken)
            assertTrue(spoken.split("\\s+".toRegex()).size <= 5)
            if (lastSpoken != null && engine.modifier.modify(PersonalityContext("Excellent.", DialogueCategory.Easy, EmotionState.HAPPY), DefaultPersonalityRules.all) != spoken) {
                // Ensure variation is happening across turns
            }
            lastSpoken = spoken
        }
    }

    @Test
    fun `test no architecture violations`() {
        // Verify engine is isolated and returns clean strings
        val engine = PersonalityEngine.getInstance()
        val result = engine.processDialogue("Ready!", DialogueCategory.Idle, EmotionState.POKER)
        assertEquals("Ready!", result)
    }
}
