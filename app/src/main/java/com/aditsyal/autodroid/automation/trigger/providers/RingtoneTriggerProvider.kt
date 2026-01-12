package com.aditsyal.autodroid.automation.trigger.providers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import androidx.core.content.ContextCompat
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
class RingtoneTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider {

    override val type: String = "RINGTONE_PROFILE"

    private val scope = CoroutineScope(Dispatchers.IO)
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private var volumeChangeReceiver: BroadcastReceiver? = null
    private var isRegistered = false

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerVolumeChangeReceiver()
            Timber.d("Registered ringtone trigger ${trigger.id}: ${trigger.triggerConfig["profile"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register ringtone trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterVolumeChangeReceiver()
            }
            Timber.d("Unregistered ringtone trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister ringtone trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterVolumeChangeReceiver()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear ringtone triggers")
        }
    }

    private fun registerVolumeChangeReceiver() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            volumeChangeReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == "android.media.VOLUME_CHANGED_ACTION") {
                        val streamType = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_TYPE", -1)
                        val newVolume = intent.getIntExtra("android.media.EXTRA_VOLUME_STREAM_VALUE", -1)
                        val oldVolume = intent.getIntExtra("android.media.EXTRA_PREV_VOLUME_STREAM_VALUE", -1)

                        Timber.d("Volume changed: stream=$streamType, old=$oldVolume, new=$newVolume")

                        // Check relevant streams: RING, NOTIFICATION, ALARM
                        if (streamType in listOf(AudioManager.STREAM_RING, AudioManager.STREAM_NOTIFICATION, AudioManager.STREAM_ALARM)) {
                            checkRingtoneTriggers(streamType, newVolume, oldVolume)
                        }
                    }
                }
            }

            val filter = IntentFilter("android.media.VOLUME_CHANGED_ACTION")
            ContextCompat.registerReceiver(
                context,
                volumeChangeReceiver,
                filter,
                ContextCompat.RECEIVER_EXPORTED
            )
            isRegistered = true
            Timber.d("Registered volume change receiver")
        }
    }

    private fun unregisterVolumeChangeReceiver() {
        if (isRegistered) {
            volumeChangeReceiver?.let { receiver ->
                try {
                    context.unregisterReceiver(receiver)
                    Timber.d("Unregistered volume change receiver")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to unregister volume change receiver")
                }
            }
            volumeChangeReceiver = null
            isRegistered = false
        }
    }

    private fun checkRingtoneTriggers(streamType: Int, newVolume: Int, oldVolume: Int) {
        activeTriggers.values.forEach { trigger ->
            val targetProfile = trigger.triggerConfig["profile"]?.toString()
            val triggerOnChange = trigger.triggerConfig["triggerOnChange"]?.toString()?.toBoolean() ?: true

            if (targetProfile != null) {
                val shouldTrigger = when (targetProfile) {
                    "RINGTONE" -> streamType == AudioManager.STREAM_RING
                    "NOTIFICATION" -> streamType == AudioManager.STREAM_NOTIFICATION
                    "ALARM" -> streamType == AudioManager.STREAM_ALARM
                    "MEDIA" -> streamType == AudioManager.STREAM_MUSIC
                    "VOICE_CALL" -> streamType == AudioManager.STREAM_VOICE_CALL
                    "SYSTEM" -> streamType == AudioManager.STREAM_SYSTEM
                    else -> false
                }

                if (shouldTrigger) {
                    if (triggerOnChange && newVolume != oldVolume) {
                        // Trigger on volume change
                        notifyTrigger(trigger, mapOf(
                            "streamType" to streamType,
                            "oldVolume" to oldVolume,
                            "newVolume" to newVolume,
                            "profile" to targetProfile
                        ))
                    } else if (!triggerOnChange) {
                        // Trigger on specific volume levels
                        val targetVolume = trigger.triggerConfig["targetVolume"]?.toString()?.toIntOrNull()
                        if (targetVolume != null && newVolume == targetVolume) {
                            notifyTrigger(trigger, mapOf(
                                "streamType" to streamType,
                                "volume" to newVolume,
                                "profile" to targetProfile
                            ))
                        }
                    }
                }
            }
        }
    }

    private fun notifyTrigger(trigger: TriggerDTO, additionalData: Map<String, Any> = emptyMap()) {
        scope.launch {
            try {
                checkTriggersUseCase(type, additionalData + mapOf("fired_trigger_id" to trigger.id))
                Timber.d("Triggered ringtone change for trigger ${trigger.id}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to check ringtone trigger ${trigger.id}")
            }
        }
    }

    // Utility method to get current volume levels for all streams
    fun getCurrentVolumeLevels(): Map<String, Int> {
        return mapOf(
            "RINGTONE" to audioManager.getStreamVolume(AudioManager.STREAM_RING),
            "NOTIFICATION" to audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION),
            "ALARM" to audioManager.getStreamVolume(AudioManager.STREAM_ALARM),
            "MEDIA" to audioManager.getStreamVolume(AudioManager.STREAM_MUSIC),
            "VOICE_CALL" to audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL),
            "SYSTEM" to audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
        )
    }

    // Method to check if a profile is muted
    fun isProfileMuted(profile: String): Boolean {
        val volume = when (profile) {
            "RINGTONE" -> audioManager.getStreamVolume(AudioManager.STREAM_RING)
            "NOTIFICATION" -> audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION)
            "ALARM" -> audioManager.getStreamVolume(AudioManager.STREAM_ALARM)
            "MEDIA" -> audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            "VOICE_CALL" -> audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL)
            "SYSTEM" -> audioManager.getStreamVolume(AudioManager.STREAM_SYSTEM)
            else -> 0
        }
        return volume == 0
    }
}