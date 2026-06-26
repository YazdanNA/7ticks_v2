package com.example.core.learning.engine

import com.example.core.database.CardEntity

/**
 * Priority session queue assembler.
 * Priorities:
 * 1. Again cards (state == 3)
 * 2. Due Review cards (state == 2 & dueDate <= currentTime)
 * 3. Learning cards (state == 1)
 * 4. New cards (state == 0)
 *
 * Implements "Reviews Always Win": If Again + Due Review already exceed or meet capacity,
 * no new cards are inserted.
 */
class SessionBuilder {
    fun buildSession(
        allCards: List<CardEntity>,
        capacity: Int,
        currentTime: Long = System.currentTimeMillis()
    ): List<CardEntity> {
        if (capacity <= 0) return emptyList()

        // Categorize card tiers
        val againCards = allCards.filter { it.state == 3 }
        val dueCards = allCards.filter { it.state == 2 && it.dueDate <= currentTime }
        val learningCards = allCards.filter { it.state == 1 }
        val newCards = allCards.filter { it.state == 0 }

        val selected = mutableListOf<CardEntity>()
        var remaining = capacity

        // Priority 1: Again cards
        val takenAgain = againCards.take(remaining)
        selected.addAll(takenAgain)
        remaining -= takenAgain.size

        // Priority 2: Due Review cards
        if (remaining > 0) {
            val takenDue = dueCards.take(remaining)
            selected.addAll(takenDue)
            remaining -= takenDue.size
        }

        // Reviews Always Win constraint:
        // "If Again + Due already exceed capacity, DO NOT insert any new cards."
        val totalReviewsCount = againCards.size + dueCards.size
        if (totalReviewsCount >= capacity) {
            return selected // Stop right here, do not add any learning or new cards
        }

        // Priority 3: Learning cards
        if (remaining > 0) {
            val takenLearning = learningCards.take(remaining)
            selected.addAll(takenLearning)
            remaining -= takenLearning.size
        }

        // Priority 4: New cards
        if (remaining > 0) {
            val takenNew = newCards.take(remaining)
            selected.addAll(takenNew)
            remaining -= takenNew.size
        }

        return selected
    }
}
