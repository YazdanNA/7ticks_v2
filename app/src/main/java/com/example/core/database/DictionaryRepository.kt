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
class DictionaryRepository @Inject constructor(
    private val context: Context
) {
    private val dbFile: File = context.getDatabasePath("seventick.db")

    private fun getDatabase(): SQLiteDatabase? {
        if (!dbFile.exists()) return null
        return try {
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        } catch (e: Exception) {
            Log.e("DictionaryRepository", "Error opening database", e)
            null
        }
    }

    suspend fun getWordDetails(word: String): VocabularyWord? = withContext(Dispatchers.IO) {
        val db = getDatabase() ?: return@withContext null
        var result: VocabularyWord? = null
        try {
            // Get first table name
            val tableCursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_metadata' AND name NOT LIKE 'sqlite_%'",
                null
            )
            val tableName = if (tableCursor.moveToFirst()) {
                tableCursor.getString(0)
            } else {
                tableCursor.close()
                return@withContext null
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

            val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: ""
            if (wordCol.isEmpty()) return@withContext null

            val cursor = db.query(
                tableName,
                null,
                "$wordCol = ?",
                arrayOf(word),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                val levelCol = columns.find { it.equals("level", ignoreCase = true) || it.contains("difficulty", ignoreCase = true) } ?: ""
                val typeCol = columns.find { it.equals("part_of_speech", ignoreCase = true) || it.equals("pos", ignoreCase = true) || it.equals("type", ignoreCase = true) } ?: ""
                val topicCol = columns.find { it.equals("topic", ignoreCase = true) || it.contains("category", ignoreCase = true) } ?: ""
                val phUsCol = columns.find { it.contains("phonetic", ignoreCase = true) && (it.contains("us", ignoreCase = true) || it.contains("american", ignoreCase = true)) } ?: ""
                val phUkCol = columns.find { it.contains("phonetic", ignoreCase = true) && (it.contains("uk", ignoreCase = true) || it.contains("british", ignoreCase = true)) } ?: ""
                val phGenericCol = columns.find { it.equals("phonetic", ignoreCase = true) || it.equals("phonetics", ignoreCase = true) || it.contains("pron", ignoreCase = true) } ?: ""

                // definitions/meanings/examples/synonyms/antonyms/wordFamily
                val defCol = columns.find { it.equals("definition", ignoreCase = true) || it.equals("definitions", ignoreCase = true) || it.contains("meaning", ignoreCase = true) } ?: ""
                val meaningCol = columns.find { it.equals("meaning", ignoreCase = true) || it.equals("meanings", ignoreCase = true) || it.contains("translation", ignoreCase = true) || it.contains("farsi", ignoreCase = true) || it.contains("persian", ignoreCase = true) || it.equals("fa_definition", ignoreCase = true) || it.equals("faDefinition", ignoreCase = true) } ?: ""
                val exCol = columns.find { it.equals("example", ignoreCase = true) || it.equals("examples", ignoreCase = true) || it.contains("sentence", ignoreCase = true) } ?: ""
                val synCol = columns.find { it.equals("synonyms", ignoreCase = true) || it.equals("synonym", ignoreCase = true) } ?: ""
                val antCol = columns.find { it.equals("antonyms", ignoreCase = true) || it.equals("antonym", ignoreCase = true) } ?: ""
                val familyCol = columns.find { it.equals("word_family", ignoreCase = true) || it.equals("wordFamily", ignoreCase = true) || it.contains("family", ignoreCase = true) } ?: ""

                val wordVal = cursor.getString(cursor.getColumnIndexOrThrow(wordCol))
                val levelVal = if (levelCol.isNotEmpty()) cursor.getString(cursor.getColumnIndexOrThrow(levelCol)) else "B1"
                val typeVal = if (typeCol.isNotEmpty()) cursor.getString(cursor.getColumnIndexOrThrow(typeCol)) else "Noun"
                val topicVal = if (topicCol.isNotEmpty()) cursor.getString(cursor.getColumnIndexOrThrow(topicCol)) else "General"
                
                val phoneticsUsVal = if (phUsCol.isNotEmpty()) cursor.getString(cursor.getColumnIndexOrThrow(phUsCol)) else {
                    if (phGenericCol.isNotEmpty()) cursor.getString(cursor.getColumnIndexOrThrow(phGenericCol)) else null
                }
                val phoneticsUkVal = if (phUkCol.isNotEmpty()) cursor.getString(cursor.getColumnIndexOrThrow(phUkCol)) else null

                // Extract list-like structures with clean split/parsing helper
                fun getListFromField(colName: String): List<String> {
                    if (colName.isEmpty()) return emptyList()
                    val idx = cursor.getColumnIndex(colName)
                    if (idx == -1) return emptyList()
                    val raw = cursor.getString(idx) ?: return emptyList()
                    return raw.split(Regex("[,;|\\n]")).map { it.trim() }.filter { it.isNotEmpty() }
                }

                val definitionsVal = getListFromField(defCol)
                val meaningsVal = getListFromField(meaningCol)
                val examplesVal = getListFromField(exCol)
                val synonymsVal = getListFromField(synCol)
                val antonymsVal = getListFromField(antCol)
                val wordFamilyVal = getListFromField(familyCol)

                result = VocabularyWord(
                    word = wordVal ?: "",
                    level = levelVal ?: "B1",
                    type = typeVal ?: "Noun",
                    topic = topicVal ?: "General",
                    phoneticsUs = phoneticsUsVal,
                    phoneticsUk = phoneticsUkVal,
                    definitions = definitionsVal,
                    meanings = meaningsVal,
                    examples = examplesVal,
                    synonyms = synonymsVal,
                    antonyms = antonymsVal,
                    wordFamily = wordFamilyVal
                )
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("DictionaryRepository", "Failed to load word details", e)
        } finally {
            db.close()
        }
        result
    }
}
