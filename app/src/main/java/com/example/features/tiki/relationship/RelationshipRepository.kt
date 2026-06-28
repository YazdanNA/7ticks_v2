package com.example.features.tiki.relationship

import android.content.Context
import android.content.SharedPreferences

class RelationshipRepository(private val context: Context? = null) {

    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences("tiki_relationship_prefs", Context.MODE_PRIVATE)
    }

    // In-memory fallback for testing
    private var memoryProgress = RelationshipProgress()

    fun getProgress(): RelationshipProgress {
        val p = prefs ?: return memoryProgress
        return try {
            RelationshipProgress(
                xp = p.getInt("xp", 0),
                lastActivityTimeMillis = p.getLong("lastActivityTimeMillis", 0L),
                completedSessions = p.getInt("completedSessions", 0),
                currentStreak = p.getInt("currentStreak", 0),
                longestStreak = p.getInt("longestStreak", 0),
                masterWords = p.getInt("masterWords", 0),
                studyDays = p.getInt("studyDays", 0),
                averageSessionLengthSeconds = p.getLong("averageSessionLengthSeconds", 0L),
                totalStudyTimeMillis = p.getLong("totalStudyTimeMillis", 0L)
            )
        } catch (e: Exception) {
            memoryProgress
        }
    }

    fun saveProgress(progress: RelationshipProgress) {
        val p = prefs
        if (p == null) {
            memoryProgress = progress
            return
        }
        try {
            p.edit().apply {
                putInt("xp", progress.xp)
                putLong("lastActivityTimeMillis", progress.lastActivityTimeMillis)
                putInt("completedSessions", progress.completedSessions)
                putInt("currentStreak", progress.currentStreak)
                putInt("longestStreak", progress.longestStreak)
                putInt("masterWords", progress.masterWords)
                putInt("studyDays", progress.studyDays)
                putLong("averageSessionLengthSeconds", progress.averageSessionLengthSeconds)
                putLong("totalStudyTimeMillis", progress.totalStudyTimeMillis)
                apply()
            }
        } catch (e: Exception) {
            memoryProgress = progress
        }
    }

    fun clear() {
        memoryProgress = RelationshipProgress()
        val p = prefs ?: return
        try {
            p.edit().clear().apply()
        } catch (e: Exception) {
            // Ignore
        }
    }
}
