package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class ToggleAirplaneModeExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val enable = config["enabled"] as? Boolean ?: true
            
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR1) {
                val isEnabled = Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    0
                ) != 0
                
                if (isEnabled == enable) {
                    Timber.i("Airplane mode is already ${if (enable) "enabled" else "disabled"}")
                    return@runCatching
                }
                
                Settings.Global.putInt(
                    context.contentResolver,
                    Settings.Global.AIRPLANE_MODE_ON,
                    if (enable) 1 else 0
                )
                
                val intent = android.content.Intent(android.provider.Settings.ACTION_AIRPLANE_MODE_SETTINGS).apply {
                    flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
                
                Timber.i("Airplane mode set to: $enable")
            } else {
                val errorMsg = "Airplane mode toggle requires Android 4.2+"
                Timber.w(errorMsg)
                throw UnsupportedOperationException(errorMsg)
            }
        }
    }
}
