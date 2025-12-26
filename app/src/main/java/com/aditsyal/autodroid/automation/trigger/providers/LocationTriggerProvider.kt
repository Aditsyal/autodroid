package com.aditsyal.autodroid.automation.trigger.providers

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.receivers.GeofenceBroadcastReceiver
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : TriggerProvider {

    override val type: String = "LOCATION"
    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("MissingPermission")
    override suspend fun registerTrigger(trigger: TriggerDTO) {
        val lat = trigger.triggerConfig["latitude"]?.toString()?.toDoubleOrNull() ?: return
        val lng = trigger.triggerConfig["longitude"]?.toString()?.toDoubleOrNull() ?: return
        val radius = trigger.triggerConfig["radius"]?.toString()?.toFloatOrNull() ?: 100f
        
        val transitionType = when (trigger.triggerConfig["transitionType"]?.toString()) {
            "EXIT" -> Geofence.GEOFENCE_TRANSITION_EXIT
            "DWELL" -> Geofence.GEOFENCE_TRANSITION_DWELL
            else -> Geofence.GEOFENCE_TRANSITION_ENTER
        }

        val geofence = Geofence.Builder()
            .setRequestId(trigger.id.toString())
            .setCircularRegion(lat, lng, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(transitionType)
            .setLoiteringDelay(300000) // 5 minutes if DWELL
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(request, geofencePendingIntent).run {
            addOnSuccessListener {
                Timber.d("Geofence registered successfully for trigger ${trigger.id}")
            }
            addOnFailureListener {
                Timber.e(it, "Failed to register geofence for trigger ${trigger.id}")
            }
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            geofencingClient.removeGeofences(listOf(triggerId.toString())).run {
                addOnSuccessListener {
                    Timber.d("Geofence removed successfully for trigger $triggerId")
                }
                addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to remove geofence for trigger $triggerId")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister location trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            geofencingClient.removeGeofences(geofencePendingIntent).run {
                addOnSuccessListener {
                    Timber.d("All geofences cleared")
                }
                addOnFailureListener { exception ->
                    Timber.e(exception, "Failed to clear geofences")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear location triggers")
        }
    }
}
