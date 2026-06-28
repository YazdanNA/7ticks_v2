package com.example.features.tiki.ambient

enum class AmbientPriority(val value: Int) {
    IDLE(0),
    AMBIENT_ANIMATION(1),
    CELEBRATION(2),
    EMOTION_TRANSITION(3),
    SPEECH(4)
}

data class AmbientState(
    val isSpeaking: Boolean = false,
    val isThinking: Boolean = false,
    val thinkingDurationMillis: Long = 0L,
    val isPaused: Boolean = false,
    val currentPriority: AmbientPriority = AmbientPriority.IDLE,
    val lastAction: AmbientAction? = null,
    val recentActions: List<AmbientAction> = emptyList()
)
