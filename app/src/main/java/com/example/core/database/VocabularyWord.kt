package com.example.core.database

data class VocabularyWord(
    val word: String,
    val level: String,
    val type: String,
    val topic: String,
    val phoneticsUs: String?,
    val phoneticsUk: String?,
    val definitions: List<String>,
    val meanings: List<String>,
    val examples: List<String>,
    val synonyms: List<String>,
    val antonyms: List<String>,
    val wordFamily: List<String>
)
