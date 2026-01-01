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
    val config: Map<String, Any> = emptyMap(),
    val parameters: List<ParameterSchema> = emptyList()
)

val triggerOptions = listOf(
    // Time-based triggers
    TriggerOption(
        "Specific Time", "TIME", mapOf("subType" to "SPECIFIC_TIME"),
        listOf(
            ParameterSchema("time", "Time", ParameterType.TIME, "12:00"),
            ParameterSchema("days", "Days", ParameterType.DROPDOWN(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")), listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))
        )
    ),
    TriggerOption("Time Interval", "TIME", mapOf("subType" to "TIME_INTERVAL"),
        listOf(ParameterSchema("intervalMinutes", "Interval (Minutes)", ParameterType.NUMBER, 30))
    ),
    
    // Location triggers
    TriggerOption("Location (Geofence Enter)", "LOCATION", mapOf("transitionType" to "ENTER"),
        listOf(
            ParameterSchema("latitude", "Latitude", ParameterType.NUMBER, 0.0),
            ParameterSchema("longitude", "Longitude", ParameterType.NUMBER, 0.0),
            ParameterSchema("radius", "Radius (meters)", ParameterType.NUMBER, 100.0)
        )
    ),
    
    // Device state triggers
    TriggerOption("Screen On", "DEVICE_STATE", mapOf("event" to "SCREEN_ON")),
    TriggerOption("Screen Off", "DEVICE_STATE", mapOf("event" to "SCREEN_OFF")),
    TriggerOption("Device Unlocked", "DEVICE_STATE", mapOf("event" to "DEVICE_UNLOCKED")),
    TriggerOption("Battery Level Threshold", "DEVICE_STATE", mapOf("event" to "BATTERY_LEVEL"),
        listOf(
            ParameterSchema("threshold", "Battery Level (%)", ParameterType.NUMBER, 20),
            ParameterSchema("operator", "Operator", ParameterType.DROPDOWN(listOf("above", "below", "equals")), "below")
        )
    ),
    
    // Connectivity triggers
    TriggerOption("WiFi Connected", "CONNECTIVITY", mapOf("event" to "WIFI_CONNECTED"),
        listOf(ParameterSchema("ssid", "SSID (Optional)", ParameterType.TEXT, ""))
    ),
    TriggerOption("Bluetooth Connected", "CONNECTIVITY", mapOf("event" to "BLUETOOTH_CONNECTED"),
        listOf(ParameterSchema("deviceAddress", "Device Address (Optional)", ParameterType.TEXT, ""))
    ),
    
    // App event triggers
    TriggerOption("App Launched", "APP_EVENT", mapOf("event" to "APP_LAUNCHED"),
        listOf(ParameterSchema("packageName", "Package Name", ParameterType.TEXT, "com.android.settings"))
    ),
    TriggerOption("Notification Received", "APP_EVENT", mapOf("event" to "NOTIFICATION_RECEIVED"),
        listOf(ParameterSchema("packageName", "Package Name (Optional)", ParameterType.TEXT, ""))
    ),
    
    // Communication triggers
    TriggerOption("SMS Received", "COMMUNICATION", mapOf("event" to "SMS_RECEIVED"),
        listOf(ParameterSchema("phoneNumber", "Sender Phone Number (Optional)", ParameterType.TEXT, ""))
    ),
    
    // Sensor triggers
    TriggerOption("Shake Phone", "SENSOR_EVENT", mapOf("sensor" to "SHAKE")),
    TriggerOption("Light Level", "SENSOR_EVENT", mapOf("sensor" to "LIGHT_LEVEL"),
        listOf(
            ParameterSchema("threshold", "Threshold (lux)", ParameterType.NUMBER, 100),
            ParameterSchema("operator", "Operator", ParameterType.DROPDOWN(listOf("above", "below")), "above")
        )
    )
)

@Composable
fun TriggerPickerDialog(
    onDismiss: () -> Unit,
    onTriggerSelected: (TriggerOption) -> Unit
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
                            onTriggerSelected(option)
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
