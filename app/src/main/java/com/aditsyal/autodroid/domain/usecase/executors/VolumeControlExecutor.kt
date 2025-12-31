package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.media.AudioManager
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class VolumeControlExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val streamTypeStr = config["stream"]?.toString() ?: "MUSIC"
            val volumeLevel = config["level"]?.toString()?.toIntOrNull() ?: 50 // Percentage

            if (volumeLevel !in 0..100) {
                val errorMsg = "Invalid volume level: $volumeLevel (must be 0-100)"
                Timber.e(errorMsg)
                throw IllegalArgumentException(errorMsg)
            }

            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null) {
                val errorMsg = "AudioManager not available"
                Timber.e(errorMsg)
                throw IllegalStateException(errorMsg)
            }

            val streamType = when (streamTypeStr.uppercase()) {
                "RING" -> AudioManager.STREAM_RING
                "NOTIFICATION" -> AudioManager.STREAM_NOTIFICATION
                "ALARM" -> AudioManager.STREAM_ALARM
                "VOICE_CALL" -> AudioManager.STREAM_VOICE_CALL
                "MUSIC" -> AudioManager.STREAM_MUSIC
                else -> {
                    Timber.w("Unknown stream type: $streamTypeStr, defaulting to MUSIC")
                    AudioManager.STREAM_MUSIC
                }
            }

            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val targetVolume = (maxVolume * (volumeLevel / 100f)).toInt().coerceIn(0, maxVolume)

            audioManager.setStreamVolume(streamType, targetVolume, AudioManager.FLAG_SHOW_UI)
            Timber.i("Volume set to $targetVolume (approx $volumeLevel%) for stream $streamTypeStr")
        }
    }
}