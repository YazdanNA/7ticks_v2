package com.example.core.database

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

data class DictWord(
    val id: Int,
    val word: String,
    val phonetics: String,
    val definition: String,
    val faDefinition: String,
    val example: String,
    val partOfSpeech: String,
    val level: String
)

@Singleton
class VocabularyDatabaseManager @Inject constructor(private val context: Context) {
    private val dbFile: File = context.getDatabasePath("seventick.db")
    private val client = OkHttpClient()

    init {
        // Ensure parent directories exist
        dbFile.parentFile?.mkdirs()
    }

    fun isDatabaseDownloaded(): Boolean {
        return dbFile.exists() && dbFile.length() > 1024 // reasonable size check
    }

    suspend fun downloadDatabase(onProgress: (Float) -> Unit): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = "https://github.com/YazdanNA/7ticks_DB/raw/refs/heads/main/seventick.db"
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                Log.e("VocabDbManager", "Failed to download database: HTTP ${response.code}")
                return@withContext false
            }

            val body = response.body
            if (body == null) {
                Log.e("VocabDbManager", "Empty body from database download")
                return@withContext false
            }

            val totalBytes = body.contentLength()
            var bytesCopied: Long = 0
            val buffer = ByteArray(8192)

            dbFile.parentFile?.mkdirs()
            if (dbFile.exists()) dbFile.delete()

            body.byteStream().use { input ->
                FileOutputStream(dbFile).use { output ->
                    var bytesRead = input.read(buffer)
                    while (bytesRead != -1) {
                        output.write(buffer, 0, bytesRead)
                        bytesCopied += bytesRead
                        if (totalBytes > 0) {
                            val progress = bytesCopied.toFloat() / totalBytes
                            onProgress(progress)
                        }
                        bytesRead = input.read(buffer)
                    }
                }
            }
            Log.d("VocabDbManager", "Database downloaded successfully: ${dbFile.length()} bytes")
            return@withContext true
        } catch (e: Exception) {
            Log.e("VocabDbManager", "Error downloading vocabulary database", e)
            if (dbFile.exists()) dbFile.delete()
            return@withContext false
        }
    }

    fun validateDatabase(): Boolean {
        if (!dbFile.exists()) return false
        var db: SQLiteDatabase? = null
        return try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val version = db.version
            Log.d("VocabDbManager", "Database validation passed. Version: $version")
            val tables = getTables(db)
            Log.d("VocabDbManager", "Database tables: $tables")
            tables.isNotEmpty()
        } catch (e: Exception) {
            Log.e("VocabDbManager", "Database validation failed", e)
            false
        } finally {
            db?.close()
        }
    }

    private fun getTables(db: SQLiteDatabase): List<String> {
        val tables = mutableListOf<String>()
        val cursor = db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'android_metadata' AND name NOT LIKE 'sqlite_%'",
            null
        )
        if (cursor.moveToFirst()) {
            do {
                tables.add(cursor.getString(0))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return tables
    }

    private fun getColumns(db: SQLiteDatabase, tableName: String): List<String> {
        val columns = mutableListOf<String>()
        val cursor = db.rawQuery("PRAGMA table_info($tableName)", null)
        val nameIndex = cursor.getColumnIndex("name")
        if (nameIndex != -1) {
            while (cursor.moveToNext()) {
                columns.add(cursor.getString(nameIndex))
            }
        }
        cursor.close()
        return columns
    }

    private fun getFirstUserTable(db: SQLiteDatabase): String? {
        val tables = getTables(db)
        // Prefer tables that actually contain vocabulary
        return tables.find { it.equals("words", ignoreCase = true) || it.equals("vocabulary", ignoreCase = true) }
            ?: tables.firstOrNull()
    }

    fun searchWords(query: String, limit: Int = 50): List<DictWord> {
        if (!isDatabaseDownloaded()) return emptyList()
        val results = mutableListOf<DictWord>()
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val tableName = getFirstUserTable(db) ?: return emptyList()
            val columns = getColumns(db, tableName)

            val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
            val defCol = columns.find { it.equals("definition", ignoreCase = true) || it.contains("meaning", ignoreCase = true) } ?: ""

            if (wordCol.isEmpty()) return emptyList()

            val selection = if (query.isEmpty()) {
                null
            } else {
                if (defCol.isNotEmpty()) {
                    "$wordCol LIKE ? OR $defCol LIKE ?"
                } else {
                    "$wordCol LIKE ?"
                }
            }
            val selectionArgs = if (query.isEmpty()) {
                null
            } else {
                if (defCol.isNotEmpty()) {
                    arrayOf("%$query%", "%$query%")
                } else {
                    arrayOf("%$query%")
                }
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
                do {
                    results.add(mapCursorToDictWord(cursor, columns))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("VocabDbManager", "Error searching words", e)
        } finally {
            db?.close()
        }
        return results
    }

    fun getAllWords(limit: Int = 200, offset: Int = 0): List<DictWord> {
        if (!isDatabaseDownloaded()) return emptyList()
        val results = mutableListOf<DictWord>()
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val tableName = getFirstUserTable(db) ?: return emptyList()
            val columns = getColumns(db, tableName)

            val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
            if (wordCol.isEmpty()) return emptyList()

            val cursor = db.query(
                tableName,
                null,
                null,
                null,
                null,
                null,
                "$wordCol ASC",
                "$offset, $limit"
            )

            if (cursor.moveToFirst()) {
                do {
                    results.add(mapCursorToDictWord(cursor, columns))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("VocabDbManager", "Error getting all words", e)
        } finally {
            db?.close()
        }
        return results
    }

    fun getWordsByLevels(levels: List<String>, limit: Int = 100, offset: Int = 0): List<DictWord> {
        if (!isDatabaseDownloaded()) return emptyList()
        val results = mutableListOf<DictWord>()
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val tableName = getFirstUserTable(db) ?: return emptyList()
            val columns = getColumns(db, tableName)

            val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
            if (wordCol.isEmpty()) return emptyList()

            val levelCol = columns.find { it.equals("level", ignoreCase = true) || it.contains("difficulty", ignoreCase = true) } ?: ""
            val selection = if (levelCol.isNotEmpty() && levels.isNotEmpty()) {
                val placeholders = levels.joinToString(",") { "?" }
                "$levelCol IN ($placeholders)"
            } else {
                null
            }
            val selectionArgs = if (levelCol.isNotEmpty() && levels.isNotEmpty()) {
                levels.toTypedArray()
            } else {
                null
            }

            val cursor = db.query(
                tableName,
                null,
                selection,
                selectionArgs,
                null,
                null,
                "$wordCol ASC",
                "$offset, $limit"
            )

            if (cursor.moveToFirst()) {
                do {
                    results.add(mapCursorToDictWord(cursor, columns))
                } while (cursor.moveToNext())
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("VocabDbManager", "Error getting words by levels", e)
        } finally {
            db?.close()
        }
        return results
    }

    fun getWordById(id: Int): DictWord? {
        if (!isDatabaseDownloaded()) return null
        var result: DictWord? = null
        var db: SQLiteDatabase? = null
        try {
            db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
            val tableName = getFirstUserTable(db) ?: return null
            val columns = getColumns(db, tableName)

            val idCol = columns.find { it.equals("id", ignoreCase = true) || it.equals("key", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
            if (idCol.isEmpty()) return null

            val cursor = db.query(
                tableName,
                null,
                "$idCol = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                result = mapCursorToDictWord(cursor, columns)
            }
            cursor.close()
        } catch (e: Exception) {
            Log.e("VocabDbManager", "Error getting word by id", e)
        } finally {
            db?.close()
        }
        return result
    }

    private fun mapCursorToDictWord(cursor: Cursor, columns: List<String>): DictWord {
        val idCol = columns.find { it.equals("id", ignoreCase = true) || it.equals("key", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
        val wordCol = columns.find { it.equals("word", ignoreCase = true) || it.contains("term", ignoreCase = true) } ?: columns.firstOrNull() ?: ""
        val defCol = columns.find { it.equals("definition", ignoreCase = true) || it.contains("meaning", ignoreCase = true) } ?: ""
        val faDefCol = columns.find { it.equals("fa_definition", ignoreCase = true) || it.equals("faDefinition", ignoreCase = true) || it.contains("farsi", ignoreCase = true) || it.contains("persian", ignoreCase = true) } ?: ""
        val phoneticCol = columns.find { it.equals("phonetics", ignoreCase = true) || it.equals("phonetic", ignoreCase = true) || it.contains("pron", ignoreCase = true) } ?: ""
        val posCol = columns.find { it.equals("part_of_speech", ignoreCase = true) || it.equals("partOfSpeech", ignoreCase = true) || it.equals("pos", ignoreCase = true) } ?: ""
        val exampleCol = columns.find { it.equals("example", ignoreCase = true) || it.contains("sample", ignoreCase = true) || it.contains("sentence", ignoreCase = true) } ?: ""
        val levelCol = columns.find { it.equals("level", ignoreCase = true) || it.contains("difficulty", ignoreCase = true) } ?: ""

        val idVal = if (idCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(idCol)
            if (idx != -1) cursor.getInt(idx) else 0
        } else 0

        val wordVal = if (wordCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(wordCol)
            if (idx != -1) cursor.getString(idx) else ""
        } else ""

        val defVal = if (defCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(defCol)
            if (idx != -1) cursor.getString(idx) else ""
        } else ""

        val faDefVal = if (faDefCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(faDefCol)
            if (idx != -1) cursor.getString(idx) else ""
        } else ""

        val phoneticVal = if (phoneticCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(phoneticCol)
            if (idx != -1) cursor.getString(idx) else ""
        } else ""

        val posVal = if (posCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(posCol)
            if (idx != -1) cursor.getString(idx) else "Noun"
        } else "Noun"

        val exampleVal = if (exampleCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(exampleCol)
            if (idx != -1) cursor.getString(idx) else ""
        } else ""

        val levelVal = if (levelCol.isNotEmpty()) {
            val idx = cursor.getColumnIndex(levelCol)
            if (idx != -1) cursor.getString(idx) else "A1"
        } else "A1"

        return DictWord(
            id = idVal,
            word = wordVal,
            phonetics = phoneticVal,
            definition = defVal,
            faDefinition = faDefVal,
            example = exampleVal,
            partOfSpeech = posVal,
            level = levelVal ?: "A1"
        )
    }
}
