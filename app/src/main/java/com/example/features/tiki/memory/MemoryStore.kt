package com.example.features.tiki.memory

import com.example.features.tiki.behavior.BehaviorEvent

data class TimestampedMemoryEvent(
    val event: BehaviorEvent,
    val timestampMillis: Long
)

class MemoryStore(private val maxEvents: Int = 50) {
    private val _events = ArrayDeque<TimestampedMemoryEvent>()
    val events: List<TimestampedMemoryEvent>
        get() = _events.toList()

    val size: Int
        get() = _events.size

    fun addEvent(event: BehaviorEvent, timestampMillis: Long) {
        if (_events.size >= maxEvents) {
            _events.removeFirst()
        }
        _events.addLast(TimestampedMemoryEvent(event, timestampMillis))
    }

    fun clear() {
        _events.clear()
    }

    fun getRecentEvents(): List<BehaviorEvent> = _events.map { it.event }
}
