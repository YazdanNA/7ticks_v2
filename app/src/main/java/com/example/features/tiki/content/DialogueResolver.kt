package com.example.features.tiki.content

import kotlin.random.Random

class DialogueResolver {

    fun resolve(
        library: DialogueLibrary,
        category: String? = null,
        language: String = "en",
        emotion: String? = null,
        relationshipLevel: Int = 1,
        currentStreak: Int = 0,
        sessionProgress: Float = 0f,
        thinkingState: String? = null,
        tags: List<String> = emptyList(),
        random: Random = Random
    ): DialogueMetadata? {
        val candidates = library.dialogues.filter { dialogue ->
            if (!dialogue.enabled) return@filter false
            
            if (dialogue.language != language) return@filter false
            
            if (category != null && dialogue.category != category) return@filter false
            
            if (emotion != null && dialogue.emotion.uppercase() != emotion.uppercase()) return@filter false
            
            if (dialogue.relationshipLevel != null && relationshipLevel < dialogue.relationshipLevel) return@filter false
            
            if (dialogue.minimumStreak != null && currentStreak < dialogue.minimumStreak) return@filter false
            if (dialogue.maximumStreak != null && currentStreak > dialogue.maximumStreak) return@filter false
            
            if (dialogue.minimumSessionProgress != null && sessionProgress < dialogue.minimumSessionProgress) return@filter false
            if (dialogue.maximumSessionProgress != null && sessionProgress > dialogue.maximumSessionProgress) return@filter false
            
            if (dialogue.thinkingState != null && thinkingState != dialogue.thinkingState) return@filter false
            
            if (tags.isNotEmpty() && !dialogue.tags.containsAll(tags)) return@filter false
            
            true
        }

        if (candidates.isEmpty()) return null

        val totalWeight = candidates.sumOf { it.weight }
        if (totalWeight <= 0) {
            return candidates.random(random)
        }

        val targetWeight = random.nextInt(totalWeight)
        var currentWeightSum = 0
        for (candidate in candidates) {
            currentWeightSum += candidate.weight
            if (targetWeight < currentWeightSum) {
                return candidate
            }
        }

        return candidates.last()
    }
}
