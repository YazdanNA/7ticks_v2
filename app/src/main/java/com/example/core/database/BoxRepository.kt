package com.example.core.database

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BoxRepository @Inject constructor(
    private val userDao: UserDao
) {
    // Leitner / Spaced repetition box queries
    fun getCardsByBox(boxIndex: Int): Flow<List<CardEntity>> {
        return userDao.getCardsByBox(boxIndex)
    }

    fun getCardCountInBox(boxIndex: Int): Flow<Int> {
        return userDao.getCardCountInBox(boxIndex)
    }

    // Custom Boxes queries
    fun getCustomBoxes(): Flow<List<CustomBoxEntity>> {
        return userDao.getCustomBoxes()
    }

    suspend fun getCustomBoxById(id: Int): CustomBoxEntity? {
        return userDao.getCustomBoxById(id)
    }

    suspend fun createCustomBox(
        name: String,
        description: String = "",
        iconName: String = "Folder",
        colorHex: String = "#00C2FF"
    ): Long {
        return userDao.insertCustomBox(
            CustomBoxEntity(
                name = name,
                description = description,
                iconName = iconName,
                colorHex = colorHex
            )
        )
    }

    suspend fun updateCustomBox(box: CustomBoxEntity) {
        userDao.updateCustomBox(box)
    }

    suspend fun archiveCustomBox(id: Int, isArchived: Boolean) {
        val box = userDao.getCustomBoxById(id)
        if (box != null) {
            userDao.updateCustomBox(box.copy(isArchived = isArchived, lastActivityAt = System.currentTimeMillis()))
        }
    }

    suspend fun duplicateCustomBox(id: Int) {
        val original = userDao.getCustomBoxById(id) ?: return
        val newBoxId = userDao.insertCustomBox(
            CustomBoxEntity(
                name = "${original.name} (Copy)",
                description = original.description,
                iconName = original.iconName,
                colorHex = original.colorHex,
                createdAt = System.currentTimeMillis(),
                lastActivityAt = System.currentTimeMillis()
            )
        )
        val originalWords = userDao.getWordsInCustomBoxOnce(id)
        originalWords.forEach { word ->
            userDao.insertBoxWord(
                word.copy(
                    id = 0, // auto-generate new id
                    boxId = newBoxId.toInt(),
                    addedAt = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun deleteCustomBox(box: CustomBoxEntity) {
        userDao.deleteAllWordsInCustomBox(box.id)
        userDao.deleteCustomBox(box)
    }

    suspend fun renameCustomBox(id: Int, newName: String) {
        val box = userDao.getCustomBoxById(id)
        if (box != null) {
            userDao.updateCustomBox(box.copy(name = newName, lastActivityAt = System.currentTimeMillis()))
        } else {
            userDao.updateCustomBoxName(id, newName)
        }
    }

    // Custom Box Words queries
    fun getWordsInCustomBox(boxId: Int): Flow<List<BoxWordEntity>> {
        return userDao.getWordsInCustomBox(boxId)
    }

    suspend fun getWordsInCustomBoxOnce(boxId: Int): List<BoxWordEntity> {
        return userDao.getWordsInCustomBoxOnce(boxId)
    }

    suspend fun getBoxWordById(id: Int): BoxWordEntity? {
        return userDao.getBoxWordById(id)
    }

    suspend fun addWordToCustomBox(boxWord: BoxWordEntity) {
        userDao.insertBoxWord(boxWord)
        val box = userDao.getCustomBoxById(boxWord.boxId)
        if (box != null) {
            userDao.updateCustomBox(box.copy(lastActivityAt = System.currentTimeMillis()))
        }
    }

    suspend fun updateBoxWord(boxWord: BoxWordEntity) {
        userDao.updateBoxWord(boxWord)
    }

    suspend fun removeWordFromCustomBox(boxId: Int, wordId: Int) {
        userDao.deleteBoxWord(boxId, wordId)
    }

    suspend fun removeBoxWordById(id: Int) {
        userDao.deleteBoxWordById(id)
    }

    // Favorites Word operations
    fun getFavoriteWords(): Flow<List<FavoriteWordEntity>> {
        return userDao.getFavoriteWords()
    }

    suspend fun getFavoriteWordsOnce(): List<FavoriteWordEntity> {
        return userDao.getFavoriteWordsOnce()
    }

    suspend fun toggleFavoriteWord(word: String) {
        if (userDao.isFavoriteWord(word)) {
            userDao.deleteFavoriteWord(word)
        } else {
            userDao.insertFavoriteWord(FavoriteWordEntity(word = word))
        }
    }

    suspend fun addFavoriteWord(word: String) {
        userDao.insertFavoriteWord(FavoriteWordEntity(word = word))
    }

    suspend fun removeFavoriteWord(word: String) {
        userDao.deleteFavoriteWord(word)
    }

    fun isFavoriteWordFlow(word: String): Flow<Boolean> {
        return userDao.isFavoriteWordFlow(word)
    }

    suspend fun isFavoriteWord(word: String): Boolean {
        return userDao.isFavoriteWord(word)
    }

    // Recent Searches operations
    fun getRecentSearches(limit: Int = 10): Flow<List<RecentSearchEntity>> {
        return userDao.getRecentSearches(limit)
    }

    suspend fun addRecentSearch(query: String) {
        if (query.trim().isNotEmpty()) {
            userDao.insertRecentSearch(RecentSearchEntity(query = query.trim(), timestamp = System.currentTimeMillis()))
        }
    }

    suspend fun deleteRecentSearch(query: String) {
        userDao.deleteRecentSearch(query)
    }

    suspend fun clearRecentSearches() {
        userDao.clearRecentSearches()
    }
}
