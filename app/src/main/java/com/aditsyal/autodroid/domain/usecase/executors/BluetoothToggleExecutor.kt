package com.aditsyal.autodroid.domain.usecase.executors

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class BluetoothToggleExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val enable = config["enabled"] as? Boolean ?: true

            // Check for permissions
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    val errorMsg = "Missing BLUETOOTH_CONNECT permission"
                    Timber.e(errorMsg)
                    throw SecurityException(errorMsg)
                }
            } else {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                    val errorMsg = "Missing BLUETOOTH_ADMIN permission"
                    Timber.e(errorMsg)
                    throw SecurityException(errorMsg)
                }
            }

            val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            val bluetoothAdapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

            if (bluetoothAdapter == null) {
                val errorMsg = "BluetoothAdapter not available on this device"
                Timber.e(errorMsg)
                throw IllegalStateException(errorMsg)
            }

            if (enable) {
                if (!bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.enable()
                    Timber.i("Bluetooth enabled")
                }
            } else {
                if (bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.disable()
                    Timber.i("Bluetooth disabled")
                }
            }
        }
    }
}