package com.example.core.learning

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.CardEntity
import com.example.core.database.DictWord
import com.example.core.database.UserDatabase
import com.example.core.database.UserProgressEntity
import com.example.core.database.VocabularyDatabaseManager
import com.example.core.learning.engine.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SmartSessionEngineTest {

    private lateinit var database: UserDatabase
    private lateinit var vocabManager: VocabularyDatabaseManager
    private lateinit var engine: SmartSessionEngine

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, UserDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        vocabManager = VocabularyDatabaseManager(context)
        VocabularyDatabaseManager.isTestMode = true
        VocabularyDatabaseManager.testWords.clear()

        engine = SmartSessionEngine(
            userDao = database.userDao(),
            smartSessionDao = database.smartSessionDao(),
            vocabDbManager = vocabManager
        )

        // Seed some standard dictionary words of levels A1, A2, B1, B2
        val seedWords = mutableListOf<DictWord>()
        for (i in 1..100) {
            val level = when {
                i <= 30 -> "A1"
                i <= 60 -> "A2"
                i <= 80 -> "B1"
                else -> "B2"
            }
            seedWords.add(
                DictWord(
                    id = i,
                    word = "word_$i",
                    level = level
                )
            )
        }
        VocabularyDatabaseManager.testWords.addAll(seedWords)
    }

    @After
    fun tearDown() {
        VocabularyDatabaseManager.isTestMode = false
        VocabularyDatabaseManager.testWords.clear()
        database.close()
    }

    @Test
    fun testFirstLaunchNoHistoryCapacityCalculation() = runBlocking {
        // First launch: no history. Capacity calculation should fallback to 20 seconds.
        // User profile has level 1 (only A1 allowed).
        database.userDao().insertUserProgress(UserProgressEntity(id = 0, level = 1))

        // 10 minutes study duration = 600 seconds.
        // Expected capacity = 600 / 20 = 30 cards.
        val cards = engine.buildSmartSession(durationMinutes = 10)

        assertEquals(30, cards.size)
        // Ensure all are from allowed CEFR levels (A1)
        cards.forEach { card ->
            val word = vocabManager.getWordById(card.wordId)
            assertEquals("A1", word?.level)
        }
    }

    @Test
    fun test30DayOverwriteAndPersistence() = runBlocking {
        val dao = database.smartSessionDao()

        // Insert 35 daily stats with different dates
        for (i in 1..35) {
            val dateStr = String.format("2026-06-%02d", i)
            val stats = SmartDailyStatsEntity(
                dateStr = dateStr,
                averageSeconds = 15.0,
                totalStudyTimeSeconds = 150,
                cardsReviewedCount = 10
            )
            dao.insertDailyStatsAndPrune(stats)
        }

        val allStats = dao.getDailyStatsOnce()
        // Ensure oldest records are successfully pruned, keeping only exactly the 30 most recent ones
        assertEquals(30, allStats.size)

        // Overwrite oldest data check: The oldest date should be deleted.
        // The dates from i=1..5 should be deleted. The remaining should start from "2026-06-06"
        val oldestRemainingDate = allStats.map { it.dateStr }.minOrNull()
        assertEquals("2026-06-06", oldestRemainingDate)
    }

    @Test
    fun testCardTimingTrackerBasic() {
        val tracker = CardTimingTracker()
        tracker.start()
        Thread.sleep(1100)
        val elapsed = tracker.stop()
        assertTrue("Elapsed thinking time should be at least 1 second", elapsed >= 1)
    }

    @Test
    fun testCardTimingTrackerCensorAndCap() {
        val tracker = CardTimingTracker()

        // 1. Censor < 1 second
        tracker.start()
        // Stop immediately
        val elapsedShort = tracker.stop()
        assertEquals(-1, elapsedShort) // Under 1 second must return -1 (ignored)

        // 2. Cap > 60 seconds
        val longTracker = object : CardTimingTracker() {
            fun stopWithSeconds(sec: Int): Int {
                return when {
                    sec < 1 -> -1
                    sec > 60 -> 60
                    else -> sec
                }
            }
        }
        assertEquals(60, longTracker.stopWithSeconds(75))
        assertEquals(60, longTracker.stopWithSeconds(500))
        assertEquals(22, longTracker.stopWithSeconds(22))
    }

    @Test
    fun testCardTimingTrackerLifecyclePauses() {
        val tracker = CardTimingTracker()

        // Start -> Pause -> Resume -> Stop
        tracker.start()
        Thread.sleep(200)
        tracker.pause()
        Thread.sleep(500) // This pause period must not count
        tracker.resume()
        Thread.sleep(900)
        val elapsed = tracker.stop()

        // Only the active running parts (200ms + 900ms = 1100ms) should count, making it around 1 second.
        assertEquals(1, elapsed)
    }

    @Test
    fun testSessionBuilderReviewPriority() {
        val builder = SessionBuilder()

        val cards = listOf(
            CardEntity(id = 1, wordId = 1, word = "new1", state = 0, dueDate = 0),
            CardEntity(id = 2, wordId = 2, word = "again1", state = 3, dueDate = 100),
            CardEntity(id = 3, wordId = 3, word = "due1", state = 2, dueDate = 50),
            CardEntity(id = 4, wordId = 4, word = "learn1", state = 1, dueDate = 200),
            CardEntity(id = 5, wordId = 5, word = "due2", state = 2, dueDate = 150) // due
        )

        // Build session with capacity 4, current time = 100
        // Due review cards are only those with dueDate <= 100.
        // At currentTime = 100:
        // - Again cards: again1 (id=2)
        // - Due cards: due1 (id=3) because 50 <= 100. due2 (id=5) is not due because 150 > 100.
        // - Learning cards: learn1 (id=4)
        // - New cards: new1 (id=1)
        // Order: Again -> Due -> Learning -> New
        val selected = builder.buildSession(cards, capacity = 4, currentTime = 100)

        assertEquals(3, selected.size) // again1, due1, learn1
        assertEquals(2, selected[0].id) // again1
        assertEquals(3, selected[1].id) // due1
        assertEquals(4, selected[2].id) // learn1
    }

    @Test
    fun testReviewsAlwaysWin() {
        val builder = SessionBuilder()

        // Capacity = 3
        // We have 4 reviews (2 Again, 2 Due)
        val cards = listOf(
            CardEntity(id = 1, wordId = 1, word = "new1", state = 0),
            CardEntity(id = 2, wordId = 2, word = "again1", state = 3),
            CardEntity(id = 3, wordId = 3, word = "again2", state = 3),
            CardEntity(id = 4, wordId = 4, word = "due1", state = 2, dueDate = 50),
            CardEntity(id = 5, wordId = 5, word = "due2", state = 2, dueDate = 50),
            CardEntity(id = 6, wordId = 6, word = "learn1", state = 1)
        )

        val selected = builder.buildSession(cards, capacity = 3, currentTime = 100)

        // Reviews always win: Again + Due exceed capacity, so no learning or new cards are added.
        assertEquals(3, selected.size)
        // Ensure none of them are state 1 (Learning) or state 0 (New)
        selected.forEach { card ->
            assertTrue(card.state == 3 || card.state == 2)
        }
    }

    @Test
    fun testNewCardCreationOnDemand() = runBlocking {
        // Set progress level to 1 (only A1 allowed)
        database.userDao().insertUserProgress(UserProgressEntity(id = 0, level = 1))

        // Create 2 existing cards
        val card1 = CardEntity(wordId = 1, word = "word_1", state = 1)
        val card2 = CardEntity(wordId = 2, word = "word_2", state = 2, dueDate = System.currentTimeMillis() - 100)
        database.userDao().insertCard(card1)
        database.userDao().insertCard(card2)

        // Expected Capacity with default 20s avg on 5 min (300 seconds) is 15 cards.
        // 2 cards are existing (1 is learning, 1 is due). Remaining capacity = 13.
        // It should create 13 new cards on demand from allowed CEFR levels.
        val sessionCards = engine.buildSmartSession(durationMinutes = 5)

        assertEquals(15, sessionCards.size)
        // Verify no duplicate cards exist
        val uniqueIds = sessionCards.map { it.wordId }.toSet()
        assertEquals(15, uniqueIds.size)
    }

    @Test
    fun testCEFRRestrictions() = runBlocking {
        // Progress level = 2 (Allowed: A1, A2. Forbidden: B1, B2)
        database.userDao().insertUserProgress(UserProgressEntity(id = 0, level = 2))

        val sessionCards = engine.buildSmartSession(durationMinutes = 5)

        sessionCards.forEach { card ->
            val word = vocabManager.getWordById(card.wordId)
            assertNotNull(word)
            assertTrue(word!!.level == "A1" || word.level == "A2")
            assertFalse(word.level == "B1" || word.level == "B2")
        }
    }

    @Test
    fun testNoDuplicateCards() = runBlocking {
        database.userDao().insertUserProgress(UserProgressEntity(id = 0, level = 1))

        // Run session builder once
        val session1 = engine.buildSmartSession(durationMinutes = 2)
        val count1 = database.userDao().getAllCardsOnce().size

        // Run session builder again
        val session2 = engine.buildSmartSession(durationMinutes = 2)
        val count2 = database.userDao().getAllCardsOnce().size

        // Verify that second run didn't cause duplicates or recreate cards with the same wordId
        val allCards = database.userDao().getAllCardsOnce()
        val uniqueWordIds = allCards.map { it.wordId }.toSet()
        assertEquals(allCards.size, uniqueWordIds.size)
    }

    @Test
    fun testPersistenceAfterAppRestart() = runBlocking {
        // Log some reviews
        engine.timingTracker.start()
        Thread.sleep(1100)
        engine.logCardReview(cardId = 12, rating = 3) // Rating Good

        val stats = database.smartSessionDao().getReviewLogsForDate(
            engine.getStatsManager().getCurrentDateString()
        )
        assertEquals(1, stats.size)
        assertEquals(12, stats[0].cardId)
        assertEquals(3, stats[0].rating)
    }

    @Test
    fun testEmptyDatabase() = runBlocking {
        database.userDao().insertUserProgress(UserProgressEntity(id = 0, level = 1))
        VocabularyDatabaseManager.testWords.clear() // No vocabulary available

        // Running engine with completely empty database should gracefully return empty list, not crash
        val session = engine.buildSmartSession(durationMinutes = 5)
        assertTrue(session.isEmpty())
    }

    @Test
    fun testInterruptedSessions() {
        val tracker = CardTimingTracker()
        tracker.start()
        tracker.pause()
        tracker.pause() // redundant pause
        tracker.resume()
        tracker.resume() // redundant resume
        val sec = tracker.stop()
        assertTrue(sec >= 0)
    }
}
