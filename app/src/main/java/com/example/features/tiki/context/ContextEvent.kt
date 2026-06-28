package com.example.features.tiki.context

sealed interface ContextEvent {
    val eventName: String

    object ApplicationStarted : ContextEvent { override val eventName = "ApplicationStarted" }
    object ApplicationClosed : ContextEvent { override val eventName = "ApplicationClosed" }
    data class SessionStarted(val totalCards: Int = 10) : ContextEvent { override val eventName = "SessionStarted" }
    object SessionFinished : ContextEvent { override val eventName = "SessionFinished" }
    object SessionAbandoned : ContextEvent { override val eventName = "SessionAbandoned" }
    object FirstCard : ContextEvent { override val eventName = "FirstCard" }
    object LastCard : ContextEvent { override val eventName = "LastCard" }
    object HalfSessionReached : ContextEvent { override val eventName = "HalfSessionReached" }
    object CardFlipped : ContextEvent { override val eventName = "CardFlipped" }
    object TranslationOpened : ContextEvent { override val eventName = "TranslationOpened" }
    object TranslationClosed : ContextEvent { override val eventName = "TranslationClosed" }
    object MoreDetailsOpened : ContextEvent { override val eventName = "MoreDetailsOpened" }
    object MoreDetailsClosed : ContextEvent { override val eventName = "MoreDetailsClosed" }
    data class CardAnswered(val isMastered: Boolean = false) : ContextEvent { override val eventName = "CardAnswered" }
    object ThinkingStarted : ContextEvent { override val eventName = "ThinkingStarted" }
    data class ThinkingFinished(val durationMillis: Long) : ContextEvent { override val eventName = "ThinkingFinished" }
    object FirstMasterWord : ContextEvent { override val eventName = "FirstMasterWord" }
    object MasterWord : ContextEvent { override val eventName = "MasterWord" }
    object ReviewQueueEmpty : ContextEvent { override val eventName = "ReviewQueueEmpty" }
    object NewWordUnlocked : ContextEvent { override val eventName = "NewWordUnlocked" }
    object StreakExtended : ContextEvent { override val eventName = "StreakExtended" }
    object LevelCompleted : ContextEvent { override val eventName = "LevelCompleted" }
    object AchievementUnlocked : ContextEvent { override val eventName = "AchievementUnlocked" }
    data class Custom(override val eventName: String) : ContextEvent
}
