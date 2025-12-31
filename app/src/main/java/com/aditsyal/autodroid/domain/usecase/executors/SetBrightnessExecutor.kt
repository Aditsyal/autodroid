package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class SetBrightnessExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val brightness = config["brightness"]?.toString()?.toIntOrNull() ?: 50
            if (brightness !in 0..100) {
                throw IllegalArgumentException("Brightness must be 0-100")
            }

            val brightnessValue = (brightness / 100f * 255).toInt().coerceIn(0, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue)
            Timber.i("Brightness set to $brightness%")
        }
    }
}