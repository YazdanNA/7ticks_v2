package com.example.features.tiki.director

import com.example.features.tiki.model.EmotionState
import kotlin.random.Random

object DirectorRules {

    private const val SILENCE_CHANCE = 0.20 // 20% chance of natural silence for low priority events

    fun shouldProcessEvent(
        incomingPriority: DirectorPriority,
        state: DirectorState,
        currentTimeMillis: Long
    ): Boolean {
        if (state.isPaused) return false

        // 1. Priority Check: Discard if incoming priority is strictly lower than active playing priority
        if (incomingPriority.value < state.activePriority) {
            return false
        }

        // 2. Cooldown Check: Discard lower-priority reactions during emotional spam cooldown
        if (currentTimeMillis < state.cooldownExpirationMillis) {
            // Only allow high priority overrides (Speech, Celebration, Relationship Greetings)
            if (incomingPriority.value < DirectorPriority.RELATIONSHIP_GREETING.value) {
                return false
            }
        }

        return true
    }

    fun resolveConflict(
        candidates: List<Pair<DirectorDecision, DirectorPriority>>
    ): Pair<DirectorDecision, DirectorPriority>? {
        if (candidates.isEmpty()) return null
        // Returns the candidate with the highest priority. If priorities are equal, takes the newest one.
        return candidates.maxByOrNull { it.second.value }
    }

    /**
     * Applies organic styling, randomizes silence, or pairs/adjusts expressions.
     */
    fun refineDecision(
        decision: DirectorDecision,
        priority: DirectorPriority,
        random: Random = Random
    ): DirectorDecision {
        // Low priority reactions sometimes choose natural silence to avoid overreacting
        if (priority.value <= DirectorPriority.BEHAVIOR_REACTION.value) {
            if (random.nextDouble() < SILENCE_CHANCE) {
                return DirectorDecision.RemainSilent
            }
        }
        return decision
    }

    fun calculateCooldownDuration(priority: DirectorPriority): Long {
        return when (priority) {
            DirectorPriority.SPEECH -> 1000L
            DirectorPriority.CELEBRATION -> 3000L
            DirectorPriority.RELATIONSHIP_GREETING -> 2000L
            DirectorPriority.ACHIEVEMENT -> 1500L
            DirectorPriority.BEHAVIOR_REACTION -> 4000L // longer cooldown for spammy feedback
            DirectorPriority.AMBIENT -> 500L
        }
    }
}
