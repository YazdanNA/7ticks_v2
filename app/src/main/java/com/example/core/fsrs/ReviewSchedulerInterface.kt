package com.example.core.fsrs

interface ReviewSchedulerInterface {
    fun calculateNextReview(
        card: FsrsCardModel,
        rating: ReviewRatingModel,
        currentTime: Long
    ): FsrsCardModel
}
