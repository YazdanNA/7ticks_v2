package com.example.core.learning.engine

import com.example.core.database.CardEntity
import com.example.core.database.VocabularyDatabaseManager

/**
 * Handles level filtering to ensure that study materials strictly align with unlocked CEFR configurations.
 */
class SessionSelector(private val vocabDbManager: VocabularyDatabaseManager) {
    suspend fun filterCardsByCefr(cards: List<CardEntity>, allowedLevels: List<String>): List<CardEntity> {
        val filtered = mutableListOf<CardEntity>()
        for (card in cards) {
            val word = vocabDbManager.getWordById(card.wordId)
            val level = word?.level?.uppercase() ?: "A1"
            if (allowedLevels.contains(level)) {
                filtered.add(card)
            }
        }
        return filtered
    }
}
