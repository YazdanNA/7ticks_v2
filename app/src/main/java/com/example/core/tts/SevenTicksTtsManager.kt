package com.example.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale

class SevenTicksTtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var pendingIsMale: Boolean? = null

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("SevenTicksTts", "US English Language is not supported or missing data")
            } else {
                isInitialized = true
                Log.d("SevenTicksTts", "TTS Initialized successfully")
                val text = pendingText
                val isMale = pendingIsMale
                if (text != null && isMale != null) {
                    speak(text, isMale)
                    pendingText = null
                    pendingIsMale = null
                }
            }
        } else {
            Log.e("SevenTicksTts", "Initialization failed")
        }
    }

    fun speak(text: String, isMale: Boolean = true) {
        if (!isInitialized) {
            pendingText = text
            pendingIsMale = isMale
            return
        }
        try {
            tts?.stop() // Ensure only one TTS session is active at a time
            
            val voices = tts?.voices
            if (voices != null && voices.isNotEmpty()) {
                val selectedVoice = findVoice(voices, isMale)
                if (selectedVoice != null) {
                    tts?.voice = selectedVoice
                }
            }

            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SevenTicksTtsUtterance")
        } catch (e: Exception) {
            Log.e("SevenTicksTts", "Error during speak", e)
        }
    }

    private fun findVoice(voices: Set<Voice>, isMale: Boolean): Voice? {
        val targetGender = if (isMale) "male" else "female"
        var voice = voices.find { 
            it.locale.language == "en" && it.name.lowercase().contains(targetGender) 
        }
        if (voice != null) return voice

        val maleNames = listOf("en-us-x-sfg", "en-us-x-iom", "en-us-x-jcd")
        val femaleNames = listOf("en-us-x-tpf", "en-us-x-iog", "en-us-x-iol")
        val targetList = if (isMale) maleNames else femaleNames

        for (name in targetList) {
            voice = voices.find { it.name.lowercase().contains(name) }
            if (voice != null) return voice
        }

        return voices.find { it.locale.language == "en" }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e("SevenTicksTts", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("SevenTicksTts", "Error during shutdown", e)
        }
    }
}
