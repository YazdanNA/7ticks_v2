package com.example

import android.app.Application
import com.example.core.database.PreferencesManager
import com.example.core.database.UserDatabase
import com.example.core.database.UserRepository
import com.example.core.database.VocabularyDatabaseManager
import com.example.core.database.BoxRepository
import com.example.core.database.DictionaryRepository
import com.example.core.database.SearchRepository

class SevenTicksApplication : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var vocabDatabaseManager: VocabularyDatabaseManager
        private set

    lateinit var userDatabase: UserDatabase
        private set

    lateinit var userRepository: UserRepository
        private set

    lateinit var boxRepository: BoxRepository
        private set

    lateinit var dictionaryRepository: DictionaryRepository
        private set

    lateinit var searchRepository: SearchRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferencesManager = PreferencesManager(this)
        vocabDatabaseManager = VocabularyDatabaseManager(this)
        userDatabase = UserDatabase.getDatabase(this)
        val smartSessionEngine = com.example.core.learning.engine.SmartSessionEngine(
            userDatabase.userDao(),
            userDatabase.smartSessionDao(),
            vocabDatabaseManager
        )
        val vocabularyRepository = com.example.core.database.VocabularyRepository(vocabDatabaseManager)
        userRepository = UserRepository(this, userDatabase.userDao(), vocabDatabaseManager, preferencesManager, smartSessionEngine)
        boxRepository = BoxRepository(userDatabase.userDao())
        dictionaryRepository = DictionaryRepository(this, vocabularyRepository)
        searchRepository = SearchRepository(this)
    }

    companion object {
        lateinit var instance: SevenTicksApplication
            private set
    }
}
