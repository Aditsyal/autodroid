package com.aditsyal.autodroid.automation.trigger.providers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
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
class MusicTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, AudioManager.OnAudioFocusChangeListener {

    override val type: String = "MUSIC_EVENT"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
    private var isRegistered = false
    private var isMusicPlaying = false

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerListenersIfNeeded()
            Timber.d("Registered music trigger ${trigger.id}: ${trigger.triggerConfig["event"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register music trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterListeners()
            }
            Timber.d("Unregistered music trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister music trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterListeners()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear music triggers")
        }
    }

    private fun registerListenersIfNeeded() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            try {
                audioManager?.let { am ->
                    val result = am.requestAudioFocus(
                        this,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN
                    )
                    if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                        Timber.d("MusicTriggerProvider: Audio focus granted")
                    } else {
                        Timber.w("MusicTriggerProvider: Audio focus request failed")
                    }
                }
                
                val filter = IntentFilter().apply {
                    addAction("com.android.music.metachanged")
                    addAction("com.android.music.playstatechanged")
                    addAction("com.android.music.playbackcomplete")
                    addAction("com.spotify.music.metadatachanged")
                    addAction("com.spotify.music.playbackcomplete")
                    addAction("android.intent.action.MEDIA_BUTTON")
                }
                context.registerReceiver(musicStateReceiver, filter)
                isRegistered = true
                Timber.d("MusicTriggerProvider: Registered listeners")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register MusicTriggerProvider listeners")
            }
        }
    }

    private fun unregisterListeners() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(musicStateReceiver)
                audioManager?.abandonAudioFocus(this)
                isRegistered = false
                Timber.d("MusicTriggerProvider: Unregistered listeners")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister MusicTriggerProvider listeners")
            }
        }
    }

    private val musicStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            val isPlaying = intent.getBooleanExtra("playing", false)
            val stateChanged = isMusicPlaying != isPlaying
            
            if (stateChanged) {
                isMusicPlaying = isPlaying
                val event = if (isPlaying) "MUSIC_STARTED" else "MUSIC_STOPPED"
                Timber.d("Music state changed: $event")
                notifyTriggers(event)
            }
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Timber.d("Audio focus gained - music playing")
                if (!isMusicPlaying) {
                    isMusicPlaying = true
                    notifyTriggers("MUSIC_STARTED")
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Timber.d("Audio focus lost - music stopped")
                if (isMusicPlaying) {
                    isMusicPlaying = false
                    notifyTriggers("MUSIC_STOPPED")
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Timber.d("Audio focus lost transient")
                if (isMusicPlaying) {
                    isMusicPlaying = false
                    notifyTriggers("MUSIC_STOPPED")
                }
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Timber.d("Audio focus lost transient (can duck)")
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

    private fun notifyTrigger(trigger: TriggerDTO) {
        scope.launch {
            try {
                checkTriggersUseCase(type, mapOf("fired_trigger_id" to trigger.id))
            } catch (e: Exception) {
                Timber.e(e, "Failed to check trigger ${trigger.id}")
            }
        }
    }
}
