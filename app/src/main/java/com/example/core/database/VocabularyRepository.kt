package com.example.core.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VocabularyRepository @Inject constructor(
    private val vocabDatabaseManager: VocabularyDatabaseManager
) {
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

    suspend fun getWordsByLevels(levels: List<String>, limit: Int = 100): List<DictWord> = withContext(Dispatchers.IO) {
        vocabDatabaseManager.getWordsByLevels(levels, limit)
    }
}
