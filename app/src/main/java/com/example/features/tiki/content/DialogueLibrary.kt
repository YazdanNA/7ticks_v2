package com.example.features.tiki.content

data class DialogueLibrary(
    val version: String,
    val language: String,
    val dialogues: List<DialogueMetadata>
)
