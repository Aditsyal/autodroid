package com.aditsyal.autodroid.automation.trigger.providers

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.receivers.TriggerAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimeTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : TriggerProvider {

    override val type: String = "TIME"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        val timeStr = trigger.triggerConfig["time"] as? String ?: return
        val days = trigger.triggerConfig["days"] as? List<*> ?: emptyList<String>()
        
        val parts = timeStr.split(":")
        if (parts.size != 2) return
        
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // If time has already passed today, schedule for tomorrow
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        scheduleAlarm(trigger.id, calendar.timeInMillis)
    }

    private fun scheduleAlarm(triggerId: Long, triggerTime: Long) {
        val intent = Intent(context, TriggerAlarmReceiver::class.java).apply {
            putExtra("trigger_id", triggerId)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            triggerId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Timber.d("Scheduling alarm for trigger $triggerId at $triggerTime")
        
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to schedule exact alarm for trigger $triggerId")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        val intent = Intent(context, TriggerAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            triggerId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    override suspend fun clearTriggers() {
        // This is hard with AlarmManager without keeping a list of IDs.
        // We'll rely on the persistence layer to call unregisterTrigger for each.
    }
}
