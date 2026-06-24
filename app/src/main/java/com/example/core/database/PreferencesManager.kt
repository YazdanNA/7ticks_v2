package com.example.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class PreferencesManager(private val context: Context) {

    private fun <T> getValue(key: Preferences.Key<T>, defaultValue: T): T = runBlocking {
        try {
            context.dataStore.data.first()[key] ?: defaultValue
        } catch (e: Exception) {
            defaultValue
        }
    }

    private fun <T> setValue(key: Preferences.Key<T>, value: T) = runBlocking {
        try {
            context.dataStore.edit { preferences ->
                preferences[key] = value
            }
        } catch (e: Exception) {
            // Handle error gracefully
        }
        Unit
    }

    var isFirstLaunch: Boolean
        get() = getValue(KEY_FIRST_LAUNCH, true)
        set(value) = setValue(KEY_FIRST_LAUNCH, value)

    var userName: String
        get() = getValue(KEY_USER_NAME, "Ali")
        set(value) = setValue(KEY_USER_NAME, value)

    var avatar: String
        get() = getValue(KEY_AVATAR, "avatar_1")
        set(value) = setValue(KEY_AVATAR, value)

    var nativeLanguage: String
        get() = getValue(KEY_NATIVE_LANG, "Persian")
        set(value) = setValue(KEY_NATIVE_LANG, value)

    var targetLanguage: String
        get() = getValue(KEY_TARGET_LANG, "English")
        set(value) = setValue(KEY_TARGET_LANG, value)

    var dailyGoal: String
        get() = getValue(KEY_DAILY_GOAL, "30 words / day")
        set(value) = setValue(KEY_DAILY_GOAL, value)

    var reminderTime: String
        get() = getValue(KEY_REMINDER_TIME, "09:00 AM")
        set(value) = setValue(KEY_REMINDER_TIME, value)

    var currentLevel: Int
        get() = getValue(KEY_CURRENT_LEVEL, 1)
        set(value) = setValue(KEY_CURRENT_LEVEL, value)

    companion object {
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AVATAR = stringPreferencesKey("avatar")
        private val KEY_NATIVE_LANG = stringPreferencesKey("native_lang")
        private val KEY_TARGET_LANG = stringPreferencesKey("target_lang")
        private val KEY_DAILY_GOAL = stringPreferencesKey("daily_goal")
        private val KEY_REMINDER_TIME = stringPreferencesKey("reminder_time")
        private val KEY_CURRENT_LEVEL = intPreferencesKey("current_level")
    }
}
