package com.example.core.fsrs

import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class FsrsRepository @Inject constructor(
    private val fsrsService: FsrsService = FsrsService()
) : ReviewSchedulerInterface {
    
    override fun calculateNextReview(
        card: FsrsCardModel,
        rating: ReviewRatingModel,
        currentTime: Long
    ): FsrsCardModel {
        return fsrsService.calculateNextReview(card, rating, currentTime)
    }
}
