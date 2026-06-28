package com.example.features.tiki.content

import kotlin.random.Random

class ContentEngine(
    private val loader: DialogueLoader = DialogueLoader(),
    private val validator: DialogueValidator = DialogueValidator(),
    private val resolver: DialogueResolver = DialogueResolver()
) {
    private val libraries = mutableMapOf<String, DialogueLibrary>()
    private val usedDialogueIds = mutableSetOf<String>()

    fun loadLibrary(jsonStr: String): DialogueLibrary {
        val lib = loader.loadFromString(jsonStr)
        val errors = validator.validate(lib)
        if (errors.any { it.type == "DUPLICATE_ID" || it.type == "EMPTY_TEXT" || it.type == "INVALID_PRIORITY" }) {
            throw IllegalArgumentException("Dialogue library validation failed with critical errors: $errors")
        }
        libraries[lib.language] = lib
        return lib
    }

    fun loadLibrary(library: DialogueLibrary) {
        val errors = validator.validate(library)
        if (errors.any { it.type == "DUPLICATE_ID" || it.type == "EMPTY_TEXT" || it.type == "INVALID_PRIORITY" }) {
            throw IllegalArgumentException("Dialogue library validation failed with critical errors: $errors")
        }
        libraries[library.language] = library
    }

    fun getLibrary(language: String): DialogueLibrary? {
        return libraries[language]
    }

    fun removeLibrary(language: String) {
        libraries.remove(language)
    }

    fun clear() {
        libraries.clear()
        usedDialogueIds.clear()
    }

    fun recordUsed(dialogueId: String) {
        usedDialogueIds.add(dialogueId)
    }

    fun getUnusedContent(language: String): List<DialogueMetadata> {
        val lib = libraries[language] ?: return emptyList()
        return lib.dialogues.filter { it.id !in usedDialogueIds }
    }

    fun resolveDialogue(
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
        val targetLib = libraries[language]
        var resolved: DialogueMetadata? = null
        if (targetLib != null) {
            resolved = resolver.resolve(
                targetLib, category, language, emotion, relationshipLevel,
                currentStreak, sessionProgress, thinkingState, tags, random
            )
        }

        if (resolved == null && language != "en") {
            val enLib = libraries["en"]
            if (enLib != null) {
                resolved = resolver.resolve(
                    enLib, category, "en", emotion, relationshipLevel,
                    currentStreak, sessionProgress, thinkingState, tags, random
                )
            }
        }

        if (resolved != null) {
            recordUsed(resolved.id)
        }

        return resolved
    }
}
