package com.example.features.tiki.memory

import com.example.features.tiki.behavior.BehaviorEvent

class MemoryEngine(
    val store: MemoryStore = MemoryStore(50),
    val session: SessionMemory = SessionMemory()
) {

    fun processEvent(
        event: BehaviorEvent,
        currentTimeMillis: Long = System.currentTimeMillis()
    ) {
        when (event) {
            is BehaviorEvent.SessionStarted -> {
                reset()
                session.startSession(currentTimeMillis)
                store.addEvent(event, currentTimeMillis)
            }
            is BehaviorEvent.SessionFinished -> {
                store.addEvent(event, currentTimeMillis)
                reset()
            }
            else -> {
                if (session.sessionStartTimeMillis == 0L) {
                    session.startSession(currentTimeMillis)
                }
                session.recordEvent(event)
                store.addEvent(event, currentTimeMillis)
            }
        }
    }

    fun getSnapshot(currentTimeMillis: Long = System.currentTimeMillis()): MemorySnapshot {
        return MemoryAnalyzer.analyze(store, session, currentTimeMillis)
    }

    fun reset() {
        store.clear()
        session.reset()
    }

    companion object {
        @Volatile
        private var INSTANCE: MemoryEngine? = null

        fun getInstance(): MemoryEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = MemoryEngine()
                INSTANCE = instance
                instance
            }
        }
    }
}
