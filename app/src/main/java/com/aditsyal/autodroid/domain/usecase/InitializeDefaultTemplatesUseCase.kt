package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.google.gson.Gson
import timber.log.Timber
import javax.inject.Inject

/**
 * Initialize default templates in the database
 */
class InitializeDefaultTemplatesUseCase @Inject constructor(
    private val templateDao: TemplateDao
) {
    private val gson = Gson()

    suspend operator fun invoke() {
        try {
            val existingTemplates = templateDao.getAllTemplates()
            // Check if templates already exist (using a one-time check)
            var hasTemplates = false
            existingTemplates.collect { templates ->
                hasTemplates = templates.isNotEmpty()
            }

            if (hasTemplates) {
                Timber.d("Default templates already initialized")
                return
            }

            val templates = createDefaultTemplates()
            templates.forEach { template ->
                templateDao.insertTemplate(template)
            }
            Timber.i("Initialized ${templates.size} default templates")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize default templates")
        }
    }

    private fun createDefaultTemplates(): List<TemplateEntity> {
        return listOf(
            // Morning Routine
            createTemplate(
                name = "Morning Routine",
                description = "Automate your morning: WiFi on, volume up, brightness high",
                category = "ROUTINE",
                macro = MacroDTO(
                    name = "Morning Routine",
                    description = "Start your day right",
                    triggers = listOf(
                        TriggerDTO(triggerType = "TIME", triggerConfig = mapOf("time" to "07:00", "subType" to "SPECIFIC_TIME"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "WIFI_TOGGLE", actionConfig = mapOf("enabled" to true), executionOrder = 0),
                        ActionDTO(actionType = "SET_BRIGHTNESS", actionConfig = mapOf("brightness" to 80), executionOrder = 1),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "MUSIC", "level" to 70), executionOrder = 2),
                        ActionDTO(actionType = "SHOW_NOTIFICATION", actionConfig = mapOf("title" to "Good Morning!", "message" to "Morning routine activated"), executionOrder = 3)
                    ),
                    constraints = emptyList()
                )
            ),
            // Work Mode
            createTemplate(
                name = "Work Mode",
                description = "Enter work mode: Silence phone, enable WiFi, set DND",
                category = "WORK",
                macro = MacroDTO(
                    name = "Work Mode",
                    description = "Focus mode for work",
                    triggers = listOf(
                        TriggerDTO(triggerType = "TIME", triggerConfig = mapOf("time" to "09:00", "subType" to "SPECIFIC_TIME"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "ENABLE_DO_NOT_DISTURB", actionConfig = emptyMap(), executionOrder = 0),
                        ActionDTO(actionType = "WIFI_TOGGLE", actionConfig = mapOf("enabled" to true), executionOrder = 1),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "RING", "level" to 20), executionOrder = 2)
                    ),
                    constraints = listOf(
                        ConstraintDTO(constraintType = "DAY_OF_WEEK", constraintConfig = mapOf("days" to listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")))
                    )
                )
            ),
            // Sleep Mode
            createTemplate(
                name = "Sleep Mode",
                description = "Prepare for sleep: DND on, brightness low, WiFi off",
                category = "SLEEP",
                macro = MacroDTO(
                    name = "Sleep Mode",
                    description = "Wind down for the night",
                    triggers = listOf(
                        TriggerDTO(triggerType = "TIME", triggerConfig = mapOf("time" to "22:00", "subType" to "SPECIFIC_TIME"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "ENABLE_DO_NOT_DISTURB", actionConfig = emptyMap(), executionOrder = 0),
                        ActionDTO(actionType = "SET_BRIGHTNESS", actionConfig = mapOf("brightness" to 10), executionOrder = 1),
                        ActionDTO(actionType = "WIFI_TOGGLE", actionConfig = mapOf("enabled" to false), executionOrder = 2),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "RING", "level" to 0), executionOrder = 3)
                    ),
                    constraints = emptyList()
                )
            ),
            // Battery Saver
            createTemplate(
                name = "Battery Saver",
                description = "Save battery: Reduce brightness, disable WiFi/Bluetooth when battery is low",
                category = "BATTERY",
                macro = MacroDTO(
                    name = "Battery Saver",
                    description = "Extend battery life",
                    triggers = listOf(
                        TriggerDTO(triggerType = "DEVICE_STATE", triggerConfig = mapOf("event" to "BATTERY_LEVEL", "threshold" to 20, "operator" to "below"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "SET_BRIGHTNESS", actionConfig = mapOf("brightness" to 30), executionOrder = 0),
                        ActionDTO(actionType = "WIFI_TOGGLE", actionConfig = mapOf("enabled" to false), executionOrder = 1),
                        ActionDTO(actionType = "BLUETOOTH_TOGGLE", actionConfig = mapOf("enabled" to false), executionOrder = 2),
                        ActionDTO(actionType = "SHOW_NOTIFICATION", actionConfig = mapOf("title" to "Battery Saver", "message" to "Battery optimization activated"), executionOrder = 3)
                    ),
                    constraints = emptyList()
                )
            ),
            // Location-Based: Home
            createTemplate(
                name = "Arrive Home",
                description = "When you arrive home: WiFi on, volume up",
                category = "LOCATION",
                macro = MacroDTO(
                    name = "Arrive Home",
                    description = "Home automation",
                    triggers = listOf(
                        TriggerDTO(triggerType = "LOCATION", triggerConfig = mapOf("latitude" to 0.0, "longitude" to 0.0, "radius" to 100.0, "transitionType" to "ENTER"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "WIFI_TOGGLE", actionConfig = mapOf("enabled" to true), executionOrder = 0),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "MUSIC", "level" to 50), executionOrder = 1)
                    ),
                    constraints = emptyList()
                )
            ),
            // Communication: Auto Reply
            createTemplate(
                name = "Auto Reply to SMS",
                description = "Automatically reply to SMS messages when driving",
                category = "COMMUNICATION",
                macro = MacroDTO(
                    name = "Auto Reply to SMS",
                    description = "Reply to messages automatically",
                    triggers = listOf(
                        TriggerDTO(triggerType = "COMMUNICATION", triggerConfig = mapOf("event" to "SMS_RECEIVED"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "SEND_SMS", actionConfig = mapOf("phoneNumber" to "{sender}", "message" to "I'm driving, will reply later"), executionOrder = 0)
                    ),
                    constraints = listOf(
                        ConstraintDTO(constraintType = "TIME_RANGE", constraintConfig = mapOf("startTime" to "08:00", "endTime" to "20:00"))
                    )
                )
            ),
            // Charging: Full Charge Alert
            createTemplate(
                name = "Battery Full Alert",
                description = "Notify when battery reaches 100%",
                category = "BATTERY",
                macro = MacroDTO(
                    name = "Battery Full Alert",
                    description = "Alert when fully charged",
                    triggers = listOf(
                        TriggerDTO(triggerType = "DEVICE_STATE", triggerConfig = mapOf("event" to "BATTERY_LEVEL", "threshold" to 100, "operator" to "equals"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "SHOW_NOTIFICATION", actionConfig = mapOf("title" to "Battery Full", "message" to "Your device is fully charged"), executionOrder = 0),
                        ActionDTO(actionType = "VIBRATE", actionConfig = mapOf("duration" to 500), executionOrder = 1)
                    ),
                    constraints = listOf(
                        ConstraintDTO(constraintType = "CHARGING_STATUS", constraintConfig = mapOf("isCharging" to true))
                    )
                )
            ),
            // App: Focus Mode
            createTemplate(
                name = "Focus Mode",
                description = "Enable focus mode when specific app opens",
                category = "APP",
                macro = MacroDTO(
                    name = "Focus Mode",
                    description = "Distraction-free mode",
                    triggers = listOf(
                        TriggerDTO(triggerType = "APP_EVENT", triggerConfig = mapOf("event" to "APP_LAUNCHED", "packageName" to "com.example.app"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "ENABLE_DO_NOT_DISTURB", actionConfig = emptyMap(), executionOrder = 0),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "NOTIFICATION", "level" to 0), executionOrder = 1)
                    ),
                    constraints = emptyList()
                )
            ),
            // Time: Lunch Break
            createTemplate(
                name = "Lunch Break",
                description = "Lunch break routine: Volume up, notifications on",
                category = "ROUTINE",
                macro = MacroDTO(
                    name = "Lunch Break",
                    description = "Lunch time automation",
                    triggers = listOf(
                        TriggerDTO(triggerType = "TIME", triggerConfig = mapOf("time" to "12:30", "subType" to "SPECIFIC_TIME"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "DISABLE_DO_NOT_DISTURB", actionConfig = emptyMap(), executionOrder = 0),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "RING", "level" to 70), executionOrder = 1),
                        ActionDTO(actionType = "SHOW_NOTIFICATION", actionConfig = mapOf("title" to "Lunch Break", "message" to "Time for lunch!"), executionOrder = 2)
                    ),
                    constraints = listOf(
                        ConstraintDTO(constraintType = "DAY_OF_WEEK", constraintConfig = mapOf("days" to listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")))
                    )
                )
            ),
            // Connectivity: WiFi Auto-Connect
            createTemplate(
                name = "Auto Connect to Home WiFi",
                description = "Automatically adjust settings when connecting to home WiFi",
                category = "CONNECTIVITY",
                macro = MacroDTO(
                    name = "Auto Connect to Home WiFi",
                    description = "Home network automation",
                    triggers = listOf(
                        TriggerDTO(triggerType = "CONNECTIVITY", triggerConfig = mapOf("event" to "WIFI_CONNECTED", "ssid" to "HomeNetwork"))
                    ),
                    actions = listOf(
                        ActionDTO(actionType = "SET_BRIGHTNESS", actionConfig = mapOf("brightness" to 70), executionOrder = 0),
                        ActionDTO(actionType = "VOLUME_CONTROL", actionConfig = mapOf("stream" to "MUSIC", "level" to 60), executionOrder = 1)
                    ),
                    constraints = emptyList()
                )
            )
        )
    }

    private fun createTemplate(
        name: String,
        description: String,
        category: String,
        macro: MacroDTO
    ): TemplateEntity {
        return TemplateEntity(
            name = name,
            description = description,
            category = category,
            macroJson = gson.toJson(macro)
        )
    }
}

