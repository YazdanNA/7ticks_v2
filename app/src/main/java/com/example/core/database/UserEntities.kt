package com.example.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 0,
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    val lastReviewTime: Long = 0,
    val userName: String = "Ali",
    val avatar: String = "avatar_1",
    val nativeLanguage: String = "Persian",
    val targetLanguage: String = "English",
    val dailyGoal: String = "30 words / day",
    val reminderTime: String = "09:00 AM"
)

@Entity(tableName = "review_cards")
data class CardEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordId: Int,
    val word: String,
    val boxIndex: Int = 1, // 1 to 7 corresponding to the 7 boxes
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val elapsedDays: Int = 0,
    val scheduledDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val state: Int = 0, // FSRS state: 0=New, 1=Learning, 2=Review, 3=Relearning
    val lastReviewed: Long = 0,
    val dueDate: Long = System.currentTimeMillis()
)

@Entity(tableName = "review_history")
data class ReviewHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val wordId: Int,
    val word: String,
    val rating: Int, // 1=Again, 2=Hard, 3=Good, 4=Easy
    val timestamp: Long = System.currentTimeMillis(),
    val stability: Double = 0.0,
    val difficulty: Double = 0.0
)

@Entity(tableName = "custom_boxes")
data class CustomBoxEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "box_words")
data class BoxWordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val boxId: Int,
    val wordId: Int,
    val word: String,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val unlocked: Boolean = false,
    val unlockedAt: Long = 0,
    val colorHex: String,
    val iconName: String
)

@Entity(tableName = "challenges")
data class ChallengeEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val current: Int = 0,
    val completed: Boolean = false
)

@Entity(tableName = "daily_stats")
data class StatisticsEntity(
    @PrimaryKey val dateStr: String, // e.g. "2026-06-24"
    val wordsLearned: Int = 0,
    val wordsReviewed: Int = 0,
    val xpEarned: Int = 0
)

@Entity(tableName = "learning_sessions")
data class SessionStateEntity(
    @PrimaryKey val id: Int = 0,
    val active: Boolean = false,
    val cardIds: String = "", // comma-separated or JSON list
    val currentIndex: Int = 0,
    val startTime: Long = 0,
    val pendingReviews: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)
