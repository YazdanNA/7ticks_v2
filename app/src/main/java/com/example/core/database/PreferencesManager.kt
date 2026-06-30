package com.example.core.database

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
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

    // New settings fields for Settings screen
    var appLanguage: String
        get() = getValue(KEY_APP_LANGUAGE, "en")
        set(value) = setValue(KEY_APP_LANGUAGE, value)

    var themeMode: String
        get() = getValue(KEY_THEME_MODE, "system")
        set(value) = setValue(KEY_THEME_MODE, value)

    var fontSizeSetting: String
        get() = getValue(KEY_FONT_SIZE, "default")
        set(value) = setValue(KEY_FONT_SIZE, value)

    var soundEnabled: Boolean
        get() = getValue(KEY_SOUND_ENABLED, true)
        set(value) = setValue(KEY_SOUND_ENABLED, value)

    var hapticEnabled: Boolean
        get() = getValue(KEY_HAPTIC_ENABLED, true)
        set(value) = setValue(KEY_HAPTIC_ENABLED, value)

    var cloudBackupAuto: Boolean
        get() = getValue(KEY_CLOUD_BACKUP_AUTO, true)
        set(value) = setValue(KEY_CLOUD_BACKUP_AUTO, value)

    var lastBackupTime: String
        get() = getValue(KEY_LAST_BACKUP_TIME, "Never")
        set(value) = setValue(KEY_LAST_BACKUP_TIME, value)

    // Flow representations for reactive updates in Jetpack Compose
    val appLanguageFlow: Flow<String> = context.dataStore.data.map { it[KEY_APP_LANGUAGE] ?: "en" }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { it[KEY_THEME_MODE] ?: "system" }

    val fontSizeFlow: Flow<String> = context.dataStore.data.map { it[KEY_FONT_SIZE] ?: "default" }

    val soundEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_SOUND_ENABLED] ?: true }

    val hapticEnabledFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_HAPTIC_ENABLED] ?: true }

    val cloudBackupAutoFlow: Flow<Boolean> = context.dataStore.data.map { it[KEY_CLOUD_BACKUP_AUTO] ?: true }

    val lastBackupTimeFlow: Flow<String> = context.dataStore.data.map { it[KEY_LAST_BACKUP_TIME] ?: "Never" }

    companion object {
        private val KEY_FIRST_LAUNCH = booleanPreferencesKey("is_first_launch")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_AVATAR = stringPreferencesKey("avatar")
        private val KEY_NATIVE_LANG = stringPreferencesKey("native_lang")
        private val KEY_TARGET_LANG = stringPreferencesKey("target_lang")
        private val KEY_DAILY_GOAL = stringPreferencesKey("daily_goal")
        private val KEY_REMINDER_TIME = stringPreferencesKey("reminder_time")
        private val KEY_CURRENT_LEVEL = intPreferencesKey("current_level")

        // New keys
        private val KEY_APP_LANGUAGE = stringPreferencesKey("app_language")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        private val KEY_FONT_SIZE = stringPreferencesKey("font_size")
        private val KEY_SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        private val KEY_HAPTIC_ENABLED = booleanPreferencesKey("haptic_enabled")
        private val KEY_CLOUD_BACKUP_AUTO = booleanPreferencesKey("cloud_backup_auto")
        private val KEY_LAST_BACKUP_TIME = stringPreferencesKey("last_backup_time")
    }
}
