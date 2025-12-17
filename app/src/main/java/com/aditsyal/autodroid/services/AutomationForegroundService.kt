package com.aditsyal.autodroid.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aditsyal.autodroid.R
import com.aditsyal.autodroid.workers.MacroTriggerWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.os.BatteryManager
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AutomationForegroundService : Service() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private val dynamicReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.action?.let { action ->
                serviceScope.launch {
                    try {
                        when (action) {
                            Intent.ACTION_BATTERY_CHANGED -> {
                                val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                                if (level >= 0 && scale > 0) {
                                    val batteryPct = (level / scale.toFloat() * 100).toInt()
                                    Timber.d("Service: Battery level changed: $batteryPct%")
                                    checkTriggersUseCase(
                                        "SYSTEM_EVENT",
                                        mapOf("event" to "BATTERY_CHANGED", "level" to batteryPct)
                                    )
                                }
                            }
                            Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                                val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                                Timber.d("Service: Airplane mode changed: $isAirplaneModeOn")
                                checkTriggersUseCase(
                                    "SYSTEM_EVENT",
                                    mapOf("event" to "AIRPLANE_MODE", "state" to isAirplaneModeOn)
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Error handling dynamic broadcast")
                    }
                }
            }
        }
    }

    companion object {
        private const val CHANNEL_ID = "automation_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("AutomationForegroundService: Created")
        createNotificationChannel()
        
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_BATTERY_CHANGED)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
        }
        registerReceiver(dynamicReceiver, filter)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("AutomationForegroundService: Started")

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Autodroid")
            .setContentText("Monitoring automations...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE or
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        // Ensure background worker is scheduled
        val workRequest = PeriodicWorkRequestBuilder<MacroTriggerWorker>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "MacroTriggerWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(dynamicReceiver)
        Timber.d("AutomationForegroundService: Destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Automation Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

