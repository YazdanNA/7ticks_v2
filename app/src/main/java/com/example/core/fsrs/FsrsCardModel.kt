package com.example.core.fsrs

import java.util.Date

data class FsrsCardModel(
    val id: Int = 0,
    val wordId: Int,
    val word: String,
    val stability: Double = 0.0,
    val difficulty: Double = 0.0,
    val elapsedDays: Int = 0,
    val scheduledDays: Int = 0,
    val reps: Int = 0,
    val lapses: Int = 0,
    val state: Int = 0, // 0 = New, 1 = Learning, 2 = Review, 3 = Relearning
    val lastReviewed: Date? = null,
    val dueDate: Date = Date()
)
