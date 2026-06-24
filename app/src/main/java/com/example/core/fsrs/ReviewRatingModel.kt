package com.example.core.fsrs

enum class ReviewRatingModel(val value: Int) {
    AGAIN(1),
    HARD(2),
    GOOD(3),
    EASY(4);

    companion object {
        fun fromValue(value: Int): ReviewRatingModel {
            return entries.find { it.value == value } ?: GOOD
        }
    }
}
