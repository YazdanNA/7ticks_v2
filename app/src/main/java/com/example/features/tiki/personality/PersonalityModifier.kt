package com.example.features.tiki.personality

import kotlin.random.Random

object ToxicFilter {
    private val FORBIDDEN_WORDS = setOf(
        "wrong", "forgot again", "stupid", "fail", "bad", "terrible", "loser", "idiot", "useless"
    )

    fun isToxicOrHarsh(text: String): Boolean {
        val lower = text.lowercase()
        return FORBIDDEN_WORDS.any { lower.contains(it) }
    }

    fun sanitize(text: String): String {
        when {
            text.equals("Wrong.", ignoreCase = true) -> return "We'll get it."
            text.equals("You forgot again.", ignoreCase = true) -> return "One more try."
        }
        if (isToxicOrHarsh(text)) {
            return "Keep going!"
        }
        return text
    }
}

class PersonalityModifier(private val random: Random = Random.Default) {
    private val recentlyUsedStyles = ArrayDeque<String>()
    private val maxRecentStyles = 15

    fun modify(context: PersonalityContext, rules: List<PersonalityRule>): String {
        val sanitizedBase = ToxicFilter.sanitize(context.baseDialogueText)
        if (sanitizedBase.isEmpty()) return ""

        val applicableRules = rules.filter { it.appliesTo(context) }
        val candidatePool = mutableListOf(sanitizedBase)
        for (rule in applicableRules) {
            candidatePool.addAll(rule.modify(context).map { ToxicFilter.sanitize(it) })
        }

        // Enforce max 5 words limit and non-toxicity
        val validCandidates = candidatePool.filter { candidate ->
            candidate.isNotEmpty() && candidate.split("\\s+".toRegex()).size <= 5
        }.distinct()

        val nonRecent = if (validCandidates.size > 1) {
            validCandidates.filter { it !in recentlyUsedStyles }
        } else {
            validCandidates
        }

        val finalPool = if (nonRecent.isEmpty()) validCandidates else nonRecent
        val chosen = if (finalPool.isEmpty()) "Great job!" else finalPool.random(random)

        recordUsage(chosen)
        return chosen
    }

    fun recordUsage(text: String) {
        if (recentlyUsedStyles.size >= maxRecentStyles) {
            recentlyUsedStyles.removeFirst()
        }
        recentlyUsedStyles.addLast(text)
    }

    fun clearHistory() {
        recentlyUsedStyles.clear()
    }
}
