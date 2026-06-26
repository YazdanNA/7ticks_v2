package com.example.core.ui.components.flashcard

import android.content.Context
import com.example.core.tts.SevenTicksTtsManager

class FlashcardAudioController(context: Context) {
    private val ttsManager = SevenTicksTtsManager(context)

    fun speakWord(text: String) {
        ttsManager.speak(text, isMale = true)
    }

    fun speakExample(text: String) {
        ttsManager.speak(text, isMale = false)
    }

    fun stop() {
        ttsManager.stop()
    }

    fun shutdown() {
        ttsManager.shutdown()
    }
}
