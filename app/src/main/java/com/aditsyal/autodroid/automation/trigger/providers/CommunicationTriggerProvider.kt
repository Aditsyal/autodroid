package com.aditsyal.autodroid.automation.trigger.providers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.telephony.TelephonyManager
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
 * Trigger provider for communication events:
 * - Call received
 * - Call ended
 * - Missed call
 * - SMS received
 */
@Singleton
class CommunicationTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, BroadcastReceiver() {

    override val type: String = "COMMUNICATION"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private var isRegistered = false
    private var lastCallState = TelephonyManager.CALL_STATE_IDLE
    private var lastCallNumber: String? = null

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerReceiverIfNeeded()
            Timber.d("Registered communication trigger ${trigger.id}: ${trigger.triggerConfig["event"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register communication trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterReceiver()
            }
            Timber.d("Unregistered communication trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister communication trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterReceiver()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear communication triggers")
        }
    }

    private fun registerReceiverIfNeeded() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            try {
                val filter = IntentFilter().apply {
                    addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED)
                    addAction(android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
                }
                context.registerReceiver(this, filter)
                isRegistered = true
                Timber.d("CommunicationTriggerProvider: Registered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register CommunicationTriggerProvider receiver")
            }
        }
    }

    private fun unregisterReceiver() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
                isRegistered = false
                Timber.d("CommunicationTriggerProvider: Unregistered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister CommunicationTriggerProvider receiver")
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
                    TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {
                        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                        val phoneNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
                        
                        when (state) {
                            TelephonyManager.EXTRA_STATE_RINGING -> {
                                lastCallNumber = phoneNumber
                                Timber.d("Call received from: $phoneNumber")
                                notifyCallReceived(phoneNumber)
                            }
                            TelephonyManager.EXTRA_STATE_OFFHOOK -> {
                                // Call answered
                                Timber.d("Call answered")
                            }
                            TelephonyManager.EXTRA_STATE_IDLE -> {
                                if (lastCallState == TelephonyManager.CALL_STATE_RINGING) {
                                    // Missed call
                                    Timber.d("Missed call from: $lastCallNumber")
                                    notifyMissedCall(lastCallNumber)
                                } else if (lastCallState == TelephonyManager.CALL_STATE_OFFHOOK) {
                                    // Call ended
                                    Timber.d("Call ended")
                                    notifyCallEnded(lastCallNumber)
                                }
                                lastCallNumber = null
                            }
                        }
                        lastCallState = when (state) {
                            TelephonyManager.EXTRA_STATE_RINGING -> TelephonyManager.CALL_STATE_RINGING
                            TelephonyManager.EXTRA_STATE_OFFHOOK -> TelephonyManager.CALL_STATE_OFFHOOK
                            else -> TelephonyManager.CALL_STATE_IDLE
                        }
                    }
                    android.provider.Telephony.Sms.Intents.SMS_RECEIVED_ACTION -> {
                        val messages = android.provider.Telephony.Sms.Intents.getMessagesFromIntent(intent)
                        messages?.forEach { message ->
                            val sender = message.displayOriginatingAddress
                            val body = message.displayMessageBody
                            Timber.d("SMS received from: $sender")
                            notifySmsReceived(sender, body)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in CommunicationTriggerProvider.onReceive")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun notifyCallReceived(phoneNumber: String?) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "CALL_RECEIVED" }
            .forEach { trigger ->
                val requiredNumber = trigger.triggerConfig["phoneNumber"]?.toString()
                if (requiredNumber == null || phoneNumber == requiredNumber) {
                    notifyTrigger(trigger, mapOf("phoneNumber" to (phoneNumber ?: "")))
                }
            }
    }

    private fun notifyCallEnded(phoneNumber: String?) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "CALL_ENDED" }
            .forEach { trigger ->
                notifyTrigger(trigger, mapOf("phoneNumber" to (phoneNumber ?: "")))
            }
    }

    private fun notifyMissedCall(phoneNumber: String?) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "MISSED_CALL" }
            .forEach { trigger ->
                val requiredNumber = trigger.triggerConfig["phoneNumber"]?.toString()
                if (requiredNumber == null || phoneNumber == requiredNumber) {
                    notifyTrigger(trigger, mapOf("phoneNumber" to (phoneNumber ?: "")))
                }
            }
    }

    private fun notifySmsReceived(sender: String, body: String) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "SMS_RECEIVED" }
            .forEach { trigger ->
                val requiredSender = trigger.triggerConfig["phoneNumber"]?.toString()
                if (requiredSender == null || sender == requiredSender) {
                    notifyTrigger(trigger, mapOf(
                        "phoneNumber" to sender,
                        "message" to body
                    ))
                }
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

