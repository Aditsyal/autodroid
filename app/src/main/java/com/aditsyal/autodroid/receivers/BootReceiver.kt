package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aditsyal.autodroid.workers.MacroTriggerWorker
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed - scheduling macro workers")
            context?.let { ctx ->
                // Start the foreground service for real-time monitoring
                val serviceIntent = Intent(ctx, com.aditsyal.autodroid.services.AutomationForegroundService::class.java)
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    ctx.startForegroundService(serviceIntent)
                } else {
                    ctx.startService(serviceIntent)
                }

                val workRequest = PeriodicWorkRequestBuilder<MacroTriggerWorker>(15, TimeUnit.MINUTES)
                    .build()

                WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                    "MacroTriggerWork",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )
                
                Timber.d("BootReceiver: Started service and scheduled MacroTriggerWorker")
            }
        }
    }
}

