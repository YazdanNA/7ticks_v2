package com.example.features.tiki.behavior

sealed interface BehaviorEvent {
    object SessionStarted : BehaviorEvent
    object SessionFinished : BehaviorEvent
    object CardFlipped : BehaviorEvent
    object CardAnsweredAgain : BehaviorEvent
    object CardAnsweredHard : BehaviorEvent
    object CardAnsweredGood : BehaviorEvent
    object CardAnsweredEasy : BehaviorEvent
    object CardThinkingStarted : BehaviorEvent
    data class CardThinkingFinished(val durationMillis: Long) : BehaviorEvent
    object TranslationOpened : BehaviorEvent
    object TranslationClosed : BehaviorEvent
    object MoreDetailsOpened : BehaviorEvent
    object MoreDetailsClosed : BehaviorEvent
    object AppPaused : BehaviorEvent
    object AppResumed : BehaviorEvent
    object DictionaryOpened : BehaviorEvent
    object AnalysisOpened : BehaviorEvent
    object BoxesOpened : BehaviorEvent
    object ProfileOpened : BehaviorEvent
    data class WordSearched(val query: String, val found: Boolean) : BehaviorEvent
    data class WordStarred(val word: String) : BehaviorEvent
    data class OnboardingStepChanged(val step: Int) : BehaviorEvent
}
