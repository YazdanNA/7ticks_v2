package com.example.features.tiki.dialogue

import com.example.features.tiki.behavior.BehaviorEvent
import com.example.features.tiki.memory.LearningMood
import com.example.features.tiki.memory.MemorySnapshot
import com.example.features.tiki.model.EmotionState
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class TikiDialogueEngineTest {

    private fun createDummySnapshot(
        easyStreak: Int = 0,
        againStreak: Int = 0,
        mood: LearningMood = LearningMood.CONFIDENT
    ): MemorySnapshot {
        return MemorySnapshot(
            recentAgainCount = 0,
            recentHardCount = 0,
            recentGoodCount = 0,
            recentEasyCount = 0,
            currentStreak = easyStreak,
            currentEasyStreak = easyStreak,
            longestEasyStreak = easyStreak,
            currentAgainStreak = againStreak,
            longestAgainStreak = againStreak,
            averageThinkingTimeMillis = 1000L,
            cardsAnswered = 10,
            sessionElapsedTimeMillis = 10000L,
            translationUsageCount = 0,
            moreDetailsUsageCount = 0,
            numberOfFlips = 5,
            totalEventsInStore = 15,
            learningMood = mood
        )
    }

    @Test
    fun `test no consecutive repetition`() {
        val rule = object : DialogueRule {
            override val ruleName = "TestRule"
            override val category = DialogueCategory.Easy
            override val priority = 10
            override fun evaluate(context: DialogueContext) = DialogueRuleResult(
                category, priority, listOf(
                    WeightedDialogue("One"),
                    WeightedDialogue("Two")
                )
            )
        }
        val engine = DialogueEngine(listOf(rule))
        val context = DialogueContext(EmotionState.HAPPY)

        var lastText: String? = null
        for (i in 1..20) {
            val result = engine.selectDialogue(context)
            assertNotNull(result.selectedDialogue)
            if (lastText != null) {
                assertNotEquals("Consecutive repetition occurred at turn $i", lastText, result.selectedDialogue)
            }
            lastText = result.selectedDialogue
        }
    }

    @Test
    fun `test priority override`() {
        val engine = DialogueEngine()
        // Context where both SessionComplete (100) and Easy (20) match
        val context = DialogueContext(
            currentEmotion = EmotionState.HAPPY,
            behaviorEvent = BehaviorEvent.CardAnsweredEasy,
            sessionProgress = 1.0f
        )

        val result = engine.selectDialogue(context)
        assertEquals(DialogueCategory.SessionComplete, result.category)
        assertEquals(DialoguePriority.SESSION_COMPLETE, result.priority)
    }

    @Test
    fun `test silent responses`() {
        val engine = DialogueEngine()
        val snapshot = createDummySnapshot(easyStreak = 3)
        val context = DialogueContext(
            currentEmotion = EmotionState.SMILE_BIG,
            behaviorEvent = BehaviorEvent.CardAnsweredEasy,
            memorySnapshot = snapshot
        )

        val result = engine.selectDialogue(context)
        assertEquals(DialogueCategory.Silence, result.category)
        assertEquals("", result.selectedDialogue)
    }

    @Test
    fun `test weighted random`() {
        val candidates = listOf(
            WeightedDialogue("Heavy", 10000),
            WeightedDialogue("Light", 1)
        )
        val selector = DialogueSelector(Random(42))
        var heavyCount = 0

        for (i in 1..100) {
            val history = DialogueHistory() // fresh history each time
            val selected = selector.select(candidates, history)
            if (selected == "Heavy") heavyCount++
        }

        assertTrue("Heavy should be selected vastly more often than Light", heavyCount > 95)
    }

    @Test
    fun `test context selection`() {
        val engine = DialogueEngine()

        // 1. Five Again Streak
        val againSnapshot = createDummySnapshot(againStreak = 5)
        val againContext = DialogueContext(
            currentEmotion = EmotionState.SAD,
            behaviorEvent = BehaviorEvent.CardAnsweredAgain,
            memorySnapshot = againSnapshot
        )
        assertEquals(DialogueCategory.AgainStreak, engine.selectDialogue(againContext).category)

        // 2. Half Session
        val halfContext = DialogueContext(
            currentEmotion = EmotionState.HAPPY,
            behaviorEvent = BehaviorEvent.CardFlipped,
            sessionProgress = 0.5f
        )
        assertEquals(DialogueCategory.HalfSession, engine.selectDialogue(halfContext).category)

        // 3. Poker Long Thinking
        val pokerContext = DialogueContext(
            currentEmotion = EmotionState.POKER,
            behaviorEvent = BehaviorEvent.CardThinkingFinished(9000L),
            thinkingTimeMillis = 9000L
        )
        assertEquals(DialogueCategory.LongThinking, engine.selectDialogue(pokerContext).category)
    }

    @Test
    fun `test emotion filtering`() {
        val engine = DialogueEngine()
        val context = DialogueContext(
            currentEmotion = EmotionState.SAD,
            behaviorEvent = BehaviorEvent.CardAnsweredEasy
        )

        // Filter requiring HAPPY emotion. Current emotion is SAD.
        val filteredResult = engine.selectDialogue(
            context = context,
            emotionFilter = setOf(EmotionState.HAPPY)
        )
        // Should fall back to empty fallback since SAD is not in filter
        assertEquals(DialogueCategory.Idle, filteredResult.category)
        assertEquals(DialoguePriority.FALLBACK, filteredResult.priority)

        // Filter requiring SAD emotion. Current emotion is SAD.
        val passedResult = engine.selectDialogue(
            context = context,
            emotionFilter = setOf(EmotionState.SAD)
        )
        assertEquals(DialogueCategory.Easy, passedResult.category)
    }

    @Test
    fun `test category filtering`() {
        val engine = DialogueEngine()
        val context = DialogueContext(
            currentEmotion = EmotionState.HAPPY,
            behaviorEvent = BehaviorEvent.SessionStarted
        )

        // Allow only Greeting
        val greetResult = engine.selectDialogue(context, categoryFilter = setOf(DialogueCategory.Greeting))
        assertEquals(DialogueCategory.Greeting, greetResult.category)

        // Allow only Celebration (none matches SessionStarted)
        val celebResult = engine.selectDialogue(context, categoryFilter = setOf(DialogueCategory.Celebration))
        assertEquals(DialogueCategory.Idle, celebResult.category) // fallback
    }

    @Test
    fun `test empty dialogue fallback`() {
        val engine = DialogueEngine(initialRules = emptyList())
        val context = DialogueContext(EmotionState.POKER)

        val result = engine.selectDialogue(context)
        assertEquals(DialogueCategory.Idle, result.category)
        assertEquals(DialoguePriority.FALLBACK, result.priority)
        assertEquals("Ready!", result.selectedDialogue)
    }

    @Test
    fun `test max 5 words enforced`() {
        try {
            WeightedDialogue("This sentence has way too many words in it")
            fail("Expected IllegalArgumentException for exceeding 5 words")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("exceeds 5 words"))
        }

        // 5 words should pass
        assertNotNull(WeightedDialogue("One two three four five"))
    }
}
