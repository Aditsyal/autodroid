package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import timber.log.Timber

class DeviceStateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        when (intent.action) {
            Intent.ACTION_BATTERY_CHANGED -> {
                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (level >= 0 && scale > 0) {
                    val batteryPct = (level / scale.toFloat() * 100).toInt()
                    Timber.d("Battery level changed: $batteryPct%")
                    // TODO: Trigger macros based on battery level constraints
                }
            }

            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                Timber.d("Airplane mode changed: $isAirplaneModeOn")
                // TODO: Trigger macros based on airplane mode
            }

            Intent.ACTION_DEVICE_STORAGE_LOW -> {
                Timber.d("Device storage low")
                // TODO: Trigger macros based on storage constraints
            }
        }
    }
}

