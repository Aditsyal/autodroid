package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.os.Build
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class MediaControlExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val action = config["action"]?.toString()?.lowercase() ?: "play_pause"
            val mediaAppPackage = config["package"]?.toString() // Optional: target specific media app

            performMediaAction(action, mediaAppPackage)
            Timber.i("Media control action executed: $action")
        }.onFailure { e ->
            Timber.e(e, "Media control execution failed")
        }
    }

    private fun performMediaAction(action: String, targetPackage: String?) {
        try {
            val keyCode = when (action) {
                "play", "pause", "play_pause" -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
                "next", "next_track" -> KeyEvent.KEYCODE_MEDIA_NEXT
                "previous", "prev", "previous_track" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
                "stop" -> KeyEvent.KEYCODE_MEDIA_STOP
                "fast_forward", "forward" -> KeyEvent.KEYCODE_MEDIA_FAST_FORWARD
                "rewind" -> KeyEvent.KEYCODE_MEDIA_REWIND
                else -> throw IllegalArgumentException("Unknown media action: $action")
            }

            sendMediaKeyEvent(keyCode, targetPackage)
        } catch (e: Exception) {
            Timber.e(e, "Failed to perform media action: $action")
            throw e
        }
    }

    private fun sendMediaKeyEvent(keyCode: Int, targetPackage: String?) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // Use MediaSessionManager for Lollipop and above
                val controllers = mediaSessionManager.getActiveSessions(null)

                if (controllers.isNotEmpty()) {
                    // Find target controller if package specified
                    val targetController = if (targetPackage != null) {
                        controllers.find { it.packageName == targetPackage }
                    } else {
                        // Use the first active controller (usually the most recent)
                        controllers.firstOrNull()
                    }

                    targetController?.let { controller ->
                        val keyEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
                        controller.dispatchMediaButtonEvent(keyEvent)

                        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
                        controller.dispatchMediaButtonEvent(upEvent)

                        Timber.d("Dispatched media key $keyCode to ${controller.packageName}")
                        return
                    }
                }
            }

            // Fallback: Send global media button intent
            sendGlobalMediaIntent(keyCode)

        } catch (e: SecurityException) {
            Timber.w(e, "Media session access denied, trying global intent fallback")
            sendGlobalMediaIntent(keyCode)
        } catch (e: Exception) {
            Timber.e(e, "Failed to send media key event")
            sendGlobalMediaIntent(keyCode)
        }
    }

    private fun sendGlobalMediaIntent(keyCode: Int) {
        try {
            val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(android.content.Intent.EXTRA_KEY_EVENT,
                    KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                addFlags(android.content.Intent.FLAG_RECEIVER_FOREGROUND)
            }

            context.sendBroadcast(intent)

            // Send key up event
            val upIntent = android.content.Intent(android.content.Intent.ACTION_MEDIA_BUTTON).apply {
                putExtra(android.content.Intent.EXTRA_KEY_EVENT,
                    KeyEvent(KeyEvent.ACTION_UP, keyCode))
                addFlags(android.content.Intent.FLAG_RECEIVER_FOREGROUND)
            }

            context.sendBroadcast(upIntent)

            Timber.d("Sent global media intent for key $keyCode")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send global media intent")
            throw RuntimeException("Unable to control media playback: ${e.message}")
        }
    }

    // Utility methods
    fun getActiveMediaSessions(): List<String> {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mediaSessionManager.getActiveSessions(null)
                    .map { it.packageName }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get active media sessions")
            emptyList()
        }
    }

    fun isMediaPlaying(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val controllers = mediaSessionManager.getActiveSessions(null)
                return controllers.any { controller ->
                    controller.playbackState?.state == android.media.session.PlaybackState.STATE_PLAYING
                }
            }
            false
        } catch (e: Exception) {
            Timber.e(e, "Failed to check media playing state")
            false
        }
    }
}