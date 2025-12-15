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

class AutomationForegroundService : Service() {

    companion object {
        private const val CHANNEL_ID = "automation_channel"
        private const val NOTIFICATION_ID = 1
    }

    override fun onCreate() {
        super.onCreate()
        Timber.d("AutomationForegroundService: Created")
        createNotificationChannel()
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

        startForeground(NOTIFICATION_ID, notification)

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

