package com.example.core.fsrs

import java.util.Date
import com.example.core.database.CardEntity
import com.example.core.database.BoxWordEntity

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

fun CardEntity.toFsrsModel() = FsrsCardModel(
    id = id,
    wordId = wordId,
    word = word,
    stability = stability,
    difficulty = difficulty,
    elapsedDays = elapsedDays,
    scheduledDays = scheduledDays,
    reps = reps,
    lapses = lapses,
    state = state,
    lastReviewed = if (lastReviewed > 0) Date(lastReviewed) else null,
    dueDate = Date(dueDate)
)

fun BoxWordEntity.toFsrsModel() = FsrsCardModel(
    id = id,
    wordId = wordId,
    word = word,
    stability = stability,
    difficulty = difficulty,
    elapsedDays = elapsedDays,
    scheduledDays = scheduledDays,
    reps = reps,
    lapses = lapses,
    state = state,
    lastReviewed = if (lastReviewed > 0) Date(lastReviewed) else null,
    dueDate = Date(dueDate)
)

fun FsrsCardModel.toCardEntity(oldBoxIndex: Int) = CardEntity(
    id = id,
    wordId = wordId,
    word = word,
    boxIndex = oldBoxIndex,
    stability = stability,
    difficulty = difficulty,
    elapsedDays = elapsedDays,
    scheduledDays = scheduledDays,
    reps = reps,
    lapses = lapses,
    state = state,
    lastReviewed = lastReviewed?.time ?: 0L,
    dueDate = dueDate.time
)

fun FsrsCardModel.toBoxWordEntity(boxId: Int, oldBoxIndex: Int, addedAt: Long) = BoxWordEntity(
    id = id,
    boxId = boxId,
    wordId = wordId,
    word = word,
    boxIndex = oldBoxIndex,
    stability = stability,
    difficulty = difficulty,
    elapsedDays = elapsedDays,
    scheduledDays = scheduledDays,
    reps = reps,
    lapses = lapses,
    state = state,
    lastReviewed = lastReviewed?.time ?: 0L,
    dueDate = dueDate.time,
    addedAt = addedAt
)
