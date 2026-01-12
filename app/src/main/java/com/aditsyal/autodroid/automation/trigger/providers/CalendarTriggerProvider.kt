package com.aditsyal.autodroid.automation.trigger.providers

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.receivers.TriggerAlarmReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CalendarTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context
) : TriggerProvider {

    override val type: String = "CALENDAR_EVENT"
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val contentResolver = context.contentResolver
    private val handler = Handler(Looper.getMainLooper())
    private val registeredObservers = mutableMapOf<Long, ContentObserver>()

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            // Check for READ_CALENDAR permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
                Timber.w("READ_CALENDAR permission not granted for calendar trigger ${trigger.id}")
                return
            }

            val triggerSubType = trigger.triggerConfig["subType"]?.toString() ?: "UPCOMING_EVENT"

            when (triggerSubType) {
                "UPCOMING_EVENT" -> registerUpcomingEventTrigger(trigger)
                "SPECIFIC_CALENDAR" -> registerSpecificCalendarTrigger(trigger)
                "EVENT_TITLE" -> registerEventTitleTrigger(trigger)
                else -> {
                    Timber.w("Unknown calendar trigger subType: $triggerSubType, defaulting to UPCOMING_EVENT")
                    registerUpcomingEventTrigger(trigger)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to register calendar trigger ${trigger.id}")
        }
    }

    private fun registerUpcomingEventTrigger(trigger: TriggerDTO) {
        val minutesBefore = trigger.triggerConfig["minutesBefore"]?.toString()?.toIntOrNull() ?: 15
        val maxEvents = trigger.triggerConfig["maxEvents"]?.toString()?.toIntOrNull() ?: 5

        val upcomingEvents = getUpcomingEvents(minutesBefore, maxEvents)
        if (upcomingEvents.isEmpty()) {
            Timber.d("No upcoming events found for trigger ${trigger.id}")
            return
        }

        upcomingEvents.forEach { event ->
            scheduleAlarm(trigger.id, event.alarmTime)
            Timber.d("Scheduled alarm for event '${event.title}' at ${Date(event.alarmTime)}")
        }

        // Register content observer for calendar changes
        registerCalendarObserver(trigger.id)
    }

    private fun registerSpecificCalendarTrigger(trigger: TriggerDTO) {
        val calendarId = trigger.triggerConfig["calendarId"]?.toString()?.toLongOrNull()
        val minutesBefore = trigger.triggerConfig["minutesBefore"]?.toString()?.toIntOrNull() ?: 15

        if (calendarId == null) {
            Timber.w("Calendar trigger ${trigger.id} missing calendarId configuration")
            return
        }

        val upcomingEvents = getUpcomingEventsFromCalendar(calendarId, minutesBefore, 10)
        upcomingEvents.forEach { event ->
            scheduleAlarm(trigger.id, event.alarmTime)
        }

        registerCalendarObserver(trigger.id)
    }

    private fun registerEventTitleTrigger(trigger: TriggerDTO) {
        val eventTitle = trigger.triggerConfig["eventTitle"]?.toString()
        val minutesBefore = trigger.triggerConfig["minutesBefore"]?.toString()?.toIntOrNull() ?: 15

        if (eventTitle.isNullOrBlank()) {
            Timber.w("Calendar trigger ${trigger.id} missing eventTitle configuration")
            return
        }

        val upcomingEvents = getEventsByTitle(eventTitle, minutesBefore, 10)
        upcomingEvents.forEach { event ->
            scheduleAlarm(trigger.id, event.alarmTime)
        }

        registerCalendarObserver(trigger.id)
    }

    private data class CalendarEvent(
        val id: Long,
        val title: String,
        val startTime: Long,
        val alarmTime: Long
    )

    private fun getUpcomingEvents(minutesBefore: Int, maxEvents: Int): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        val futureTime = now + TimeUnit.DAYS.toMillis(7) // Look 7 days ahead

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )

        val selection = "${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(now.toString(), futureTime.toString())

        return try {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )?.use { cursor ->
                val events = mutableListOf<CalendarEvent>()
                while (cursor.moveToNext() && events.size < maxEvents) {
                    val eventId = cursor.getLong(0)
                    val title = cursor.getString(1) ?: "Untitled Event"
                    val startTime = cursor.getLong(2)

                    val alarmTime = startTime - TimeUnit.MINUTES.toMillis(minutesBefore.toLong())
                    if (alarmTime > now) {
                        events.add(CalendarEvent(eventId, title, startTime, alarmTime))
                    }
                }
                events
            } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to query calendar events")
            emptyList()
        }
    }

    private fun getUpcomingEventsFromCalendar(calendarId: Long, minutesBefore: Int, maxEvents: Int): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        val futureTime = now + TimeUnit.DAYS.toMillis(7)

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )

        val selection = "${CalendarContract.Events.CALENDAR_ID} = ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf(calendarId.toString(), now.toString(), futureTime.toString())

        return try {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )?.use { cursor ->
                val events = mutableListOf<CalendarEvent>()
                while (cursor.moveToNext() && events.size < maxEvents) {
                    val eventId = cursor.getLong(0)
                    val title = cursor.getString(1) ?: "Untitled Event"
                    val startTime = cursor.getLong(2)

                    val alarmTime = startTime - TimeUnit.MINUTES.toMillis(minutesBefore.toLong())
                    if (alarmTime > now) {
                        events.add(CalendarEvent(eventId, title, startTime, alarmTime))
                    }
                }
                events
            } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to query calendar events for calendar $calendarId")
            emptyList()
        }
    }

    private fun getEventsByTitle(eventTitle: String, minutesBefore: Int, maxEvents: Int): List<CalendarEvent> {
        val now = System.currentTimeMillis()
        val futureTime = now + TimeUnit.DAYS.toMillis(7)

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DTSTART
        )

        val selection = "${CalendarContract.Events.TITLE} LIKE ? AND ${CalendarContract.Events.DTSTART} >= ? AND ${CalendarContract.Events.DTSTART} <= ?"
        val selectionArgs = arrayOf("%$eventTitle%", now.toString(), futureTime.toString())

        return try {
            contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                "${CalendarContract.Events.DTSTART} ASC"
            )?.use { cursor ->
                val events = mutableListOf<CalendarEvent>()
                while (cursor.moveToNext() && events.size < maxEvents) {
                    val eventId = cursor.getLong(0)
                    val title = cursor.getString(1) ?: "Untitled Event"
                    val startTime = cursor.getLong(2)

                    val alarmTime = startTime - TimeUnit.MINUTES.toMillis(minutesBefore.toLong())
                    if (alarmTime > now) {
                        events.add(CalendarEvent(eventId, title, startTime, alarmTime))
                    }
                }
                events
            } ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to query calendar events by title '$eventTitle'")
            emptyList()
        }
    }

    private fun registerCalendarObserver(triggerId: Long) {
        val observer = object : ContentObserver(handler) {
            override fun onChange(selfChange: Boolean) {
                onChange(selfChange, null)
            }

            override fun onChange(selfChange: Boolean, uri: Uri?) {
                Timber.d("Calendar content changed for trigger $triggerId")
                // Note: In a real implementation, you might want to reschedule alarms
                // when calendar events change. For now, we'll just log the change.
            }
        }

        contentResolver.registerContentObserver(
            CalendarContract.Events.CONTENT_URI,
            true,
            observer
        )

        registeredObservers[triggerId] = observer
    }

    private fun scheduleAlarm(triggerId: Long, triggerTime: Long) {
        val intent = Intent(context, TriggerAlarmReceiver::class.java).apply {
            putExtra("trigger_id", triggerId)
            putExtra("calendar_event", true)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            triggerId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        Timber.d("Scheduling calendar alarm for trigger $triggerId at $triggerTime")

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
            Timber.e(e, "Failed to schedule exact alarm for calendar trigger $triggerId")
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            // Cancel any pending alarms
            val intent = Intent(context, TriggerAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                triggerId.toInt(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                Timber.d("Cancelled calendar alarm for trigger $triggerId")
            }

            // Unregister content observer
            registeredObservers[triggerId]?.let { observer ->
                contentResolver.unregisterContentObserver(observer)
                registeredObservers.remove(triggerId)
                Timber.d("Unregistered calendar observer for trigger $triggerId")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister calendar trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        // Unregister all observers
        registeredObservers.forEach { (triggerId, observer) ->
            try {
                contentResolver.unregisterContentObserver(observer)
                Timber.d("Unregistered calendar observer for trigger $triggerId")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister observer for trigger $triggerId")
            }
        }
        registeredObservers.clear()
    }
}