package com.example.core.feedback

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FeedbackManager private constructor(context: Context) {

    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO)

    // --- State Support for UI ---
    private val _isPronunciationActive = MutableStateFlow(false)
    val isPronunciationActive: StateFlow<Boolean> = _isPronunciationActive.asStateFlow()

    private val _spokenText = MutableStateFlow("")
    val spokenText: StateFlow<String> = _spokenText.asStateFlow()

    private val _spokenTextRange = MutableStateFlow<Pair<Int, Int>?>(null)
    val spokenTextRange: StateFlow<Pair<Int, Int>?> = _spokenTextRange.asStateFlow()

    // --- Sound Pool and Caching ---
    private var soundPool: SoundPool? = null
    private val soundIds = mutableMapOf<String, Int>()
    private var isInitialized = false

    // --- Haptic Feedback Vibrator ---
    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Failed to access Vibrator service", e)
            null
        }
    }

    init {
        initSoundPool()
    }

    private fun initSoundPool() {
        scope.launch {
            try {
                val audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()

                soundPool = SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build()

                // Generate and cache sounds programmatically
                val soundTypes = listOf(
                    SoundType("easy", 200, "easy"),
                    SoundType("good", 250, "good"),
                    SoundType("hard", 200, "hard"),
                    SoundType("again", 300, "again"),
                    SoundType("typing", 15, "typing")
                )

                soundTypes.forEach { sound ->
                    val cacheFile = File(appContext.cacheDir, "feedback_${sound.key}.wav")
                    // Always regenerate or verify the existence of the file
                    AudioSynth.createWavFile(cacheFile, durationMs = sound.durationMs, type = sound.type)
                    
                    soundPool?.let { pool ->
                        val soundId = pool.load(cacheFile.absolutePath, 1)
                        if (soundId != 0) {
                            soundIds[sound.key] = soundId
                            Log.d("FeedbackManager", "Loaded sound programmatically: ${sound.key} with ID: $soundId")
                        }
                    }
                }
                isInitialized = true
            } catch (e: Exception) {
                Log.e("FeedbackManager", "Failed to initialize SoundPool and generate sounds", e)
            }
        }
    }

    // --- Play Sound Feedback ---
    fun playSound(type: String) {
        if (!isInitialized) {
            Log.w("FeedbackManager", "SoundPool not initialized yet, skipping sound play: $type")
            return
        }
        val soundId = soundIds[type.lowercase()]
        if (soundId != null && soundId != 0) {
            scope.launch {
                try {
                    // Play sound with left/right volume = 1.0, priority = 1, loop = 0, rate = 1.0f
                    soundPool?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                } catch (e: Exception) {
                    Log.e("FeedbackManager", "Error playing sound feedback: $type", e)
                }
            }
        } else {
            Log.e("FeedbackManager", "Sound not found or failed to load: $type")
        }
    }

    // --- Haptic Feedback Controls ---
    fun vibrateLight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrate(15, 60, VibrationEffect.EFFECT_TICK)
        } else {
            vibrate(15, 60, -1)
        }
    }

    fun vibrateMedium() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrate(40, 120, VibrationEffect.EFFECT_CLICK)
        } else {
            vibrate(40, 120, -1)
        }
    }

    fun vibrateHeavy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrate(80, 255, VibrationEffect.EFFECT_HEAVY_CLICK)
        } else {
            vibrate(80, 255, -1)
        }
    }

    private fun vibrate(durationMs: Long, amplitude: Int, effectId: Int) {
        val activeVibrator = vibrator ?: return
        try {
            if (!activeVibrator.hasVibrator()) return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val effect = try {
                    if (effectId != -1) {
                        VibrationEffect.createPredefined(effectId)
                    } else {
                        VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                    }
                } catch (e: Exception) {
                    VibrationEffect.createOneShot(durationMs, amplitude.coerceIn(1, 255))
                }
                activeVibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                activeVibrator.vibrate(durationMs)
            }
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Error executing haptic vibration", e)
        }
    }

    // --- UI Pronunciation and Highlight States ---
    fun setPronunciationActive(active: Boolean) {
        _isPronunciationActive.value = active
    }

    fun setSpokenText(text: String) {
        _spokenText.value = text
    }

    fun setSpokenTextRange(range: Pair<Int, Int>?) {
        _spokenTextRange.value = range
    }

    fun resetHighlightState() {
        _isPronunciationActive.value = false
        _spokenText.value = ""
        _spokenTextRange.value = null
    }

    // --- Resource Clean Up ---
    fun release() {
        try {
            soundPool?.release()
            soundPool = null
            soundIds.clear()
            isInitialized = false
        } catch (e: Exception) {
            Log.e("FeedbackManager", "Error releasing SoundPool", e)
        }
    }

    private data class SoundType(val key: String, val durationMs: Int, val type: String)

    companion object {
        @Volatile
        private var INSTANCE: FeedbackManager? = null

        fun getInstance(context: Context): FeedbackManager {
            return INSTANCE ?: synchronized(this) {
                val instance = FeedbackManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}

// --- High-fidelity Waveform Synthesizer to PCM WAV Files ---
private object AudioSynth {
    fun createWavFile(file: File, sampleRate: Int = 22050, durationMs: Int, type: String) {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val dataSize = numSamples * 2 // 16-bit = 2 bytes per sample
        val totalSize = 36 + dataSize

        val header = ByteBuffer.allocate(44).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            put("RIFF".toByteArray())
            putInt(totalSize)
            put("WAVE".toByteArray())
            put("fmt ".toByteArray())
            putInt(16) // Subchunk1Size
            putShort(1.toShort()) // AudioFormat = PCM
            putShort(1.toShort()) // NumChannels = Mono
            putInt(sampleRate)
            putInt(sampleRate * 2) // ByteRate = sampleRate * blockAlign
            putShort(2.toShort()) // BlockAlign = 2
            putShort(16.toShort()) // BitsPerSample = 16
            put("data".toByteArray())
            putInt(dataSize)
        }.array()

        val data = ByteArray(dataSize)
        val buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sampleValue = when (type) {
                "easy" -> {
                    // Ascending chord sequence: C5 (523.25Hz) -> E5 (659.25Hz) -> G5 (783.99Hz)
                    val freq = if (i < numSamples / 3) {
                        523.25
                    } else if (i < 2 * numSamples / 3) {
                        659.25
                    } else {
                        783.99
                    }
                    val envelope = getEnvelope(i, numSamples)
                    (Math.sin(2.0 * Math.PI * freq * t) * 16000.0 * envelope).toInt().toShort()
                }
                "good" -> {
                    // Pleasant soft chime: C5 (523.25Hz) + G5 (783.99Hz)
                    val freq1 = 523.25
                    val freq2 = 783.99
                    val envelope = getEnvelope(i, numSamples)
                    val wave = (Math.sin(2.0 * Math.PI * freq1 * t) + Math.sin(2.0 * Math.PI * freq2 * t)) / 2.0
                    (wave * 15000.0 * envelope).toInt().toShort()
                }
                "hard" -> {
                    // Flat/neutral frequency: E4 (329.63Hz)
                    val freq = 329.63
                    val envelope = getEnvelope(i, numSamples)
                    (Math.sin(2.0 * Math.PI * freq * t) * 12000.0 * envelope).toInt().toShort()
                }
                "again" -> {
                    // Warning low beep alert: G3 (196.00Hz)
                    val freq = 196.00
                    val envelope = getEnvelope(i, numSamples)
                    (Math.sin(2.0 * Math.PI * freq * t) * 14000.0 * envelope).toInt().toShort()
                }
                "typing" -> {
                    // Sharp, short, organic tick sound (15ms duration): high freq with rapid exponential decay
                    val freq = 1500.0
                    val envelope = Math.exp(-250.0 * t)
                    (Math.sin(2.0 * Math.PI * freq * t) * 9000.0 * envelope).toInt().toShort()
                }
                else -> 0.toShort()
            }
            buffer.putShort(sampleValue)
        }

        try {
            FileOutputStream(file).use { fos ->
                fos.write(header)
                fos.write(data)
            }
        } catch (e: Exception) {
            Log.e("AudioSynth", "Error writing wav file for type $type", e)
        }
    }

    private fun getEnvelope(i: Int, total: Int): Double {
        val attack = (total * 0.08).toInt().coerceAtLeast(1)
        val decay = (total * 0.25).toInt().coerceAtLeast(1)
        return when {
            i < attack -> i.toDouble() / attack
            i > total - decay -> (total - i).toDouble() / decay
            else -> 1.0
        }
    }
}
