package com.example.features.tiki.content

import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class TikiContentEngineTest {

    private val baseEnglishJson = """
        {
          "version": "1.0",
          "language": "en",
          "dialogues": [
            {
              "id": "greetings_welcome",
              "text": "Welcome!",
              "emotion": "HAPPY",
              "category": "Greeting",
              "priority": 80,
              "relationshipLevel": 1,
              "minimumStreak": 0,
              "maximumStreak": 5,
              "minimumSessionProgress": 0.0,
              "maximumSessionProgress": 0.5,
              "weight": 100,
              "tags": ["welcome", "greeting"]
            },
            {
              "id": "motivation_keep_going",
              "text": "Keep going!",
              "emotion": "HAPPY",
              "category": "Encouragement",
              "priority": 60,
              "relationshipLevel": 1,
              "minimumStreak": 0,
              "weight": 100,
              "tags": ["motivation"]
            }
          ]
        }
    """.trimIndent()

    private val germanTranslationJson = """
        {
          "version": "1.0",
          "language": "de",
          "dialogues": [
            {
              "id": "greetings_welcome",
              "text": "Willkommen!",
              "emotion": "HAPPY",
              "category": "Greeting",
              "priority": 80,
              "relationshipLevel": 1,
              "minimumStreak": 0,
              "maximumStreak": 5,
              "minimumSessionProgress": 0.0,
              "maximumSessionProgress": 0.5,
              "weight": 100,
              "tags": ["welcome", "greeting"]
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `test JSON loading and parsing`() {
        val loader = DialogueLoader()
        val lib = loader.loadFromString(baseEnglishJson)

        assertEquals("1.0", lib.version)
        assertEquals("en", lib.language)
        assertEquals(2, lib.dialogues.size)

        val first = lib.dialogues.first()
        assertEquals("greetings_welcome", first.id)
        assertEquals("Welcome!", first.text)
        assertEquals("HAPPY", first.emotion)
        assertEquals("Greeting", first.category)
        assertEquals(80, first.priority)
        assertEquals(1, first.relationshipLevel)
        assertEquals(0, first.minimumStreak)
        assertEquals(5, first.maximumStreak)
        assertEquals(0.0f, first.minimumSessionProgress)
        assertEquals(0.5f, first.maximumSessionProgress)
        assertEquals(100, first.weight)
        assertTrue(first.tags.contains("welcome"))
    }

    @Test
    fun `test validation of empty text, duplicate IDs, invalid priority, and bad tags`() {
        val invalidJson = """
            {
              "version": "1.0",
              "language": "en",
              "dialogues": [
                {
                  "id": "item_1",
                  "text": "",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": 80
                },
                {
                  "id": "item_1",
                  "text": "Hello!",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": -10
                },
                {
                  "id": "item_3",
                  "text": "Hi!",
                  "emotion": "INVALID_EMOTION_NAME",
                  "category": "Greeting",
                  "priority": 10,
                  "tags": [""]
                }
              ]
            }
        """.trimIndent()

        val loader = DialogueLoader()
        val lib = loader.loadFromString(invalidJson)

        val validator = DialogueValidator()
        val errors = validator.validate(lib)

        assertTrue(errors.any { it.type == "EMPTY_TEXT" })
        assertTrue(errors.any { it.type == "DUPLICATE_ID" })
        assertTrue(errors.any { it.type == "INVALID_PRIORITY" })
        assertTrue(errors.any { it.type == "INVALID_EMOTION" })
        assertTrue(errors.any { it.type == "BROKEN_TAG" })
    }

    @Test
    fun `test duplicate detection`() {
        val duplicateJson = """
            {
              "version": "1.0",
              "language": "en",
              "dialogues": [
                {
                  "id": "same_id",
                  "text": "One",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": 10
                },
                {
                  "id": "same_id",
                  "text": "Two",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": 10
                }
              ]
            }
        """.trimIndent()

        val loader = DialogueLoader()
        val lib = loader.loadFromString(duplicateJson)
        val validator = DialogueValidator()
        val errors = validator.validate(lib)

        assertTrue(errors.any { it.type == "DUPLICATE_ID" && it.dialogueId == "same_id" })
    }

    @Test
    fun `test weight selection`() {
        val weightedJson = """
            {
              "version": "1.0",
              "language": "en",
              "dialogues": [
                {
                  "id": "rare",
                  "text": "Rare",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": 10,
                  "weight": 1
                },
                {
                  "id": "common",
                  "text": "Common",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": 10,
                  "weight": 999
                }
              ]
            }
        """.trimIndent()

        val engine = ContentEngine()
        engine.loadLibrary(weightedJson)

        var commonCount = 0
        var rareCount = 0

        for (i in 0 until 1000) {
            val resolved = engine.resolveDialogue(category = "Greeting", language = "en", random = Random)
            assertNotNull(resolved)
            if (resolved!!.id == "common") {
                commonCount++
            } else if (resolved.id == "rare") {
                rareCount++
            }
        }

        assertTrue(commonCount > rareCount)
        assertTrue(commonCount > 900)
    }

    @Test
    fun `test multi language loading and missing translation fallback`() {
        val engine = ContentEngine()
        engine.loadLibrary(baseEnglishJson)
        engine.loadLibrary(germanTranslationJson)

        val deLib = engine.getLibrary("de")
        assertNotNull(deLib)
        assertEquals("de", deLib!!.language)

        val enLib = engine.getLibrary("en")
        assertNotNull(enLib)
        assertEquals("en", enLib!!.language)

        val resolvedDe = engine.resolveDialogue(category = "Greeting", language = "de")
        assertNotNull(resolvedDe)
        assertEquals("Willkommen!", resolvedDe!!.text)

        val resolvedFallback = engine.resolveDialogue(category = "Encouragement", language = "de")
        assertNotNull(resolvedFallback)
        assertEquals("Keep going!", resolvedFallback!!.text)
        assertEquals("en", resolvedFallback.language)
    }

    @Test
    fun `test version compatibility and future extensibility`() {
        val futureJson = """
            {
              "version": "2.5",
              "language": "en",
              "dialogues": [
                {
                  "id": "future_item",
                  "text": "Future Text",
                  "emotion": "HAPPY",
                  "category": "Greeting",
                  "priority": 10,
                  "brandNewMetadataField": "Unexpected but supported value"
                }
              ]
            }
        """.trimIndent()

        val loader = DialogueLoader()
        val lib = loader.loadFromString(futureJson)

        assertEquals("2.5", lib.version)
        val first = lib.dialogues.first()
        assertEquals("Future Text", first.text)
        assertEquals("Unexpected but supported value", first.additionalProperties["brandNewMetadataField"])
    }
}
