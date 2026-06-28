package com.example.features.tiki.director

enum class DirectorPriority(val value: Int) {
    AMBIENT(10),
    BEHAVIOR_REACTION(60),
    ACHIEVEMENT(70),
    RELATIONSHIP_GREETING(80),
    CELEBRATION(90),
    SPEECH(100);

    companion object {
        fun fromValue(value: Int): DirectorPriority {
            return values().minByOrNull { Math.abs(it.value - value) } ?: AMBIENT
        }
    }
}
