package com.example.core.database

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DictionaryRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vocabularyRepository: VocabularyRepository
) {
    suspend fun getWordDetails(word: String): WordDetails? {
        return vocabularyRepository.getWordDetailsByString(word)
    }
}
