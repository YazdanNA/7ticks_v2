package com.example.features.tiki.content

data class DialogueMetadata(
    val id: String,
    val text: String,
    val emotion: String,
    val category: String,
    val priority: Int,
    val relationshipLevel: Int? = null,
    val minimumStreak: Int? = null,
    val maximumStreak: Int? = null,
    val minimumSessionProgress: Float? = null,
    val maximumSessionProgress: Float? = null,
    val thinkingState: String? = null,
    val cooldown: Long? = null,
    val weight: Int = 100,
    val language: String = "en",
    val enabled: Boolean = true,
    val tags: List<String> = emptyList(),
    val additionalProperties: Map<String, Any> = emptyMap()
)
