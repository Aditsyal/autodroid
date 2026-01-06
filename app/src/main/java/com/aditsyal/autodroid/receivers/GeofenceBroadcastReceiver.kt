package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
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
        val ctx = context ?: return
        val i = intent ?: return
        val event = GeofencingEvent.fromIntent(i) ?: return

        if (event.hasError()) {
            Timber.e("Geofencing error: ${event.errorCode}")
            return
        }

        // Check location permissions
        val hasFineLocation = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarseLocation = ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            Timber.w("Geofence triggered but location permissions not granted")
            return
        }

        // Check if GPS is enabled
        val locationManager = ctx.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val isGpsEnabled = locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true

        if (!isGpsEnabled) {
            Timber.w("Geofence triggered but GPS is disabled")
            // Continue anyway, as network location might be available
        }

        val transitionType = event.geofenceTransition
        val transitionName = when (transitionType) {
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_ENTER -> "ENTER"
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_EXIT -> "EXIT"
            com.google.android.gms.location.Geofence.GEOFENCE_TRANSITION_DWELL -> "DWELL"
            else -> "UNKNOWN"
        }
        val triggers = event.triggeringGeofences ?: return

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            triggers.forEach { geofence ->
                val triggerId = geofence.requestId.toLongOrNull() ?: return@forEach
                Timber.d("Geofence transition $transitionName detected for trigger $triggerId")
                
                checkTriggersUseCase(
                    "LOCATION", 
                    mapOf(
                        "fired_trigger_id" to triggerId,
                        "transitionType" to transitionName
                    )
                )
            }
        }
    }
}
