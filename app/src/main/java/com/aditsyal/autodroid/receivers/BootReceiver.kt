package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.aditsyal.autodroid.AutodroidApplication
import com.aditsyal.autodroid.domain.usecase.InitializeTriggersUseCase
import com.aditsyal.autodroid.workers.MacroTriggerWorker
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            Timber.d("Boot completed - scheduling macro workers and initializing triggers")
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

                // Initialize triggers after boot using Hilt EntryPoint
                val appContext = ctx.applicationContext as? AutodroidApplication
                if (appContext != null) {
                    val entryPoint = EntryPointAccessors.fromApplication(
                        appContext,
                        BootReceiverEntryPoint::class.java
                    )
                    val initializeTriggersUseCase = entryPoint.initializeTriggersUseCase()

                    val scope = CoroutineScope(Dispatchers.IO)
                    scope.launch {
                        try {
                            Timber.d("Initializing triggers after boot")
                            initializeTriggersUseCase()
                        } catch (e: Exception) {
                            Timber.e(e, "Failed to initialize triggers after boot")
                        }
                    }
                } else {
                    Timber.w("Could not get AutodroidApplication context for trigger initialization")
                }
            }
        }
    }

    @dagger.hilt.EntryPoint
    @dagger.hilt.InstallIn(dagger.hilt.components.SingletonComponent::class)
    interface BootReceiverEntryPoint {
        fun initializeTriggersUseCase(): InitializeTriggersUseCase
    }
}

