package com.example.features.tiki.dialogue

sealed interface DialogueCategory {
    val categoryName: String

    object Greeting : DialogueCategory { override val categoryName = "Greeting" }
    object Encouragement : DialogueCategory { override val categoryName = "Encouragement" }
    object Celebration : DialogueCategory { override val categoryName = "Celebration" }
    object Failure : DialogueCategory { override val categoryName = "Failure" }
    object Recovery : DialogueCategory { override val categoryName = "Recovery" }
    object Thinking : DialogueCategory { override val categoryName = "Thinking" }
    object LongThinking : DialogueCategory { override val categoryName = "LongThinking" }
    object Again : DialogueCategory { override val categoryName = "Again" }
    object AgainStreak : DialogueCategory { override val categoryName = "AgainStreak" }
    object Easy : DialogueCategory { override val categoryName = "Easy" }
    object EasyStreak : DialogueCategory { override val categoryName = "EasyStreak" }
    object Good : DialogueCategory { override val categoryName = "Good" }
    object Hard : DialogueCategory { override val categoryName = "Hard" }
    object HalfSession : DialogueCategory { override val categoryName = "HalfSession" }
    object SessionComplete : DialogueCategory { override val categoryName = "SessionComplete" }
    object Motivation : DialogueCategory { override val categoryName = "Motivation" }
    object Reminder : DialogueCategory { override val categoryName = "Reminder" }
    object Idle : DialogueCategory { override val categoryName = "Idle" }
    object Silence : DialogueCategory { override val categoryName = "Silence" }
    data class Custom(override val categoryName: String) : DialogueCategory
}
