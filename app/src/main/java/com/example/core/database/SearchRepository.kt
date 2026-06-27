package com.example.core.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SearchRepository @Inject constructor(
    private val context: Context
) {
    private val dbFile: File = context.getDatabasePath("seventick.db")
    private var cachedDb: SQLiteDatabase? = null

    @Synchronized
    private fun getDatabase(): SQLiteDatabase? {
        if (!dbFile.exists()) return null
        val db = cachedDb
        if (db != null && db.isOpen) {
            return db
        }
        return try {
            val newDb = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            cachedDb = newDb
            newDb
        } catch (e: Exception) {
            Log.e("SearchRepository", "Error opening vocabulary database", e)
            null
        }
    }

    suspend fun search(query: String, limit: Int = 50): List<SearchResult> = withContext(Dispatchers.IO) {
        val db = getDatabase() ?: return@withContext emptyList()
        val results = mutableListOf<SearchResult>()
        try {
            // Find first table name
            val tableCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_metadata' AND name NOT LIKE 'sqlite_%'",
                null
            )
            val tableName = if (tableCursor.moveToFirst()) {
                tableCursor.getString(0)
            } else {
                tableCursor.close()
                return@withContext emptyList()
            }
            tableCursor.close()

            // Get columns of the table
            val colCursor = db.rawQuery("PRAGMA table_info($tableName)", null)
            val columns = mutableListOf<String>()
            val nameIdx = colCursor.getColumnIndex("name")
            if (nameIdx != -1) {
                while (colCursor.moveToNext()) {
                    columns.add(colCursor.getString(nameIdx))
                }
            }
            colCursor.close()

            // Find exact columns or fallbacks
            val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
            val levelCol = columns.find { it.equals("level", ignoreCase = true) || it.equals("difficulty_level", ignoreCase = true) } ?: ""
            val defCol = columns.find { it.equals("definition", ignoreCase = true) || it.contains("meaning", ignoreCase = true) || it.contains("def", ignoreCase = true) } ?: ""
            val typeCol = columns.find { it.equals("part_of_speech", ignoreCase = true) || it.equals("pos", ignoreCase = true) || it.equals("type", ignoreCase = true) } ?: ""
            val topicCol = columns.find { it.equals("topic", ignoreCase = true) || it.equals("category", ignoreCase = true) } ?: ""

            if (wordCol.isEmpty()) return@withContext emptyList()

            // Search while typing support: query starts with the string (extremely fast prefix search!)
            val selection = if (query.isEmpty()) {
                null
            } else {
                "$wordCol LIKE ?"
            }
            val selectionArgs = if (query.isEmpty()) {
                null
            } else {
                arrayOf("$query%") // Prefix match: "a", "ab", "abi", "ability"
            }

            val cursor = db.query(
                tableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "$wordCol ASC",
                limit.toString()
            )

            if (cursor.moveToFirst()) {
                val wordIdx = cursor.getColumnIndex(wordCol)
                val levelIdx = if (levelCol.isNotEmpty()) cursor.getColumnIndex(levelCol) else -1
                val defIdx = if (defCol.isNotEmpty()) cursor.getColumnIndex(defCol) else -1
                val typeIdx = if (typeCol.isNotEmpty()) cursor.getColumnIndex(typeCol) else -1
                val topicIdx = if (topicCol.isNotEmpty()) cursor.getColumnIndex(topicCol) else -1

                do {
                    val wordVal = if (wordIdx != -1) cursor.getString(wordIdx) ?: "" else ""
                    val levelVal = if (levelIdx != -1) cursor.getString(levelIdx) ?: "B1" else "B1"
                    
                    val defRaw = if (defIdx != -1) cursor.getString(defIdx) ?: "" else ""
                    val defTrimmed = defRaw.trim()
                    val defVal = if (defTrimmed.startsWith("[") || defTrimmed.startsWith("{")) {
                        JsonParserUtils.parseJsonArray(defTrimmed).firstOrNull() ?: defTrimmed
                    } else {
                        defRaw
                    }

                    val typeRaw = if (typeIdx != -1) cursor.getString(typeIdx) ?: "Noun" else "Noun"
                    val typeTrimmed = typeRaw.trim()
                    val typeVal = if (typeTrimmed.startsWith("[") || typeTrimmed.startsWith("{")) {
                        JsonParserUtils.parseJsonArray(typeTrimmed).firstOrNull() ?: typeTrimmed
                    } else {
                        typeRaw
                    }

                    val topicRaw = if (topicIdx != -1) cursor.getString(topicIdx) ?: "General" else "General"
                    val topicTrimmed = topicRaw.trim()
                    val topicVal = if (topicTrimmed.startsWith("[") || topicTrimmed.startsWith("{")) {
                        JsonParserUtils.parseJsonArray(topicTrimmed).firstOrNull() ?: topicTrimmed
                    } else {
                        topicRaw
                    }

                    results.add(
                        SearchResult(
                            word = wordVal,
                            level = levelVal,
                            shortMeaning = defVal,
                            type = typeVal,
                            topic = topicVal
                        )
                    )
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("SearchRepository", "Search failed", e)
        }
        results
    }
}
