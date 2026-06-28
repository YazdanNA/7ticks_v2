package com.example.features.tiki.content

import com.example.features.tiki.dialogue.DialogueCategory
import com.example.features.tiki.model.EmotionState

data class ValidationError(
    val type: String,
    val description: String,
    val dialogueId: String? = null
)

class DialogueValidator {

    fun validate(library: DialogueLibrary): List<ValidationError> {
        val errors = mutableListOf<ValidationError>()
        val ids = mutableSetOf<String>()

        for (dialogue in library.dialogues) {
            val id = dialogue.id
            
            // 1. Duplicate IDs
            if (ids.contains(id)) {
                errors.add(ValidationError("DUPLICATE_ID", "Duplicate dialogue ID found: '$id'", id))
            } else {
                ids.add(id)
            }

            // 2. Empty Text
            if (dialogue.text.trim().isEmpty()) {
                errors.add(ValidationError("EMPTY_TEXT", "Dialogue text is empty", id))
            }

            // 3. Invalid Priorities
            if (dialogue.priority < 0) {
                errors.add(ValidationError("INVALID_PRIORITY", "Priority cannot be negative: ${dialogue.priority}", id))
            }

            // 4. Broken Tags
            for (tag in dialogue.tags) {
                if (tag.trim().isEmpty()) {
                    errors.add(ValidationError("BROKEN_TAG", "Dialogue contains empty/blank tag", id))
                }
            }

            // 5. Invalid Metadata: Verify Category
            val validCategories = setOf(
                "Greeting", "Encouragement", "Celebration", "Failure", "Recovery", 
                "Thinking", "LongThinking", "Again", "AgainStreak", "Easy", 
                "EasyStreak", "Good", "Hard", "HalfSession", "SessionComplete", 
                "Motivation", "Reminder", "Idle", "Silence",
                "session_start_flow", "session_mid_progress", "session_soft_checkpoint",
                "session_continuation_hint", "session_progress_update", "session_near_completion",
                "session_completion_ready", "session_flow_stable", "session_light_pause_return",
                "session_session_smoothing",
                "finish_first_close", "finish_soft_end", "finish_complete_state",
                "finish_neutral_exit", "finish_flow_release", "finish_session_reset",
                "finish_completion_ack", "finish_stable_end", "finish_light_wrap",
                "finish_silent_close",
                "streak_first_start", "streak_growth_step", "streak_continuation",
                "streak_milestone_reached", "streak_long_chain", "achievement_unlock_basic",
                "achievement_unlock_advanced", "achievement_progress_track", "achievement_consistency_high",
                "achievement_mastery_path", "reward_soft_ack", "reward_neutral_gain",
                "reward_stable_progress", "reward_long_term", "reward_session_chain",
                "master_word_first_contact", "master_word_recognition", "master_word_recall",
                "master_word_precision", "master_word_confidence", "master_word_retention",
                "master_word_context_fit", "master_word_deep_link", "master_word_stability",
                "master_word_completion",
                "empty_first_state", "empty_after_review", "empty_session_end",
                "empty_all_clear", "empty_waiting_mode", "empty_soft_pause",
                "empty_stable_idle", "empty_ready_next", "empty_flow_reset",
                "empty_silent_hold",
                "goal_first_setup", "goal_adjustment", "goal_progress_check",
                "goal_soft_reminder", "goal_direction_shift", "coaching_first_hint",
                "coaching_mid_support", "coaching_focus_reset", "coaching_path_correction",
                "coaching_stability",
                "funny_first_contact", "funny_repeat_attempt", "funny_chain_reaction",
                "funny_confusion_flip", "funny_success_twist", "funny_failure_light",
                "funny_unexpected_win", "rare_behavior_detected", "rare_insight_moment",
                "rare_learning_glitch", "rare_surprise_success", "rare_pattern_break",
                "rare_micro_joke", "rare_context_shift", "rare_session_anomaly",
                "relation_first_interaction", "relation_trust_gain", "relation_frustration_support",
                "relation_long_term_companion", "relation_learning_together", "relation_emotional_balance",
                "relation_recovery_support", "relation_consistency_presence", "relation_soft_encouragement",
                "relation_neutral_connection", "relation_boundary_respect", "relation_safe_learning_space",
                "relation_understanding_state", "relation_session_companion", "relation_continuity_flow",
                "season_first_contact", "season_mid_session", "season_event_trigger", "season_holiday_awareness",
                "season_time_shift", "event_soft_reaction", "event_context_update", "event_learning_adjustment",
                "event_flow_state", "event_neutral_presence"
            )
            if (dialogue.category !in validCategories) {
                errors.add(ValidationError("INVALID_CATEGORY", "Category '${dialogue.category}' is invalid", id))
            }

            // 6. Invalid Metadata: Verify Emotion State Name
            val validEmotions = setOf(
                "HAPPY", "SAD", "CURIOUS", "POKER", "THINKING", "SLEEPY",
                "SESSION_FLOW_AWARENESS", "SESSION_MICRO_PROGRESS", "SESSION_SOFT_MILESTONE",
                "SESSION_CONTINUATION_STATE", "SESSION_NEUTRAL_UPDATE", "SESSION_LIGHT_RECOGNITION",
                "SESSION_PROGRESS_STABILITY", "SESSION_FOCUS_CONTINUITY", "SESSION_LOW_PRESSURE_MOTIVATION",
                "SESSION_SESSION_SMOOTHING",
                "SESSION_END_CALM", "SESSION_COMPLETION_SOFT", "SESSION_WRAP_NEUTRAL",
                "SESSION_EXIT_FLOW", "SESSION_FINISH_STABLE", "SESSION_LIGHT_CLOSURE",
                "SESSION_RESET_READY", "SESSION_END_AWARENESS", "SESSION_GENTLE_FINISH",
                "SESSION_SILENT_COMPLETION",
                "STREAK_STABLE_GROWTH", "ACHIEVEMENT_SOFT_UNLOCK", "CONSISTENCY_RECOGNITION",
                "HABIT_REINFORCEMENT", "PROGRESS_MILESTONE_CALM", "EFFORT_CONTINUITY",
                "LONG_TERM_SUPPORT", "QUIET_PRIDE", "STEADY_MASTERY", "LOW_PRESSURE_REWARD",
                "MASTERY_RECOGNITION_SOFT", "VOCABULARY_PRECISION", "SEMANTIC_DEPTH_AWARENESS",
                "LINGUISTIC_CLARITY", "ADVANCED_UNDERSTANDING", "RARE_WORD_ACKNOWLEDGMENT",
                "KNOWLEDGE_STABILITY", "MEANING_LOCK_STATE", "FINAL_UNDERSTANDING", "QUIET_MASTERY",
                "QUEUE_EMPTY_CALM", "WAITING_NEXT_INPUT", "EMPTY_STATE_STABLE", "REVIEW_COMPLETE_SILENT",
                "NO_TASKS_AWARENESS", "PAUSE_BETWEEN_CYCLES", "NEUTRAL_COMPLETION_SPACE", "SOFT_IDLE_READINESS",
                "LEARNING_BUFFER_CLEAR", "GENTLE_STOP_STATE",
                "GOAL_AWARENESS", "COACHING_SOFT_DIRECTION", "PATH_GUIDANCE", "FOCUS_ALIGNMENT",
                "PROGRESS_ORIENTATION", "HABIT_SUPPORT", "GENTLE_PLANNING", "DIRECTION_CLARITY",
                "STEADY_MOTIVATION", "QUIET_STRUCTURE",
                "FUNNY_LIGHT_SURPRISE", "RARE_BEHAVIOR_DETECTED", "QUIRKY_INSIGHT", "UNEXPECTED_PATTERN",
                "SOFT_HUMOR", "ODD_CONNECTION", "PLAYFUL_OBSERVATION", "STRANGE_CLARITY",
                "RARE_MOMENT_AWARENESS", "GENTLE_LAUGHTER_STATE",
                "RELATIONSHIP_TRUST_BUILDING", "RELATIONSHIP_SOFT_SUPPORT", "RELATIONSHIP_PRESENCE",
                "RELATIONSHIP_GENTLE_UNDERSTANDING", "RELATIONSHIP_NONJUDGMENTAL", "RELATIONSHIP_EMOTIONAL_STABILITY",
                "RELATIONSHIP_LEARNING_COMPANION", "RELATIONSHIP_RESPECTFUL_GUIDANCE", "RELATIONSHIP_SAFE_SPACE",
                "RELATIONSHIP_CONTINUITY",
                "SEASON_SOFT_AWARENESS", "EVENT_CONTEXT_SHIFT", "TIME_BASED_MOOD", "LIGHT_FESTIVE_STATE",
                "CALM_HOLIDAY_PRESENCE", "SEASONAL_LEARNING_FLOW", "AMBIENT_EVENT_AWARENESS", "CULTURAL_TIME_HINT",
                "TEMPORAL_MOTIVATION_SOFT", "QUIET_CELEBRATION_STATE"
            )
            if (dialogue.emotion.uppercase() !in validEmotions) {
                errors.add(ValidationError("INVALID_EMOTION", "Emotion '${dialogue.emotion}' is invalid", id))
            }
        }

        return errors
    }

    fun findMissingTranslations(baseLibrary: DialogueLibrary, targetLibrary: DialogueLibrary): List<ValidationError> {
        val baseIds = baseLibrary.dialogues.map { it.id }.toSet()
        val targetIds = targetLibrary.dialogues.map { it.id }.toSet()
        
        val missingIds = baseIds - targetIds
        return missingIds.map { id ->
            ValidationError(
                type = "MISSING_TRANSLATION",
                description = "Dialogue ID '$id' is in base library but missing in translation library for '${targetLibrary.language}'",
                dialogueId = id
            )
        }
    }
}
