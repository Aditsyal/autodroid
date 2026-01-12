package com.aditsyal.autodroid.domain.usecase.executors

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class DndExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val action = config["action"]?.toString()?.lowercase() ?: "toggle"
            val interruptionFilter = config["interruptionFilter"]?.toString()?.toIntOrNull()

            when (action) {
                "enable", "on" -> enableDnd(interruptionFilter)
                "disable", "off" -> disableDnd()
                "toggle" -> toggleDnd(interruptionFilter)
                else -> throw IllegalArgumentException("Invalid action: $action. Use 'enable', 'disable', or 'toggle'")
            }

            Timber.i("DND action executed: $action")
        }.onFailure { e ->
            Timber.e(e, "DND execution failed")
        }
    }

    private fun enableDnd(filter: Int?) {
        val filterToUse = filter ?: NotificationManager.INTERRUPTION_FILTER_NONE

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(filterToUse)
                Timber.d("DND enabled with filter: $filterToUse")
            } else {
                throw SecurityException("Notification policy access not granted. User must enable DND access in settings.")
            }
        } else {
            Timber.w("DND not supported on Android versions below 6.0")
            throw UnsupportedOperationException("Do Not Disturb requires Android 6.0 or higher")
        }
    }

    private fun disableDnd() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                Timber.d("DND disabled")
            } else {
                throw SecurityException("Notification policy access not granted")
            }
        } else {
            throw UnsupportedOperationException("Do Not Disturb requires Android 6.0 or higher")
        }
    }

    private fun toggleDnd(filter: Int?) {
        val currentFilter = getCurrentInterruptionFilter()

        if (currentFilter == NotificationManager.INTERRUPTION_FILTER_ALL) {
            // Currently allowing all, enable DND
            enableDnd(filter)
        } else {
            // Currently in DND, disable it
            disableDnd()
        }
    }

    private fun getCurrentInterruptionFilter(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                notificationManager.currentInterruptionFilter
            } catch (e: Exception) {
                Timber.w(e, "Failed to get current interruption filter")
                NotificationManager.INTERRUPTION_FILTER_ALL
            }
        } else {
            NotificationManager.INTERRUPTION_FILTER_ALL
        }
    }

    // Utility methods
    fun isDndEnabled(): Boolean {
        return getCurrentInterruptionFilter() != NotificationManager.INTERRUPTION_FILTER_ALL
    }

    fun hasNotificationPolicyAccess(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager.isNotificationPolicyAccessGranted
        } else {
            true // Not required for older versions
        }
    }

    fun getCurrentFilterName(): String {
        return when (getCurrentInterruptionFilter()) {
            NotificationManager.INTERRUPTION_FILTER_ALL -> "ALL"
            NotificationManager.INTERRUPTION_FILTER_NONE -> "NONE"
            NotificationManager.INTERRUPTION_FILTER_PRIORITY -> "PRIORITY"
            NotificationManager.INTERRUPTION_FILTER_ALARMS -> "ALARMS"
            NotificationManager.INTERRUPTION_FILTER_UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }
}