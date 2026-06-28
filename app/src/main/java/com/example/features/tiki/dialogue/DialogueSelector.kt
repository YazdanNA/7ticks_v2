package com.example.features.tiki.dialogue

import kotlin.random.Random

class DialogueSelector(private val random: Random = Random.Default) {

    fun select(candidates: List<WeightedDialogue>, history: DialogueHistory): String {
        if (candidates.isEmpty()) return ""

        // Check if all candidates are silent ("")
        if (candidates.all { it.text.isEmpty() }) return ""

        // 1. Repetition Prevention: filter out lastSpokenDialogue if possible
        val nonConsecutive = if (candidates.size > 1 && history.lastSpokenDialogue != null) {
            candidates.filter { it.text != history.lastSpokenDialogue }
        } else {
            candidates
        }
        val safePool = if (nonConsecutive.isEmpty()) candidates else nonConsecutive

        // 2. Prefer unused / least used dialogues first
        val minSpokenCount = safePool.minOf { history.getSpokenCount(it.text) }
        val leastUsedPool = safePool.filter { history.getSpokenCount(it.text) == minSpokenCount }

        // 3. Support weighted random selection among leastUsedPool
        val totalWeight = leastUsedPool.sumOf { it.weight }
        if (totalWeight <= 0) return leastUsedPool.random(random).text

        var randomValue = random.nextInt(totalWeight)
        for (item in leastUsedPool) {
            randomValue -= item.weight
            if (randomValue < 0) {
                return item.text
            }
        }
        return leastUsedPool.last().text
    }
}
