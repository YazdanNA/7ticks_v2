package com.example

import android.app.Application
import com.example.core.database.PreferencesManager
import com.example.core.database.UserDatabase
import com.example.core.database.UserRepository
import com.example.core.database.VocabularyDatabaseManager

class SevenTicksApplication : Application() {

    lateinit var preferencesManager: PreferencesManager
        private set

    lateinit var vocabDatabaseManager: VocabularyDatabaseManager
        private set

    lateinit var userDatabase: UserDatabase
        private set

    lateinit var userRepository: UserRepository
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this

        preferencesManager = PreferencesManager(this)
        vocabDatabaseManager = VocabularyDatabaseManager(this)
        userDatabase = UserDatabase.getDatabase(this)
        userRepository = UserRepository(this, userDatabase.userDao(), vocabDatabaseManager, preferencesManager)
    }

    companion object {
        lateinit var instance: SevenTicksApplication
            private set
    }
}
