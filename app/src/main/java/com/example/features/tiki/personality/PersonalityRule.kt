package com.example.features.tiki.personality

interface PersonalityRule {
    val name: String
    fun appliesTo(context: PersonalityContext): Boolean
    fun modify(context: PersonalityContext): List<String>
}
