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
    TriggerOption("Time Trigger", "TIME", mapOf("time" to "12:00", "days" to listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday"))),
    TriggerOption("Location (Geofence)", "LOCATION", mapOf("latitude" to 0.0, "longitude" to 0.0, "radius" to 100.0, "transitionType" to "ENTER")),
    TriggerOption("Shake Phone", "SENSOR_EVENT", mapOf("sensor" to "SHAKE")),
    TriggerOption("Proximity Sensor", "SENSOR_EVENT", mapOf("sensor" to "PROXIMITY")),
    TriggerOption("App Opened", "APP_EVENT", mapOf("packageName" to "com.android.settings")),
    TriggerOption("Battery Level", "SYSTEM_EVENT", mapOf("event" to "BATTERY_CHANGED")),
    TriggerOption("Airplane Mode", "SYSTEM_EVENT", mapOf("event" to "AIRPLANE_MODE")),
    TriggerOption("Device Storage Low", "SYSTEM_EVENT", mapOf("event" to "STORAGE_LOW")),
    TriggerOption("Screen On", "SYSTEM_EVENT", mapOf("event" to "SCREEN_ON")),
    TriggerOption("Screen Off", "SYSTEM_EVENT", mapOf("event" to "SCREEN_OFF"))
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
