package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.BatteryManager
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class DeviceStateReceiver : BroadcastReceiver() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        val pendingResult = goAsync()
        val scope = CoroutineScope(Dispatchers.IO)

        scope.launch {
            try {
                when (intent.action) {
                    Intent.ACTION_BATTERY_CHANGED -> {
                        try {
                            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                            if (level >= 0 && scale > 0) {
                                val batteryPct = (level / scale.toFloat() * 100).toInt()
                                Timber.d("Battery level changed: $batteryPct%")
                                checkTriggersUseCase(
                                    "SYSTEM_EVENT",
                                    mapOf("event" to "BATTERY_CHANGED", "level" to batteryPct)
                                )
                            }
                        } catch (e: Exception) {
                            Timber.e(e, "Error processing battery changed event")
                        }
                    }

                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        try {
                            val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                            Timber.d("Airplane mode changed: $isAirplaneModeOn")
                            checkTriggersUseCase(
                                "SYSTEM_EVENT",
                                mapOf("event" to "AIRPLANE_MODE", "state" to isAirplaneModeOn)
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error processing airplane mode changed event")
                        }
                    }

                    Intent.ACTION_DEVICE_STORAGE_LOW -> {
                        try {
                            Timber.d("Device storage low")
                            checkTriggersUseCase(
                                "SYSTEM_EVENT",
                                mapOf("event" to "STORAGE_LOW")
                            )
                        } catch (e: Exception) {
                            Timber.e(e, "Error processing storage low event")
                        }
                    }
                    
                    else -> {
                        Timber.d("Unhandled broadcast action: ${intent.action}")
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error handling broadcast in DeviceStateReceiver")
            } finally {
                pendingResult.finish()
            }
        }
    }
}

