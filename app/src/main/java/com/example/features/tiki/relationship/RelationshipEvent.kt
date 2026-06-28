package com.example.features.tiki.relationship

sealed interface RelationshipEvent {
    data class SessionCompleted(val durationMillis: Long = 0L) : RelationshipEvent
    object DailyStudyPlayed : RelationshipEvent
    object ReturningTomorrow : RelationshipEvent
    data class WordsMastered(val count: Int) : RelationshipEvent
    data class StreakMaintained(val days: Int) : RelationshipEvent
    object DifficultWordHelped : RelationshipEvent

    // Future Support - Prepared architecture without implementation
    object Birthday : RelationshipEvent
    object LongAbsence : RelationshipEvent
    object Anniversary : RelationshipEvent
    object SeasonalEvent : RelationshipEvent
}
