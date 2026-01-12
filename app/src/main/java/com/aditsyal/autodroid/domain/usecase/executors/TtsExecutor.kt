package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class TtsExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private var textToSpeech: TextToSpeech? = null
    private var isInitialized = false

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val text = config["text"]?.toString()
                ?: throw IllegalArgumentException("Text is required for TTS")

            val language = config["language"]?.toString() ?: "en"
            val pitch = config["pitch"]?.toString()?.toFloatOrNull() ?: 1.0f
            val speechRate = config["speechRate"]?.toString()?.toFloatOrNull() ?: 1.0f
            val queueMode = config["queueMode"]?.toString()?.lowercase() ?: "flush"
            val utteranceId = config["utteranceId"]?.toString() ?: UUID.randomUUID().toString()

            speakText(text, language, pitch, speechRate, queueMode, utteranceId)
        }.onFailure { e ->
            Timber.e(e, "TTS execution failed")
        }
    }

    private suspend fun speakText(
        text: String,
        language: String,
        pitch: Float,
        speechRate: Float,
        queueMode: String,
        utteranceId: String
    ) {
        val tts = getTextToSpeech()

        // Set language
        val locale = when (language.lowercase()) {
            "en", "english" -> Locale.ENGLISH
            "es", "spanish" -> Locale("es", "ES")
            "fr", "french" -> Locale.FRENCH
            "de", "german" -> Locale.GERMAN
            "it", "italian" -> Locale.ITALIAN
            "ja", "japanese" -> Locale.JAPANESE
            "ko", "korean" -> Locale.KOREAN
            "zh", "chinese" -> Locale.CHINESE
            "pt", "portuguese" -> Locale("pt", "PT")
            "ru", "russian" -> Locale("ru", "RU")
            else -> {
                // Try to parse as locale string (e.g., "en_US")
                try {
                    val parts = language.split("_")
                    if (parts.size == 2) {
                        Locale(parts[0], parts[1])
                    } else {
                        Locale(language)
                    }
                } catch (e: Exception) {
                    Timber.w("Invalid language '$language', defaulting to English")
                    Locale.ENGLISH
                }
            }
        }

        val result = tts.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Timber.w("Language $language not supported, falling back to English")
            tts.setLanguage(Locale.ENGLISH)
        }

        // Set speech parameters
        tts.setPitch(pitch.coerceIn(0.5f, 2.0f))
        tts.setSpeechRate(speechRate.coerceIn(0.1f, 5.0f))

        // Set utterance progress listener for completion tracking
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String) {
                Timber.d("TTS started: $utteranceId")
            }

            override fun onDone(utteranceId: String) {
                Timber.d("TTS completed: $utteranceId")
            }

            override fun onError(utteranceId: String) {
                Timber.e("TTS error for utterance: $utteranceId")
            }
        })

        // Determine queue mode
        val queueModeInt = when (queueMode) {
            "add" -> TextToSpeech.QUEUE_ADD
            "flush" -> TextToSpeech.QUEUE_FLUSH
            else -> TextToSpeech.QUEUE_FLUSH
        }

        // Speak the text
        val speakResult = tts.speak(text, queueModeInt, null, utteranceId)

        if (speakResult == TextToSpeech.ERROR) {
            throw RuntimeException("TTS speak failed")
        }

        Timber.i("TTS queued: '$text' (language: $language, pitch: $pitch, rate: $speechRate)")
    }

    private suspend fun getTextToSpeech(): TextToSpeech {
        if (textToSpeech != null && isInitialized) {
            return textToSpeech!!
        }

        return suspendCancellableCoroutine { continuation ->
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    isInitialized = true
                    Timber.d("TextToSpeech initialized successfully")
                    continuation.resume(textToSpeech!!)
                } else {
                    Timber.e("TextToSpeech initialization failed")
                    continuation.resumeWithException(RuntimeException("TTS initialization failed"))
                }
            }
        }
    }

    // Method to stop current speech
    fun stopSpeech(): Result<Unit> {
        return runCatching {
            textToSpeech?.stop() ?: throw IllegalStateException("TTS not initialized")
            Timber.d("TTS stopped")
        }
    }

    // Method to check if TTS is currently speaking
    fun isSpeaking(): Boolean {
        return textToSpeech?.isSpeaking ?: false
    }

    // Method to get available languages
    fun getAvailableLanguages(): Set<Locale> {
        return textToSpeech?.availableLanguages ?: emptySet()
    }

    // Cleanup method (call when done with TTS)
    fun shutdown() {
        textToSpeech?.shutdown()
        textToSpeech = null
        isInitialized = false
        Timber.d("TTS shutdown")
    }
}