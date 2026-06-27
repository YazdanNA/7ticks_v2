package com.example.features.tiki

import com.example.features.tiki.api.TikiController
import com.example.features.tiki.api.TikiEvents
import com.example.features.tiki.model.EmotionState
import com.example.features.tiki.repository.DialogueRepository
import com.example.features.tiki.repository.EmotionAssetRepository
import org.junit.Assert.*
import org.junit.Test

class TikiEngineTest {

    @Test
    fun `test asset lookup maps to correct properties`() {
        val repository = EmotionAssetRepository()

        // Verify HAPPY lookup
        val happyAsset = repository.resolve(EmotionState.HAPPY)
        assertEquals("happy", happyAsset.face)
        assertEquals("normal", happyAsset.body)
        assertEquals("none", happyAsset.gadget)
        assertEquals("st-happy", happyAsset.tikiStateKey)

        // Verify SAD lookup
        val sadAsset = repository.resolve(EmotionState.SAD)
        assertEquals("sad", sadAsset.face)
        assertEquals("sad", sadAsset.body)
        assertEquals("none", sadAsset.gadget)
        assertEquals("st-sad", sadAsset.tikiStateKey)

        // Verify STREAK_FIRE lookup
        val fireAsset = repository.resolve(EmotionState.STREAK_FIRE)
        assertEquals("happy", fireAsset.face)
        assertEquals("large", fireAsset.body)
        assertEquals("fire", fireAsset.gadget)
        assertEquals("st-streak-fire", fireAsset.tikiStateKey)
    }

    @Test
    fun `test asset lookup fallback for unknown or null emotion`() {
        val repository = EmotionAssetRepository()
        
        // Null emotion should safely fall back to HAPPY
        val fallbackAsset = repository.resolve(null)
        assertEquals("happy", fallbackAsset.face)
        assertEquals("st-happy", fallbackAsset.tikiStateKey)
    }

    @Test
    fun `test dialogue selection maps to requested emotion`() {
        val repository = DialogueRepository()
        
        // Verify we get a non-empty string for positive dialogue candidate lists
        val dialogue = repository.getDialogueForEmotion(EmotionState.WELCOME, null)
        assertTrue(dialogue.isNotEmpty())
        
        // Ensure dialogue is less than or equal to 5 words
        val wordCount = dialogue.split("\\s+".toRegex()).filter { it.isNotEmpty() }.size
        assertTrue("Dialogue was too long: $dialogue", wordCount <= 5)
    }

    @Test
    fun `test dialogue selection avoids consecutive repetition`() {
        val repository = DialogueRepository()
        
        // Ensure that if we retrieve multiple times, we don't repeat the previous sentence consecutively
        val firstDialogue = repository.getDialogueForEmotion(EmotionState.HAPPY, null)
        
        // Retrieve second dialogue specifying the first dialogue as last spoken
        val secondDialogue = repository.getDialogueForEmotion(EmotionState.HAPPY, firstDialogue)
        
        assertNotEquals(firstDialogue, secondDialogue)
    }

    @Test
    fun `test controller updates flow cleanly on events`() {
        val controller = TikiController()
        
        // Initially should be default state
        var state = controller.state.value
        assertEquals(EmotionState.HAPPY, state.emotion)
        
        // Trigger welcome emotion state instantly for deterministic test
        controller.onEvent(TikiEvents.TriggerEmotion(EmotionState.WELCOME, forceInstant = true))
        
        state = controller.state.value
        assertEquals(EmotionState.WELCOME, state.emotion)
        assertEquals("happy", state.face)
        assertEquals("wave", state.body)
        assertEquals("stars", state.gadget)
        assertEquals("st-welcome", state.tikiStateKey)
        
        // Trigger manual custom dialogue
        controller.onEvent(TikiEvents.TriggerDialogue("Tiki custom text!", EmotionState.THINKING))
        
        state = controller.state.value
        assertEquals(EmotionState.THINKING, state.emotion)
        assertEquals("Tiki custom text!", state.dialogue)
    }

    @Test
    fun `test controller resets state cleanly`() {
        val controller = TikiController()
        
        controller.setEmotion(EmotionState.ANGRY_RED, forceInstant = true)
        assertEquals(EmotionState.ANGRY_RED, controller.state.value.emotion)
        
        controller.reset()
        assertEquals(EmotionState.HAPPY, controller.state.value.emotion)
    }
}
