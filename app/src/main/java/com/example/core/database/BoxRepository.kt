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

    suspend fun createCustomBox(name: String): Long {
        return userDao.insertCustomBox(CustomBoxEntity(name = name))
    }

    suspend fun deleteCustomBox(box: CustomBoxEntity) {
        userDao.deleteAllWordsInCustomBox(box.id)
        userDao.deleteCustomBox(box)
    }

    suspend fun renameCustomBox(id: Int, newName: String) {
        userDao.updateCustomBoxName(id, newName)
    }

    // Custom Box Words queries
    fun getWordsInCustomBox(boxId: Int): Flow<List<BoxWordEntity>> {
        return userDao.getWordsInCustomBox(boxId)
    }

    suspend fun addWordToCustomBox(boxId: Int, wordId: Int, word: String) {
        userDao.insertBoxWord(BoxWordEntity(boxId = boxId, wordId = wordId, word = word))
    }

    suspend fun removeWordFromCustomBox(boxId: Int, wordId: Int) {
        userDao.deleteBoxWord(boxId, wordId)
    }
}
