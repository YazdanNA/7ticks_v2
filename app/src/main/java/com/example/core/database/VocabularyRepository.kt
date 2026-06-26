package com.example.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.ConcurrentHashMap

@Singleton
class VocabularyRepository @Inject constructor(
    private val vocabDatabaseManager: VocabularyDatabaseManager
) {
    private val wordDetailsCache = ConcurrentHashMap<String, WordDetails>()

    fun isDatabaseDownloaded(): Boolean {
        return vocabDatabaseManager.isDatabaseDownloaded()
    }

    suspend fun downloadDatabase(onProgress: (Float) -> Unit): Boolean {
        return vocabDatabaseManager.downloadDatabase(onProgress)
    }

    fun validateDatabase(): Boolean {
        return vocabDatabaseManager.validateDatabase()
    }

    suspend fun getWordById(id: Int): DictWord? = withContext(Dispatchers.IO) {
        vocabDatabaseManager.getWordById(id)
    }

    suspend fun getWordDetailsById(id: Int): WordDetails? = withContext(Dispatchers.IO) {
        val dictWord = vocabDatabaseManager.getWordById(id) ?: return@withContext null
        val key = dictWord.word.lowercase().trim()
        val cached = wordDetailsCache[key]
        if (cached != null) return@withContext cached

        val details = dictWord.toWordDetails()
        wordDetailsCache[key] = details
        details
    }

    suspend fun getWordDetailsByString(word: String): WordDetails? = withContext(Dispatchers.IO) {
        val key = word.lowercase().trim()
        val cached = wordDetailsCache[key]
        if (cached != null) return@withContext cached

        val dictWord = vocabDatabaseManager.getWordByString(key) ?: return@withContext null
        val details = dictWord.toWordDetails()
        wordDetailsCache[key] = details
        details
    }

    suspend fun getWordsByLevels(levels: List<String>, limit: Int = -1): List<DictWord> = withContext(Dispatchers.IO) {
        vocabDatabaseManager.getWordsByLevels(levels, limit)
    }
}
