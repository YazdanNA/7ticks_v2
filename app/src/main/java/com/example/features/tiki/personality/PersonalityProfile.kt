package com.example.features.tiki.personality

enum class FriendshipLevel {
    STRANGER,
    COMPANION,
    TRUSTED_FRIEND,
    LEARNING_PARTNER
}

sealed interface PersonalityType {
    val name: String

    object Classic : PersonalityType { override val name = "Classic" }
    object Playful : PersonalityType { override val name = "Playful" }
    object Professional : PersonalityType { override val name = "Professional" }
    object Minimal : PersonalityType { override val name = "Minimal" }
    data class Custom(override val name: String) : PersonalityType
}

/**
 * Permanent Tiki companion profile enforcing positive, calm, and supportive boundaries.
 */
data class PersonalityProfile(
    val type: PersonalityType = PersonalityType.Classic,
    val friendshipLevel: FriendshipLevel = FriendshipLevel.STRANGER,
    val isPositive: Boolean = true,
    val isSupportive: Boolean = true,
    val allowsSarcasm: Boolean = false,
    val allowsToxic: Boolean = false
) {
    init {
        require(!allowsSarcasm && !allowsToxic) { "Tiki personality strictly forbids sarcasm and toxicity." }
    }
}
