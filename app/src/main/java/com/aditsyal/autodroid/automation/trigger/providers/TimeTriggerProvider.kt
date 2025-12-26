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
        try {
            val triggerSubType = trigger.triggerConfig["subType"]?.toString() ?: "SPECIFIC_TIME"
            
            when (triggerSubType) {
                "SPECIFIC_TIME" -> registerSpecificTimeTrigger(trigger)
                "TIME_INTERVAL" -> registerIntervalTrigger(trigger)
                "DAY_OF_WEEK" -> registerDayOfWeekTrigger(trigger)
                "DATE_RANGE" -> registerDateRangeTrigger(trigger)
                else -> {
                    Timber.w("Unknown time trigger subType: $triggerSubType, defaulting to SPECIFIC_TIME")
                    registerSpecificTimeTrigger(trigger)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to register time trigger ${trigger.id}")
        }
    }

    private fun registerSpecificTimeTrigger(trigger: TriggerDTO) {
        val timeStr = trigger.triggerConfig["time"] as? String
        if (timeStr == null) {
            Timber.w("Time trigger ${trigger.id} missing time configuration")
            return
        }
        
        val days = trigger.triggerConfig["days"] as? List<*> ?: emptyList<String>()
        
        val parts = timeStr.split(":")
        if (parts.size != 2) {
            Timber.w("Invalid time format for trigger ${trigger.id}: $timeStr (expected HH:MM)")
            return
        }
        
        val hour = parts[0].toIntOrNull()
        val minute = parts[1].toIntOrNull()
        
        if (hour == null || minute == null || hour !in 0..23 || minute !in 0..59) {
            Timber.w("Invalid hour/minute for trigger ${trigger.id}: hour=$hour, minute=$minute (must be 0-23 and 0-59)")
            return
        }

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

    private fun registerIntervalTrigger(trigger: TriggerDTO) {
        val intervalMinutes = trigger.triggerConfig["intervalMinutes"]?.toString()?.toLongOrNull()
        val intervalHours = trigger.triggerConfig["intervalHours"]?.toString()?.toLongOrNull()
        
        val intervalMs = when {
            intervalMinutes != null -> intervalMinutes * 60 * 1000
            intervalHours != null -> intervalHours * 60 * 60 * 1000
            else -> {
                Timber.w("Time interval trigger ${trigger.id} missing interval configuration")
                return
            }
        }
        
        if (intervalMs < 60000) { // Minimum 1 minute
            Timber.w("Time interval trigger ${trigger.id} has interval less than 1 minute")
            return
        }

        // Schedule first execution
        val firstExecution = System.currentTimeMillis() + intervalMs
        scheduleAlarm(trigger.id, firstExecution)
        
        // Note: For recurring intervals, we'll need to reschedule in TriggerAlarmReceiver
        Timber.d("Scheduled interval trigger ${trigger.id} with interval ${intervalMs}ms")
    }

    private fun registerDayOfWeekTrigger(trigger: TriggerDTO) {
        val timeStr = trigger.triggerConfig["time"] as? String
        val daysOfWeek = trigger.triggerConfig["daysOfWeek"] as? List<*> ?: emptyList<String>()
        
        if (timeStr == null || daysOfWeek.isEmpty()) {
            Timber.w("Day of week trigger ${trigger.id} missing time or days configuration")
            return
        }
        
        val parts = timeStr.split(":")
        if (parts.size != 2) {
            Timber.w("Invalid time format for trigger ${trigger.id}: $timeStr")
            return
        }
        
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        
        val dayNumbers = daysOfWeek.mapNotNull { day ->
            when (day.toString().uppercase()) {
                "MONDAY", "MON" -> Calendar.MONDAY
                "TUESDAY", "TUE" -> Calendar.TUESDAY
                "WEDNESDAY", "WED" -> Calendar.WEDNESDAY
                "THURSDAY", "THU" -> Calendar.THURSDAY
                "FRIDAY", "FRI" -> Calendar.FRIDAY
                "SATURDAY", "SAT" -> Calendar.SATURDAY
                "SUNDAY", "SUN" -> Calendar.SUNDAY
                else -> null
            }
        }
        
        if (dayNumbers.isEmpty()) {
            Timber.w("Day of week trigger ${trigger.id} has no valid days")
            return
        }
        
        // Find next occurrence
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Find next matching day
        var found = false
        for (i in 0..7) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayNumbers.contains(dayOfWeek) && calendar.timeInMillis > System.currentTimeMillis()) {
                found = true
                break
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        if (found) {
            scheduleAlarm(trigger.id, calendar.timeInMillis)
        } else {
            Timber.w("Could not find next occurrence for day of week trigger ${trigger.id}")
        }
    }

    private fun registerDateRangeTrigger(trigger: TriggerDTO) {
        val startDate = trigger.triggerConfig["startDate"]?.toString()?.toLongOrNull()
        val endDate = trigger.triggerConfig["endDate"]?.toString()?.toLongOrNull()
        val timeStr = trigger.triggerConfig["time"] as? String
        
        if (startDate == null || endDate == null || timeStr == null) {
            Timber.w("Date range trigger ${trigger.id} missing configuration")
            return
        }
        
        val now = System.currentTimeMillis()
        if (now < startDate || now > endDate) {
            Timber.d("Date range trigger ${trigger.id} is outside date range")
            return
        }
        
        val parts = timeStr.split(":")
        if (parts.size != 2) return
        
        val hour = parts[0].toIntOrNull() ?: return
        val minute = parts[1].toIntOrNull() ?: return
        
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            if (timeInMillis <= now) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        
        if (calendar.timeInMillis <= endDate) {
            scheduleAlarm(trigger.id, calendar.timeInMillis)
        }
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
        try {
            val intent = Intent(context, TriggerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                triggerId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Timber.d("Cancelled alarm for trigger $triggerId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister time trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        // This is hard with AlarmManager without keeping a list of IDs.
        // We'll rely on the persistence layer to call unregisterTrigger for each.
    }
}
