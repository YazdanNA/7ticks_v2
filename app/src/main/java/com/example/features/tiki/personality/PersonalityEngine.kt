package com.example.features.tiki.personality

import com.example.features.tiki.dialogue.DialogueCategory
import com.example.features.tiki.model.EmotionState

class PersonalityEngine(
    var profile: PersonalityProfile = PersonalityProfile(),
    initialRules: List<PersonalityRule> = DefaultPersonalityRules.all,
    val modifier: PersonalityModifier = PersonalityModifier()
) {
    private val rules = initialRules.toMutableList()

    fun addRule(rule: PersonalityRule) {
        rules.add(rule)
    }

    fun removeRule(ruleName: String) {
        rules.removeAll { it.name == ruleName }
    }

    fun processDialogue(
        baseText: String,
        category: DialogueCategory,
        emotion: EmotionState
    ): String {
        val context = PersonalityContext(
            baseDialogueText = baseText,
            category = category,
            emotion = emotion,
            profile = profile
        )
        return modifier.modify(context, rules)
    }

    fun reset() {
        modifier.clearHistory()
    }

    companion object {
        @Volatile
        private var INSTANCE: PersonalityEngine? = null

        fun getInstance(): PersonalityEngine {
            return INSTANCE ?: synchronized(this) {
                val instance = PersonalityEngine()
                INSTANCE = instance
                instance
            }
        }
    }
}
