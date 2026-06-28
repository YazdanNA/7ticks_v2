package com.example.features.tiki.context

fun interface ContextObserver {
    fun onContextChanged(event: ContextEvent, snapshot: ContextSnapshot, recommendation: ContextRecommendation?)
}

class ContextDispatcher {
    private val observers = mutableSetOf<ContextObserver>()

    fun addObserver(observer: ContextObserver) {
        observers.add(observer)
    }

    fun removeObserver(observer: ContextObserver) {
        observers.remove(observer)
    }

    fun dispatch(event: ContextEvent, snapshot: ContextSnapshot, recommendation: ContextRecommendation?) {
        for (observer in observers.toList()) {
            observer.onContextChanged(event, snapshot, recommendation)
        }
    }

    fun clear() {
        observers.clear()
    }
}
