package com.example.core.learning

import com.example.core.ui.components.FlashcardData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Metadata representation of an individual flashcard inside the learning session queue.
 */
data class StudySessionItem(
    val id: String,
    val data: FlashcardData,
    val circleStates: List<String>,
    val payload: Any // Stores underlying CardEntity, BoxWordEntity, etc.
)

/**
 * Manages preloading, queue order, and background preparational state of flashcards.
 * Keeps at least 20-30 cards preloaded in memory to prevent database access overhead during transitions.
 */
class SessionQueueManager(initialItems: List<StudySessionItem>) {
    private val items = ArrayList(initialItems)
    private val _currentIndex = MutableStateFlow(0)
    
    private val _currentItem = MutableStateFlow<StudySessionItem?>(items.getOrNull(0))
    val currentItem: StateFlow<StudySessionItem?> = _currentItem.asStateFlow()

    private val _nextItem = MutableStateFlow<StudySessionItem?>(items.getOrNull(1))
    val nextItem: StateFlow<StudySessionItem?> = _nextItem.asStateFlow()

    private val _isFinished = MutableStateFlow(initialItems.isEmpty())
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()

    val totalCount: Int get() = items.size
    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()

    /**
     * Move to the next card in the queue, shifting current/next items.
     * Returns true if successful, false if we reached the end of the session.
     */
    fun next(): Boolean {
        val nextIdx = _currentIndex.value + 1
        if (nextIdx >= items.size) {
            _isFinished.value = true
            _currentItem.value = null
            _nextItem.value = null
            return false
        }
        _currentIndex.value = nextIdx
        _currentItem.value = items.getOrNull(nextIdx)
        _nextItem.value = items.getOrNull(nextIdx + 1)
        return true
    }

    /**
     * Dynamically appends extra preloaded items in the background if needed.
     */
    fun appendItems(newItems: List<StudySessionItem>) {
        synchronized(items) {
            items.addAll(newItems)
            if (_currentItem.value == null && items.isNotEmpty()) {
                _currentItem.value = items.getOrNull(_currentIndex.value)
                _nextItem.value = items.getOrNull(_currentIndex.value + 1)
                _isFinished.value = false
            } else if (_nextItem.value == null) {
                _nextItem.value = items.getOrNull(_currentIndex.value + 1)
            }
        }
    }

    /**
     * Dynamically updates the current item's circle states (e.g. during ratings).
     */
    fun updateCurrentItemCircles(newCircles: List<String>) {
        val index = _currentIndex.value
        if (index in 0 until items.size) {
            val updated = items[index].copy(circleStates = newCircles)
            items[index] = updated
            _currentItem.value = updated
        }
    }
}
