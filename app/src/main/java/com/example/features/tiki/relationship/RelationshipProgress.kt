package com.example.features.tiki.relationship

data class RelationshipProgress(
    val xp: Int = 0,
    val lastActivityTimeMillis: Long = 0L,
    val completedSessions: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val masterWords: Int = 0,
    val studyDays: Int = 0,
    val averageSessionLengthSeconds: Long = 0L,
    val totalStudyTimeMillis: Long = 0L
)
