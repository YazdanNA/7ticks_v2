package com.example.core.learning.engine

import com.example.core.database.CardEntity

/**
 * Priority session queue assembler with Adaptive Allocation.
 * Avoids "Reviews Always Win" blocking by reserving 15-20% of the session capacity
 * for introducing/learning new words, ensuring continuous progress.
 */
class SessionBuilder {
    fun buildSession(
        allCards: List<CardEntity>,
        capacity: Int,
        currentTime: Long = System.currentTimeMillis(),
        targetActiveCards: Int = 120
    ): List<CardEntity> {
        if (capacity <= 0) return emptyList()

        // Categorize card tiers
        val againCards = allCards.filter { it.state == 3 }
        val dueCards = allCards.filter { it.state == 2 && it.dueDate <= currentTime }
        val learningCards = allCards.filter { it.state == 1 }
        val newCards = allCards.filter { it.state == 0 }

        // Adaptive allocation: reserve 18% of capacity for new vocabulary (state == 0)
        val targetNewCount = if (newCards.isNotEmpty()) {
            (capacity * 0.18).toInt().coerceAtLeast(1)
        } else {
            0
        }
        val maxReviewCapacity = (capacity - targetNewCount).coerceAtLeast(1)

        val selected = mutableListOf<CardEntity>()
        var remainingReviewSlots = maxReviewCapacity

        // 1. Take Again cards (Priority 1) up to review slots
        val takenAgain = againCards.take(remainingReviewSlots)
        selected.addAll(takenAgain)
        remainingReviewSlots -= takenAgain.size

        // 2. Take Due Review cards (Priority 2) up to review slots
        if (remainingReviewSlots > 0) {
            val takenDue = dueCards.take(remainingReviewSlots)
            selected.addAll(takenDue)
            remainingReviewSlots -= takenDue.size
        }

        // 3. Take Learning cards (Priority 3)
        var remainingTotalSlots = capacity - selected.size
        val takenLearning = learningCards.take(remainingTotalSlots)
        selected.addAll(takenLearning)
        remainingTotalSlots -= takenLearning.size

        // 4. Take New cards (Priority 4) up to remaining slots
        if (remainingTotalSlots > 0 && newCards.isNotEmpty()) {
            val activeCardsCount = allCards.count { card ->
                val isMatureAndDistant = card.state == 2 && card.boxIndex >= 5 && (card.dueDate - currentTime > 14L * 24 * 60 * 60 * 1000L)
                (card.state == 1 || card.state == 2 || card.state == 3) && !isMatureAndDistant
            }
            val deficit = (targetActiveCards - activeCardsCount).coerceAtLeast(0)
            val newCardsToTakeCount = minOf(deficit, remainingTotalSlots)

            if (newCardsToTakeCount > 0) {
                val takenNew = newCards.take(newCardsToTakeCount)
                selected.addAll(takenNew)
                remainingTotalSlots -= takenNew.size
            }
        }

        // 5. Backfill with remaining reviews if we couldn't fill the session with new/learning cards
        if (remainingTotalSlots > 0) {
            val remainingAgain = againCards.filter { !selected.contains(it) }
            val takenExtraAgain = remainingAgain.take(remainingTotalSlots)
            selected.addAll(takenExtraAgain)
            remainingTotalSlots -= takenExtraAgain.size
        }
        if (remainingTotalSlots > 0) {
            val remainingDue = dueCards.filter { !selected.contains(it) }
            val takenExtraDue = remainingDue.take(remainingTotalSlots)
            selected.addAll(takenExtraDue)
            remainingTotalSlots -= takenExtraDue.size
        }

        return selected
    }
}
