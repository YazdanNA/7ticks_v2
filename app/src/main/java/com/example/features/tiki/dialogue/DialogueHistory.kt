package com.example.features.tiki.dialogue

class DialogueHistory {
    private val _spokenChronological = ArrayDeque<String>()
    private val _spokenCounts = mutableMapOf<String, Int>()

    val lastSpokenDialogue: String?
        get() = _spokenChronological.lastOrNull()

    val totalSpokenCount: Int
        get() = _spokenChronological.size

    fun recordSpoken(dialogueText: String) {
        _spokenChronological.addLast(dialogueText)
        _spokenCounts[dialogueText] = (_spokenCounts[dialogueText] ?: 0) + 1
    }

    fun getSpokenCount(dialogueText: String): Int {
        return _spokenCounts[dialogueText] ?: 0
    }

    fun clear() {
        _spokenChronological.clear()
        _spokenCounts.clear()
    }
}
