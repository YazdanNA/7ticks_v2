package com.example.features.tiki.behavior

data class TimestampedEvent(
    val event: BehaviorEvent,
    val timestampMillis: Long
)

class BehaviorHistory(private val maxEvents: Int = 20) {
    private val _events = ArrayDeque<TimestampedEvent>()
    val events: List<TimestampedEvent>
        get() = _events.toList()

    fun recordEvent(event: BehaviorEvent, timestampMillis: Long) {
        if (_events.size >= maxEvents) {
            _events.removeFirst()
        }
        _events.addLast(TimestampedEvent(event, timestampMillis))
    }

    fun clear() {
        _events.clear()
    }

    fun getRecentEvents(): List<BehaviorEvent> = _events.map { it.event }

    fun getRecentAnswerEvents(): List<BehaviorEvent> = _events.map { it.event }.filter {
        it is BehaviorEvent.CardAnsweredEasy ||
        it is BehaviorEvent.CardAnsweredGood ||
        it is BehaviorEvent.CardAnsweredHard ||
        it is BehaviorEvent.CardAnsweredAgain
    }

    /**
     * Counts consecutive answer events of the exact same type from the end of the recent answer list.
     * Returns Pair(EventClass, count).
     */
    fun getTrailingAnswerStreak(): Pair<Class<out BehaviorEvent>?, Int> {
        val answers = getRecentAnswerEvents()
        if (answers.isEmpty()) return Pair(null, 0)

        val lastClass = answers.last()::class.java
        var count = 0
        for (i in answers.lastIndex downTo 0) {
            if (answers[i]::class.java == lastClass) {
                count++
            } else {
                break
            }
        }
        return Pair(lastClass, count)
    }
}
