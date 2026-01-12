package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class ScreenTimeoutExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val timeoutMinutes = config["timeoutMinutes"]?.toString()?.toFloatOrNull()
                ?: throw IllegalArgumentException("timeoutMinutes is required for screen timeout")

            val timeoutMs = (timeoutMinutes * 60 * 1000).toInt()

            // Validate timeout range (1 minute to 30 minutes)
            if (timeoutMs < 60000 || timeoutMs > 30 * 60 * 1000) {
                throw IllegalArgumentException("Screen timeout must be between 1 and 30 minutes")
            }

            setScreenTimeout(timeoutMs)
            Timber.i("Screen timeout set to ${timeoutMinutes} minutes")
        }.onFailure { e ->
            Timber.e(e, "Failed to set screen timeout")
        }
    }

    private fun setScreenTimeout(timeoutMs: Int) {
        try {
            // Check if we have WRITE_SETTINGS permission (required for Android 6.0+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(context)) {
                    throw SecurityException("WRITE_SETTINGS permission required. User must grant permission manually.")
                }
            }

            // Set the screen off timeout
            val result = Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                timeoutMs
            )

            if (!result) {
                throw RuntimeException("Failed to write screen timeout setting")
            }

            // Verify the setting was applied
            val currentTimeout = Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                -1
            )

            if (currentTimeout != timeoutMs) {
                throw RuntimeException("Screen timeout setting was not applied correctly")
            }

        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            // Fallback: try using Settings.Global for newer Android versions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                try {
                    val result = Settings.Global.putInt(
                        context.contentResolver,
                        "screen_off_timeout",
                        timeoutMs
                    )
                    if (!result) {
                        throw RuntimeException("Failed to set screen timeout via Settings.Global")
                    }
                } catch (e2: Exception) {
                    Timber.e(e2, "Settings.Global approach also failed")
                    throw e
                }
            } else {
                throw e
            }
        }
    }

    // Utility method to get current screen timeout
    fun getCurrentScreenTimeout(): Int {
        return try {
            Settings.System.getInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                30000 // Default 30 seconds
            )
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current screen timeout")
            30000
        }
    }

    // Utility method to get screen timeout in minutes
    fun getCurrentScreenTimeoutMinutes(): Float {
        return getCurrentScreenTimeout() / 1000f / 60f
    }

    // Method to check if we can write settings
    fun canWriteSettings(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.System.canWrite(context)
        } else {
            true // Pre-Marshmallow doesn't require special permission
        }
    }
}