package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aditsyal.autodroid.data.models.ConstraintDTO

data class ConstraintOption(
    val label: String,
    val type: String,
    val config: Map<String, Any> = emptyMap()
)

val constraintOptions = listOf(
    // Time constraints
    ConstraintOption("Time Range", "TIME_RANGE", mapOf("startTime" to "08:00", "endTime" to "17:00")),
    ConstraintOption("Day of Week", "DAY_OF_WEEK", mapOf("days" to listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))),
    ConstraintOption("Exclude Weekends", "EXCLUDE_WEEKENDS", mapOf("exclude" to true)),
    ConstraintOption("Specific Date", "SPECIFIC_DATE", mapOf("date" to System.currentTimeMillis())),
    
    // Device state constraints
    ConstraintOption("Battery Level", "BATTERY_LEVEL", mapOf("value" to 20, "operator" to "greater_than")),
    ConstraintOption("Battery Level Range", "BATTERY_LEVEL_RANGE", mapOf("minLevel" to 20, "maxLevel" to 80)),
    ConstraintOption("Charging Status", "CHARGING_STATUS", mapOf("isCharging" to true)),
    ConstraintOption("Screen State", "SCREEN_STATE", mapOf("isOn" to true)),
    ConstraintOption("Device Locked", "DEVICE_LOCKED", mapOf("isLocked" to false)),
    
    // Connectivity constraints
    ConstraintOption("WiFi Connected", "WIFI_CONNECTED", emptyMap()),
    ConstraintOption("WiFi Connected (Specific SSID)", "WIFI_CONNECTED", mapOf("ssid" to "MyNetwork")),
    ConstraintOption("WiFi Disconnected", "WIFI_DISCONNECTED", emptyMap()),
    ConstraintOption("Mobile Data Active", "MOBILE_DATA_ACTIVE", emptyMap()),
    ConstraintOption("Bluetooth Connected", "BLUETOOTH_CONNECTED", emptyMap()),
    
    // Location constraints
    ConstraintOption("Inside Geofence", "INSIDE_GEOFENCE", mapOf("geofenceId" to 0L)),
    ConstraintOption("Outside Geofence", "OUTSIDE_GEOFENCE", mapOf("geofenceId" to 0L)),
    
    // Context constraints
    ConstraintOption("App Running", "APP_RUNNING", mapOf("packageName" to "com.android.settings")),
    ConstraintOption("Headphones Connected", "HEADPHONES_CONNECTED", emptyMap()),
    ConstraintOption("Do Not Disturb Enabled", "DO_NOT_DISTURB_ENABLED", emptyMap()),
    
    // Legacy constraints
    ConstraintOption("Airplane Mode", "AIRPLANE_MODE", mapOf("enabled" to false))
)

@Composable
fun ConstraintPickerDialog(
    onDismiss: () -> Unit,
    onConstraintSelected: (ConstraintDTO) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Constraint") },
        text = {
            LazyColumn {
                items(constraintOptions) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        modifier = Modifier.clickable {
                            onConstraintSelected(
                                ConstraintDTO(
                                    constraintType = option.type,
                                    constraintConfig = option.config
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

