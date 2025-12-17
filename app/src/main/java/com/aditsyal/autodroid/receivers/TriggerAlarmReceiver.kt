package com.aditsyal.autodroid.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class TriggerAlarmReceiver : BroadcastReceiver() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    override fun onReceive(context: Context?, intent: Intent?) {
        val triggerId = intent?.getLongExtra("trigger_id", -1L) ?: -1L
        if (triggerId == -1L) return

        Timber.d("Internal alarm fired for trigger: $triggerId")

        // We use CheckTriggersUseCase with a specific event to only match this trigger ID
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                // We pass the triggerId in eventData. 
                // We'll update CheckTriggersUseCase to prioritize this if present.
                checkTriggersUseCase("TIME", mapOf("fired_trigger_id" to triggerId))
            } catch (e: Exception) {
                Timber.e(e, "Error handling alarm trigger")
            }
        }
    }
}
