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
                // Schedule periodic WorkManager job for trigger checking
                val workRequest = PeriodicWorkRequestBuilder<MacroTriggerWorker>(15, TimeUnit.MINUTES)
                    .build()

                WorkManager.getInstance(ctx).enqueueUniquePeriodicWork(
                    "MacroTriggerWork",
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest
                )

                Timber.d("BootReceiver: Scheduled MacroTriggerWorker")
            }
        }
    }
}

