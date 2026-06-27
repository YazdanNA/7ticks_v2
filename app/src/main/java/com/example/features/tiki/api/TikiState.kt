package com.example.features.tiki.api

import com.example.features.tiki.model.EmotionState

data class TikiState(
    val emotion: EmotionState = EmotionState.HAPPY,
    val face: String = "happy",
    val body: String = "normal",
    val gadget: String = "none",
    val tikiStateKey: String = "st-happy",
    val dialogue: String = "Hi there! I am Tiki, your vocabulary mentor.",
    val isSquished: Boolean = false
)
