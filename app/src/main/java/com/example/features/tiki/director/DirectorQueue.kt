package com.example.features.tiki.director

import java.util.concurrent.LinkedBlockingQueue

class DirectorQueue {
    private val queue = LinkedBlockingQueue<QueuedDecision>()

    data class QueuedDecision(
        val decision: DirectorDecision,
        val priority: DirectorPriority,
        val timestamp: Long
    )

    fun enqueue(decision: DirectorDecision, priority: DirectorPriority, timestamp: Long = System.currentTimeMillis()) {
        queue.put(QueuedDecision(decision, priority, timestamp))
    }

    fun dequeue(): QueuedDecision? {
        return queue.poll()
    }

    fun peek(): QueuedDecision? {
        return queue.peek()
    }

    fun clear() {
        queue.clear()
    }

    fun isEmpty(): Boolean {
        return queue.isEmpty()
    }

    fun size(): Int {
        return queue.size
    }

    fun removeLowerPriorityThan(priority: DirectorPriority) {
        queue.removeAll { it.priority.value < priority.value }
    }
}
