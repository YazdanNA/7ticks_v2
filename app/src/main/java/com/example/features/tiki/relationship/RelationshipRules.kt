package com.example.features.tiki.relationship

import com.example.features.tiki.dialogue.DialogueCategory

object RelationshipRules {

    fun calculateXpGain(event: RelationshipEvent): Int {
        return when (event) {
            is RelationshipEvent.SessionCompleted -> 50
            is RelationshipEvent.DailyStudyPlayed -> 30
            is RelationshipEvent.ReturningTomorrow -> 40
            is RelationshipEvent.WordsMastered -> event.count * 10
            is RelationshipEvent.StreakMaintained -> 20
            is RelationshipEvent.DifficultWordHelped -> 15
            is RelationshipEvent.Birthday -> 0
            is RelationshipEvent.LongAbsence -> 0
            is RelationshipEvent.Anniversary -> 0
            is RelationshipEvent.SeasonalEvent -> 0
        }
    }

    fun modifyDialogue(
        baseText: String,
        snapshot: RelationshipSnapshot,
        category: DialogueCategory
    ): String {
        val stats = snapshot.progress
        val levelNum = snapshot.level

        // Milestone-based exact overrides for Greetings/Idle
        if (category.categoryName == "Greeting" || category.categoryName == "Idle") {
            when {
                stats.completedSessions == 1 -> {
                    return "Welcome!"
                }
                stats.completedSessions == 30 -> {
                    return "Nice seeing you again."
                }
                stats.completedSessions == 100 -> {
                    return "We've come a long way."
                }
            }
        }

        // Master word milestone
        if (stats.masterWords >= 500) {
            if (category.categoryName == "Celebration" || category.categoryName == "Easy" || category.categoryName == "EasyStreak") {
                return "$baseText I'm proud of you."
            }
        }

        // General tone modifications based on level (not affecting algorithms, only dialogue tone)
        return when (levelNum) {
            1 -> {
                // New Friend: More polite and formal
                baseText
            }
            2 -> {
                // Study Buddy: Peer-like, enthusiastic
                if (baseText.endsWith("!")) {
                    baseText.substring(0, baseText.length - 1) + ", study buddy!"
                } else {
                    "$baseText, study buddy!"
                }
            }
            3 -> {
                // Reliable Partner: Team-focused
                "$baseText We make a great reliable partnership."
            }
            4 -> {
                // Trusted Companion: Deeply supportive
                "$baseText I'm glad we are trusted companions."
            }
            5 -> {
                // Legendary Partner: Extremely celebratory
                "$baseText You are a legendary partner!"
            }
            else -> baseText
        }
    }
}
