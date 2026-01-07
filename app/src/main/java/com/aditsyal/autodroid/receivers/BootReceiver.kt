package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditsyal.autodroid.workers.MacroTriggerWorker
import timber.log.Timber

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed - scheduling macro workers")
            context?.let { ctx ->
                // Use optimized scheduler from the worker
                MacroTriggerWorker.schedulePeriodicCheck(ctx)
                Timber.d("BootReceiver: Scheduled MacroTriggerWorker with optimized configuration")
            }
        }
    }
}
