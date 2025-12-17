package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import com.google.android.gms.location.GeofencingEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        val event = GeofencingEvent.fromIntent(intent ?: return) ?: return
        
        if (event.hasError()) {
            Timber.e("Geofencing error: ${event.errorCode}")
            return
        }

        val transitionType = event.geofenceTransition
        val triggers = event.triggeringGeofences ?: return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            triggers.forEach { geofence ->
                val triggerId = geofence.requestId.toLongOrNull() ?: return@forEach
                Timber.d("Geofence transition $transitionType detected for trigger $triggerId")
                
                checkTriggersUseCase(
                    "LOCATION", 
                    mapOf(
                        "fired_trigger_id" to triggerId,
                        "transition" to transitionType
                    )
                )
            }
        }
    }
}
