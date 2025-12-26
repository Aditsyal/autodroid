package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aditsyal.autodroid.data.models.TriggerDTO

data class TriggerOption(
    val label: String,
    val type: String,
    val config: Map<String, Any> = emptyMap()
)

val triggerOptions = listOf(
    // Time-based triggers
    TriggerOption("Specific Time", "TIME", mapOf("subType" to "SPECIFIC_TIME", "time" to "12:00", "days" to listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))),
    TriggerOption("Time Interval", "TIME", mapOf("subType" to "TIME_INTERVAL", "intervalMinutes" to 30)),
    TriggerOption("Day of Week", "TIME", mapOf("subType" to "DAY_OF_WEEK", "time" to "09:00", "daysOfWeek" to listOf("Monday", "Friday"))),
    
    // Location triggers
    TriggerOption("Location (Geofence Enter)", "LOCATION", mapOf("latitude" to 0.0, "longitude" to 0.0, "radius" to 100.0, "transitionType" to "ENTER")),
    TriggerOption("Location (Geofence Exit)", "LOCATION", mapOf("latitude" to 0.0, "longitude" to 0.0, "radius" to 100.0, "transitionType" to "EXIT")),
    
    // Device state triggers
    TriggerOption("Screen On", "DEVICE_STATE", mapOf("event" to "SCREEN_ON")),
    TriggerOption("Screen Off", "DEVICE_STATE", mapOf("event" to "SCREEN_OFF")),
    TriggerOption("Device Unlocked", "DEVICE_STATE", mapOf("event" to "DEVICE_UNLOCKED")),
    TriggerOption("Charging Connected", "DEVICE_STATE", mapOf("event" to "CHARGING_CONNECTED")),
    TriggerOption("Charging Disconnected", "DEVICE_STATE", mapOf("event" to "CHARGING_DISCONNECTED")),
    TriggerOption("Battery Level Threshold", "DEVICE_STATE", mapOf("event" to "BATTERY_LEVEL", "threshold" to 20, "operator" to "below")),
    
    // Connectivity triggers
    TriggerOption("WiFi Connected", "CONNECTIVITY", mapOf("event" to "WIFI_CONNECTED")),
    TriggerOption("WiFi Disconnected", "CONNECTIVITY", mapOf("event" to "WIFI_DISCONNECTED")),
    TriggerOption("WiFi SSID Connected", "CONNECTIVITY", mapOf("event" to "WIFI_CONNECTED", "ssid" to "MyNetwork")),
    TriggerOption("Bluetooth Connected", "CONNECTIVITY", mapOf("event" to "BLUETOOTH_CONNECTED")),
    TriggerOption("Bluetooth Disconnected", "CONNECTIVITY", mapOf("event" to "BLUETOOTH_DISCONNECTED")),
    TriggerOption("Mobile Data Enabled", "CONNECTIVITY", mapOf("event" to "MOBILE_DATA_ENABLED")),
    
    // App event triggers
    TriggerOption("App Launched", "APP_EVENT", mapOf("event" to "APP_LAUNCHED", "packageName" to "com.android.settings")),
    TriggerOption("App Closed", "APP_EVENT", mapOf("event" to "APP_CLOSED", "packageName" to "com.android.settings")),
    TriggerOption("App Installed", "APP_EVENT", mapOf("event" to "APP_INSTALLED")),
    TriggerOption("App Uninstalled", "APP_EVENT", mapOf("event" to "APP_UNINSTALLED")),
    TriggerOption("Notification Received", "APP_EVENT", mapOf("event" to "NOTIFICATION_RECEIVED")),
    
    // Communication triggers
    TriggerOption("Call Received", "COMMUNICATION", mapOf("event" to "CALL_RECEIVED")),
    TriggerOption("Call Ended", "COMMUNICATION", mapOf("event" to "CALL_ENDED")),
    TriggerOption("Missed Call", "COMMUNICATION", mapOf("event" to "MISSED_CALL")),
    TriggerOption("SMS Received", "COMMUNICATION", mapOf("event" to "SMS_RECEIVED")),
    
    // Sensor triggers
    TriggerOption("Shake Phone", "SENSOR_EVENT", mapOf("sensor" to "SHAKE")),
    TriggerOption("Proximity Sensor", "SENSOR_EVENT", mapOf("sensor" to "PROXIMITY")),
    TriggerOption("Light Level", "SENSOR_EVENT", mapOf("sensor" to "LIGHT_LEVEL", "threshold" to 100, "operator" to "above")),
    TriggerOption("Orientation Change", "SENSOR_EVENT", mapOf("sensor" to "ORIENTATION_CHANGE")),
    
    // System event triggers (legacy)
    TriggerOption("Airplane Mode", "SYSTEM_EVENT", mapOf("event" to "AIRPLANE_MODE")),
    TriggerOption("Device Storage Low", "SYSTEM_EVENT", mapOf("event" to "STORAGE_LOW"))
)

@Composable
fun TriggerPickerDialog(
    onDismiss: () -> Unit,
    onTriggerSelected: (TriggerDTO) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Trigger") },
        text = {
            LazyColumn {
                items(triggerOptions) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        modifier = Modifier.clickable {
                            onTriggerSelected(
                                TriggerDTO(
                                    triggerType = option.type,
                                    triggerConfig = option.config
                                )
                            )
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
