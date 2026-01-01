package com.aditsyal.autodroid.domain.usecase.executors

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class TetheringExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val tetherType = config["type"]?.toString() ?: "WIFI"
            val enable = config["enabled"] as? Boolean ?: true
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
                    val errorMsg = "Missing CHANGE_NETWORK_STATE permission"
                    Timber.e(errorMsg)
                    throw SecurityException(errorMsg)
                }
            }
            
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            if (connectivityManager == null) {
                val errorMsg = "ConnectivityManager not available"
                Timber.e(errorMsg)
                throw IllegalStateException(errorMsg)
            }
            
            when (tetherType.uppercase()) {
                "WIFI" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val errorMsg = "WiFi tethering is restricted on Android 10+. Please use system settings."
                        Timber.w(errorMsg)
                        throw UnsupportedOperationException(errorMsg)
                    } else {
                        Timber.w("WiFi tethering control is restricted on older Android versions. User must enable/disable manually.")
                        throw UnsupportedOperationException("WiFi tethering requires manual user intervention")
                    }
                }
                "USB" -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val errorMsg = "USB tethering is restricted on Android 10+. Please use system settings."
                        Timber.w(errorMsg)
                        throw UnsupportedOperationException(errorMsg)
                    } else {
                        Timber.w("USB tethering control is restricted on older Android versions. User must enable/disable manually.")
                        throw UnsupportedOperationException("USB tethering requires manual user intervention")
                    }
                }
                "BLUETOOTH" -> {
                    Timber.w("Bluetooth tethering is not supported on this platform")
                    throw UnsupportedOperationException("Bluetooth tethering is not supported")
                }
                else -> {
                    throw IllegalArgumentException("Unknown tether type: $tetherType")
                }
            }
        }
    }
}
