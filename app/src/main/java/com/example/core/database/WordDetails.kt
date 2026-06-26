package com.example.core.database

data class WordDetails(
    val word: String,
    val level: String,
    val phonetics: String,
    val definitions: List<String>,
    val translations: List<String>,
    val examples: List<String>,
    val exampleTranslations: List<String>,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val wordFamily: List<String>,
    val collocations: List<String>,
    val phrases: List<String>,
    val notes: List<String>,
    val types: List<String>,
    val topics: List<String>
) {
    // Backward compatibility helper properties
    val type: String get() = types.firstOrNull() ?: "Noun"
    val topic: String get() = topics.firstOrNull() ?: "General"
    val phoneticsUs: String get() = phonetics
    val phoneticsUk: String get() = ""
    val meanings: List<String> get() = translations
}

fun DictWord.toWordDetails(): WordDetails {
    val ph = if (phonetics_us.isNotEmpty()) phonetics_us else if (phonetics.isNotEmpty()) phonetics else "N/A"
    val defs = if (definitions_en.isNotEmpty()) definitions_en else if (definition_en.isNotEmpty()) listOf(definition_en) else if (definition.isNotEmpty()) listOf(definition) else emptyList()
    val trans = if (translations.isNotEmpty()) translations else if (definition_fa.isNotEmpty()) listOf(definition_fa) else if (faDefinition.isNotEmpty()) listOf(faDefinition) else emptyList()
    val exs = if (examples_en.isNotEmpty()) examples_en else if (example.isNotEmpty()) listOf(example) else emptyList()
    
    return WordDetails(
        word = word,
        level = level,
        phonetics = ph,
        definitions = defs,
        translations = trans,
        examples = exs,
        exampleTranslations = examples_fa,
        synonyms = synonyms,
        antonyms = antonyms,
        wordFamily = word_family,
        collocations = collocations,
        phrases = phrases,
        notes = notes,
        types = listOf(type),
        topics = listOf(topic)
    )
}

fun BoxWordEntity.toWordDetails(): WordDetails {
    val ph = when {
        !phoneticsUs.isNullOrEmpty() && !phoneticsUk.isNullOrEmpty() -> "US: $phoneticsUs / UK: $phoneticsUk"
        !phoneticsUs.isNullOrEmpty() -> "US: $phoneticsUs"
        !phoneticsUk.isNullOrEmpty() -> "UK: $phoneticsUk"
        else -> ""
    }
    return WordDetails(
        word = word,
        level = level,
        phonetics = ph,
        definitions = definitions.split("\n").filter { it.isNotBlank() },
        translations = meanings.split("\n").filter { it.isNotBlank() },
        examples = examples.split("\n").filter { it.isNotBlank() },
        exampleTranslations = emptyList(),
        synonyms = if (synonyms.isNotEmpty()) synonyms.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
        antonyms = if (antonyms.isNotEmpty()) antonyms.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
        wordFamily = if (wordFamily.isNotEmpty()) wordFamily.split(",").map { it.trim() }.filter { it.isNotEmpty() } else emptyList(),
        collocations = emptyList(),
        phrases = emptyList(),
        notes = emptyList(),
        types = listOf(type),
        topics = listOf(topic)
    )
}
