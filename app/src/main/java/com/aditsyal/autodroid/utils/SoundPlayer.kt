package com.aditsyal.autodroid.utils

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.provider.Settings
import com.aditsyal.autodroid.R
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Wrapper for MediaPlayer to ensure proper resource cleanup and singleton usage
 */
@Singleton
class SoundPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var mediaPlayer: MediaPlayer? = null

    suspend fun playSound(soundType: String = "DEFAULT", customUri: Uri? = null) {
        try {
            // Stop any existing playback
            stop()

            mediaPlayer = when (soundType.uppercase()) {
                "DEFAULT", "NOTIFICATION" -> MediaPlayer.create(context, Settings.System.DEFAULT_NOTIFICATION_URI)
                "RINGTONE" -> MediaPlayer.create(context, Settings.System.DEFAULT_RINGTONE_URI)
                "ALARM" -> MediaPlayer.create(context, Settings.System.DEFAULT_ALARM_ALERT_URI)
                else -> {
                    val uri = customUri ?: Settings.System.DEFAULT_NOTIFICATION_URI
                    MediaPlayer.create(context, uri)
                }
            }

            mediaPlayer?.start()
            Timber.i("Sound played: $soundType")
        } catch (e: Exception) {
            Timber.e(e, "Failed to play sound")
            cleanup()
        }
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            cleanup()
            Timber.i("Sound stopped")
        } catch (e: Exception) {
            Timber.e(e, "Failed to stop sound")
        }
    }

    private fun cleanup() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    // Cleanup on app destruction
    fun shutdown() {
        stop()
    }
}