package com.example.core.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.util.Log
import java.util.Locale
import kotlinx.coroutines.*

class SevenTicksTtsManager(private val context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private var pendingText: String? = null
    private var pendingIsMale: Boolean? = null
    private var utteranceDoneCallback: (() -> Unit)? = null

    private var currentText: String = ""
    private val mainScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var highlightJob: Job? = null

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
                
                tts?.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        com.example.core.feedback.FeedbackManager.getInstance(context).setPronunciationActive(true)
                        mainScope.launch {
                            startPacingHighlight(currentText)
                        }
                    }
                    override fun onDone(utteranceId: String?) {
                        highlightJob?.cancel()
                        val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
                        fm.setPronunciationActive(false)
                        fm.setSpokenTextRange(null)
                        utteranceDoneCallback?.invoke()
                    }
                    override fun onError(utteranceId: String?) {
                        highlightJob?.cancel()
                        val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
                        fm.setPronunciationActive(false)
                        fm.setSpokenTextRange(null)
                        utteranceDoneCallback?.invoke()
                    }
                    override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                        // Bypassed in favor of the beautifully paced custom highlight pacing system
                    }
                })

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
            highlightJob?.cancel()
            currentText = text
            tts?.stop() // Ensure only one TTS session is active at a time
            
            val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
            fm.setSpokenText(text)
            fm.setSpokenTextRange(null)
            
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

    suspend fun speakSuspend(text: String, isMale: Boolean): Boolean {
        if (!isInitialized) {
            kotlinx.coroutines.delay(100)
            if (!isInitialized) return false
        }
        return kotlinx.coroutines.suspendCancellableCoroutine { continuation ->
            highlightJob?.cancel()
            currentText = text
            utteranceDoneCallback = {
                if (continuation.isActive) {
                    continuation.resume(true) {}
                }
            }
            try {
                val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
                fm.setSpokenText(text)
                fm.setSpokenTextRange(null)

                val voices = tts?.voices
                if (voices != null && voices.isNotEmpty()) {
                    val selectedVoice = findVoice(voices, isMale)
                    if (selectedVoice != null) {
                        tts?.voice = selectedVoice
                    }
                }
                val utteranceId = "SevenTicksTts_${System.currentTimeMillis()}"
                val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
                if (result == TextToSpeech.ERROR) {
                    if (continuation.isActive) {
                        continuation.resume(false) {}
                    }
                }
            } catch (e: Exception) {
                Log.e("SevenTicksTts", "Error during speakSuspend", e)
                if (continuation.isActive) {
                    continuation.resume(false) {}
                }
            }
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
            highlightJob?.cancel()
            val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
            fm.setPronunciationActive(false)
            fm.setSpokenTextRange(null)
            utteranceDoneCallback = null
            tts?.stop()
        } catch (e: Exception) {
            Log.e("SevenTicksTts", "Error stopping TTS", e)
        }
    }

    fun shutdown() {
        try {
            highlightJob?.cancel()
            val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
            fm.setPronunciationActive(false)
            fm.setSpokenTextRange(null)
            utteranceDoneCallback = null
            tts?.stop()
            tts?.shutdown()
            tts = null
            isInitialized = false
        } catch (e: Exception) {
            Log.e("SevenTicksTts", "Error during shutdown", e)
        }
    }

    private fun startPacingHighlight(text: String) {
        highlightJob?.cancel()
        val fm = com.example.core.feedback.FeedbackManager.getInstance(context)
        
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            fm.setSpokenTextRange(null)
            return
        }
        
        // If it's a single word, just highlight the whole word immediately
        if (!trimmed.contains(" ")) {
            fm.setSpokenTextRange(Pair(0, text.length))
            return
        }

        // Find exact word boundaries
        val boundaries = mutableListOf<Pair<Int, Int>>()
        var inWord = false
        var start = 0
        for (i in text.indices) {
            val char = text[i]
            val isCharWord = char.isLetterOrDigit() || char == '\'' || char == '-'
            if (isCharWord) {
                if (!inWord) {
                    start = i
                    inWord = true
                }
            } else {
                if (inWord) {
                    boundaries.add(Pair(start, i))
                    inWord = false
                }
            }
        }
        if (inWord) {
            boundaries.add(Pair(start, text.length))
        }

        if (boundaries.isEmpty()) return

        highlightJob = mainScope.launch {
            // Give a slight delay of 120ms to align with physical TTS audio initiation
            delay(120)
            
            for (range in boundaries) {
                val wordLength = range.second - range.first
                fm.setSpokenTextRange(range)
                
                // Beautifully calculated duration per word: base 180ms + 42ms per character
                val delayTime = 180L + (wordLength * 42L)
                delay(delayTime)
            }
        }
    }
}
