package com.example

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ApplicationProvider
import com.example.core.database.VocabularyDatabaseManager
import com.example.core.database.PreferencesManager
import com.example.core.database.UserDatabase
import com.example.core.database.UserRepository
import com.example.core.database.CardEntity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class ExampleRobolectricTest {

  @Test
  fun runDatabaseDiagnostics() = runBlocking {
    println("--- START OF CEFR AUDIT ROBOLECTRIC ---")
    val context = ApplicationProvider.getApplicationContext<Context>()
    val manager = VocabularyDatabaseManager(context)

    if (!manager.isDatabaseDownloaded()) {
        println("Downloading database...")
        val success = manager.downloadDatabase { progress -> }
        println("Download result: $success")
    }

    if (manager.isDatabaseDownloaded()) {
        val dbFile = context.getDatabasePath("seventick.db")
        println("Database File Path: ${dbFile.absolutePath}")
        println("Database File Size: ${dbFile.length()} bytes")

        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val cursorTables = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
        var tableName = ""
        if (cursorTables.moveToFirst()) {
            do {
                val name = cursorTables.getString(0)
                if (name.equals("words", true) || name.equals("vocabulary", true)) {
                    tableName = name
                }
            } while (cursorTables.moveToNext())
        }
        cursorTables.close()

        if (tableName.isEmpty()) {
            tableName = "words"
        }
        println("Using table: $tableName")

        // 1. Get Columns list
        val pragmaCursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        val columns = mutableListOf<String>()
        if (pragmaCursor.moveToFirst()) {
            do {
                val colName = pragmaCursor.getString(1)
                columns.add(colName)
            } while (pragmaCursor.moveToNext())
        }
        pragmaCursor.close()
        println("Table columns: ${columns.joinToString(", ")}")

        // 2. Count per level
        val levelCol = columns.find { it.equals("level", ignoreCase = true) || it.contains("difficulty", ignoreCase = true) } ?: "level"
        val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: "word"
        val posCol = columns.find { it.equals("part_of_speech", ignoreCase = true) || it.equals("partOfSpeech", ignoreCase = true) || it.equals("pos", ignoreCase = true) } ?: "part_of_speech"
        val defCol = columns.find { it.equals("definition", ignoreCase = true) || it.contains("meaning", ignoreCase = true) } ?: "definition"

        val cursorCounts = db.rawQuery("SELECT $levelCol, COUNT(*) FROM $tableName GROUP BY $levelCol ORDER BY $levelCol ASC", null)
        println("CEFR Levels Counts in Database:")
        var totalWords = 0
        var a1Count = 0
        if (cursorCounts.moveToFirst()) {
            do {
                val lvl = cursorCounts.getString(0)
                val count = cursorCounts.getInt(1)
                println("Level: $lvl -> Count: $count")
                totalWords += count
                if (lvl == "A1") {
                    a1Count = count
                }
            } while (cursorCounts.moveToNext())
        }
        cursorCounts.close()
        println("Total words: $totalWords")

        // 3. First 20 A1 words (ordered by word ASC)
        println("First 20 A1 words:")
        val first20 = manager.getWordsByLevels(listOf("A1"), limit = 20, offset = 0)
        first20.forEachIndexed { i, item ->
            println("${i + 1}. ${item.word} (${item.partOfSpeech}) - ${item.level}: ${item.definition}")
        }

        // 4. Last 20 A1 words (ordered by word ASC)
        println("Last 20 A1 words:")
        val last20Offset = (a1Count - 20).coerceAtLeast(0)
        val last20 = manager.getWordsByLevels(listOf("A1"), limit = 20, offset = last20Offset)
        last20.forEachIndexed { i, item ->
            println("${i + 1}. ${item.word} (${item.partOfSpeech}) - ${item.level}: ${item.definition}")
        }

        db.close()
    } else {
        println("Database not downloaded!")
    }
    println("--- END OF CEFR AUDIT ROBOLECTRIC ---")
  }

  @Test
  fun testSmartLearnOnDemandCardCreation() = runBlocking {
    println("--- START OF SMART LEARN REFACTOR VERIFICATION ---")
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbManager = VocabularyDatabaseManager(context)
    val prefs = PreferencesManager(context)
    
    // Ensure vocabulary database is downloaded for testing
    if (!dbManager.isDatabaseDownloaded()) {
        println("Downloading database...")
        dbManager.downloadDatabase { }
    }
    
    // Clear / initialize database context safely
    val userDb = UserDatabase.getDatabase(context)
    val userDao = userDb.userDao()
    userDao.clearReviewCards() // Keep database clean for the audit test
    
    // Set onboarding settings (10 minute study goal / day)
    prefs.isFirstLaunch = false
    prefs.currentLevel = 1 // A1 level
    prefs.dailyGoal = "10 min / day"
    
    val repository = UserRepository(context, userDao, dbManager, prefs)
    
    // Execute profile and data initialization (First launch simulation)
    repository.initializeUserDataProfile()
    repository.prepareSmartLearnEngine()
    
    // VERIFICATION 1: review_cards table must be completely empty after onboarding
    val cardsAfterOnboarding = userDao.getAllCardsOnce()
    println("Verification 1 - Cards in database after onboarding: ${cardsAfterOnboarding.size}")
    assertEquals(0, cardsAfterOnboarding.size)
    
    // Execute session generation (which is now the sole card creation authority)
    val sessionCards = repository.generateSmartLearnSession()
    println("Verification 2 - Session card list count: ${sessionCards.size}")
    
    // VERIFICATION 2: Smart Learn must create only the required number of new cards (10 min -> 3 cards)
    assertEquals(3, sessionCards.size)
    
    val cardsInDbAfterSession = userDao.getAllCardsOnce()
    println("Verification 3 - Cards in database after generating session: ${cardsInDbAfterSession.size}")
    assertEquals(3, cardsInDbAfterSession.size)
    
    // VERIFICATION 3: All newly created cards must belong to user's unlocked levels ("A1")
    for (card in cardsInDbAfterSession) {
        val word = dbManager.getWordById(card.wordId)
        println("Card Word: ${card.word}, Level: ${word?.level}")
        assertEquals("A1", word?.level?.uppercase())
    }
    
    // VERIFICATION 4: No duplicate cards should be created if called again without workload change
    val secondSession = repository.generateSmartLearnSession()
    println("Verification 4 - Second session card list count: ${secondSession.size}")
    assertEquals(3, secondSession.size)
    
    val cardsInDbAfterSecondSession = userDao.getAllCardsOnce()
    assertEquals(3, cardsInDbAfterSecondSession.size)
    
    println("--- END OF SMART LEARN REFACTOR VERIFICATION ---")
  }

  @Test
  fun testAllVocabularyReachability() = runBlocking {
    println("--- START OF VOCABULARY REACHABILITY TEST ---")
    val context = ApplicationProvider.getApplicationContext<Context>()
    val dbManager = VocabularyDatabaseManager(context)
    
    if (!dbManager.isDatabaseDownloaded()) {
        dbManager.downloadDatabase { }
    }
    
    val a1WordsCount = dbManager.getWordCountByLevels(listOf("A1"))
    println("Total A1 words count via COUNT(*): $a1WordsCount")
    
    val a1WordsList = dbManager.getWordsByLevels(listOf("A1"), limit = -1)
    println("Total A1 words returned with limit = -1: ${a1WordsList.size}")
    
    assertEquals(a1WordsCount, a1WordsList.size)
    assertTrue(a1WordsList.isNotEmpty())
    
    // Check if the first word and last word are both present
    println("First word: ${a1WordsList.firstOrNull()?.word}, Last word: ${a1WordsList.lastOrNull()?.word}")
    println("--- END OF VOCABULARY REACHABILITY TEST ---")
  }
}




