package com.example.features.tiki.relationship

data class RelationshipLevel(
    val levelNumber: Int,
    val name: String,
    val xpRequired: Int
) {
    companion object {
        // Default level mapping. Configurable to satisfy the requirement of not hardcoding UI text.
        var configurableLevels: List<RelationshipLevel> = listOf(
            RelationshipLevel(1, "New Friend", 0),
            RelationshipLevel(2, "Study Buddy", 100),
            RelationshipLevel(3, "Reliable Partner", 300),
            RelationshipLevel(4, "Trusted Companion", 600),
            RelationshipLevel(5, "Legendary Partner", 1000)
        )

        fun getLevelForXp(xp: Int): RelationshipLevel {
            return configurableLevels
                .sortedByDescending { it.xpRequired }
                .firstOrNull { xp >= it.xpRequired }
                ?: configurableLevels.first()
        }

        fun getLevelByNumber(number: Int): RelationshipLevel {
            return configurableLevels.find { it.levelNumber == number } ?: configurableLevels.first()
        }
    }
}
