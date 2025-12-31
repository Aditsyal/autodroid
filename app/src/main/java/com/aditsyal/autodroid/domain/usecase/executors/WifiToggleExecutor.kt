package com.aditsyal.autodroid.domain.usecase.executors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class WifiToggleExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val enable = config["enabled"] as? Boolean ?: true
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? android.net.wifi.WifiManager

            if (wifiManager == null) {
                val errorMsg = "WifiManager not available"
                Timber.e(errorMsg)
                throw IllegalStateException(errorMsg)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = enable
                Timber.i("WiFi set to: $enable")
            } else {
                val errorMsg = "Toggling WiFi is restricted on Android Q+. Please use system settings."
                Timber.w(errorMsg)
                throw UnsupportedOperationException(errorMsg)
            }
        }
    }
}