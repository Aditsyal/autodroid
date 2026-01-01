package com.aditsyal.autodroid.automation.trigger.providers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbManager
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

@Singleton
class UsbTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, BroadcastReceiver() {

    override val type: String = "USB_CONNECTION"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private var isRegistered = false

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerReceiverIfNeeded()
            Timber.d("Registered USB trigger ${trigger.id}: ${trigger.triggerConfig["event"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register USB trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterReceiver()
            }
            Timber.d("Unregistered USB trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister USB trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterReceiver()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear USB triggers")
        }
    }

    private fun registerReceiverIfNeeded() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            try {
                val filter = IntentFilter().apply {
                    addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED)
                    addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
                }
                context.registerReceiver(this, filter)
                isRegistered = true
                Timber.d("UsbTriggerProvider: Registered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register UsbTriggerProvider receiver")
            }
        }
    }

    private fun unregisterReceiver() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
                isRegistered = false
                Timber.d("UsbTriggerProvider: Unregistered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister UsbTriggerProvider receiver")
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
                    UsbManager.ACTION_USB_DEVICE_ATTACHED -> {
                        val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
                        device?.let {
                            Timber.d("USB device attached: ${it.deviceName}")
                            notifyTriggers("USB_ATTACHED", mapOf(
                                "deviceName" to it.deviceName,
                                "vendorId" to it.vendorId,
                                "productId" to it.productId
                            ))
                        }
                    }
                    UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                        val device = intent.getParcelableExtra<android.hardware.usb.UsbDevice>(UsbManager.EXTRA_DEVICE)
                        device?.let {
                            Timber.d("USB device detached: ${it.deviceName}")
                            notifyTriggers("USB_DETACHED", mapOf(
                                "deviceName" to it.deviceName,
                                "vendorId" to it.vendorId,
                                "productId" to it.productId
                            ))
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in UsbTriggerProvider.onReceive")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyTriggers(event: String, additionalData: Map<String, Any> = emptyMap()) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == event }
            .forEach { trigger ->
                notifyTrigger(trigger, additionalData)
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
}
