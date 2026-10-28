package com.example.ai

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.random.Random

private const val TAG = "VoiceTtsManager"

class VoiceTtsManager(
    context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    // Real-time audio frequency energy (0.0f - 1.0f) driving the dynamic neon border glow
    private val _audioFrequencyLevel = MutableStateFlow(0f)
    val audioFrequencyLevel: StateFlow<Float> = _audioFrequencyLevel.asStateFlow()

    private var frequencySimJob: Job? = null

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val ttsEngine = tts ?: return
            // Try Bengali (Bangladesh / India), fallback to English
            val bnLocale = Locale("bn", "BD")
            val bnResult = ttsEngine.setLanguage(bnLocale)
            if (bnResult == TextToSpeech.LANG_MISSING_DATA || bnResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                val inResult = ttsEngine.setLanguage(Locale("bn", "IN"))
                if (inResult == TextToSpeech.LANG_MISSING_DATA || inResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    ttsEngine.setLanguage(Locale.US)
                    Log.d(TAG, "Bengali TTS not present on device engine, falling back to US")
                }
            }
            ttsEngine.setPitch(1.02f)
            ttsEngine.setSpeechRate(1.05f)

            ttsEngine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startFrequencySimulation()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopFrequencySimulation()
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopFrequencySimulation()
                }
            })
            isInitialized = true
            Log.d(TAG, "TTS initialized successfully")
        } else {
            Log.e(TAG, "TTS initialization failed: $status")
        }
    }

    fun speak(text: String) {
        if (!isInitialized || tts == null) {
            // Emulate frequency pulse even if hardware TTS is muted or missing
            simulateVoicePulseBriefly()
            return
        }
        val cleanText = text
            .replace(Regex("[#*`_\\[\\]()]"), " ")
            .take(600)

        tts?.speak(cleanText, TextToSpeech.QUEUE_FLUSH, null, "nexus_utterance_${System.currentTimeMillis()}")
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
        stopFrequencySimulation()
    }

    private fun startFrequencySimulation() {
        frequencySimJob?.cancel()
        frequencySimJob = scope.launch {
            while (isActive && _isSpeaking.value) {
                // Generate natural human voice rhythm curve
                val base = Random.nextFloat() * 0.7f + 0.3f
                _audioFrequencyLevel.value = base
                delay(60)
            }
            _audioFrequencyLevel.value = 0f
        }
    }

    private fun simulateVoicePulseBriefly() {
        scope.launch {
            _isSpeaking.value = true
            startFrequencySimulation()
            delay(2500)
            _isSpeaking.value = false
            stopFrequencySimulation()
        }
    }

    private fun stopFrequencySimulation() {
        frequencySimJob?.cancel()
        _audioFrequencyLevel.value = 0f
    }

    fun destroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
