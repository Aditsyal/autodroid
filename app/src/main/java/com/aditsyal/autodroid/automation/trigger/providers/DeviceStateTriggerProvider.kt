package com.aditsyal.autodroid.automation.trigger.providers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.PowerManager
import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trigger provider for device state events:
 * - Screen on/off
 * - Device locked/unlocked
 * - Charging connected/disconnected
 * - Battery level thresholds
 */
@Singleton
class DeviceStateTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, BroadcastReceiver() {

    override val type: String = "DEVICE_STATE"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private var isRegistered = false

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerReceiverIfNeeded()
            Timber.d("Registered device state trigger ${trigger.id}: ${trigger.triggerConfig["event"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register device state trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterReceiver()
            }
            Timber.d("Unregistered device state trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister device state trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterReceiver()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear device state triggers")
        }
    }

    private fun registerReceiverIfNeeded() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            try {
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_USER_PRESENT) // Device unlocked
                    addAction(Intent.ACTION_BATTERY_CHANGED)
                    addAction(Intent.ACTION_POWER_CONNECTED)
                    addAction(Intent.ACTION_POWER_DISCONNECTED)
                    addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
                    addAction(Intent.ACTION_HEADSET_PLUG)
                }
                context.registerReceiver(this, filter)
                isRegistered = true
                Timber.d("DeviceStateTriggerProvider: Registered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register DeviceStateTriggerProvider receiver")
            }
        }
    }

    private fun unregisterReceiver() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
                isRegistered = false
                Timber.d("DeviceStateTriggerProvider: Unregistered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister DeviceStateTriggerProvider receiver")
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        val pendingResult = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    Intent.ACTION_SCREEN_ON -> {
                        Timber.d("Screen turned on")
                        notifyTriggers("SCREEN_ON")
                    }
                    Intent.ACTION_SCREEN_OFF -> {
                        Timber.d("Screen turned off")
                        notifyTriggers("SCREEN_OFF")
                        // When screen turns off, device is typically locked
                        notifyTriggers("DEVICE_LOCKED")
                    }
                    Intent.ACTION_USER_PRESENT -> {
                        Timber.d("Device unlocked")
                        notifyTriggers("DEVICE_UNLOCKED")
                    }
                    Intent.ACTION_POWER_CONNECTED -> {
                        Timber.d("Charging connected")
                        notifyTriggers("CHARGING_CONNECTED")
                    }
                    Intent.ACTION_POWER_DISCONNECTED -> {
                        Timber.d("Charging disconnected")
                        notifyTriggers("CHARGING_DISCONNECTED")
                    }
                    Intent.ACTION_BATTERY_CHANGED -> {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)

                        if (level >= 0 && scale > 0) {
                            val batteryPct = (level / scale.toFloat() * 100).toInt()
                            checkBatteryLevelTriggers(batteryPct)
                        }

                        if (status == BatteryManager.BATTERY_STATUS_FULL) {
                            Timber.d("Battery fully charged")
                            notifyTriggers("FULLY_CHARGED")
                        }
                    }
                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        val isEnabled = isAirplaneModeOn()
                        Timber.d("Airplane mode changed: $isEnabled")
                        val event = if (isEnabled) "AIRPLANE_MODE_ON" else "AIRPLANE_MODE_OFF"
                        activeTriggers.values
                            .filter { it.triggerConfig["event"] == event }
                            .forEach { trigger ->
                                notifyTrigger(trigger, mapOf("enabled" to isEnabled))
                            }
                    }
                    Intent.ACTION_HEADSET_PLUG -> {
                        val state = intent.getIntExtra("state", -1)
                        val hasMicrophone = intent.getIntExtra("microphone", -1) == 1
                        val event = if (state == 1) "HEADPHONES_CONNECTED" else "HEADPHONES_DISCONNECTED"
                        Timber.d("Headphones $event, hasMicrophone: $hasMicrophone")
                        activeTriggers.values
                            .filter { it.triggerConfig["event"] == event }
                            .forEach { trigger ->
                                notifyTrigger(trigger, mapOf("hasMicrophone" to hasMicrophone))
                            }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in DeviceStateTriggerProvider.onReceive")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun checkBatteryLevelTriggers(currentLevel: Int) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "BATTERY_LEVEL" }
            .forEach { trigger ->
                val threshold = trigger.triggerConfig["threshold"]?.toString()?.toIntOrNull()
                val operator = trigger.triggerConfig["operator"]?.toString() ?: "below"

                if (threshold != null) {
                    val shouldTrigger = when (operator.lowercase()) {
                        "below", "less_than" -> currentLevel < threshold
                        "above", "greater_than" -> currentLevel > threshold
                        "equals" -> currentLevel == threshold
                        else -> false
                    }

                    if (shouldTrigger) {
                        notifyTrigger(trigger, mapOf("level" to currentLevel, "threshold" to threshold))
                    }
                }
            }
    }

    private fun notifyTriggers(event: String) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == event }
            .forEach { trigger ->
                notifyTrigger(trigger)
            }
    }

    private fun notifyTrigger(trigger: TriggerDTO, additionalData: Map<String, Any> = emptyMap()) {
        scope.launch {
            try {
                checkTriggersUseCase(type, additionalData + mapOf("fired_trigger_id" to trigger.id))
            } catch (e: Exception) {
                Timber.e(e, "Failed to check trigger ${trigger.id}")
            }
        }
    }

    private fun isAirplaneModeOn(): Boolean {
        return try {
            android.provider.Settings.Global.getInt(
                context.contentResolver,
                android.provider.Settings.Global.AIRPLANE_MODE_ON,
                0
            ) != 0
        } catch (e: Exception) {
            Timber.e(e, "Failed to check airplane mode")
            false
        }
    }
}