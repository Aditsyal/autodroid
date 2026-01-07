package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onActionSelected: (ActionOption) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Select Action",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(
                    items = actionOptions,
                    key = { it.label }
                ) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        supportingContent = { Text(option.type.replace("_", " ").lowercase().capitalize()) },
                        modifier = Modifier.clickable {
                            onActionSelected(option)
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }
        }
    }
}

data class ActionOption(
    val label: String,
    val type: String,
    val config: Map<String, Any> = emptyMap(),
    val parameters: List<ParameterSchema> = emptyList()
)

val actionOptions = listOf(
    // System settings
    ActionOption("Set Brightness", "SET_BRIGHTNESS", emptyMap(),
        listOf(ParameterSchema("brightness", "Brightness (0-100)", ParameterType.NUMBER, 50))
    ),
    ActionOption("Volume Control", "VOLUME_CONTROL", emptyMap(),
        listOf(
            ParameterSchema("stream", "Stream", ParameterType.DROPDOWN(listOf("MUSIC", "RING", "NOTIFICATION", "ALARM", "SYSTEM")), "MUSIC"),
            ParameterSchema("level", "Volume Level (0-100)", ParameterType.NUMBER, 50)
        )
    ),
    ActionOption("WiFi Toggle", "WIFI_TOGGLE", emptyMap(),
        listOf(ParameterSchema("enabled", "Enabled", ParameterType.TOGGLE, true))
    ),
    ActionOption("Bluetooth Toggle", "BLUETOOTH_TOGGLE", emptyMap(),
        listOf(ParameterSchema("enabled", "Enabled", ParameterType.TOGGLE, true))
    ),
    
    // Communication
    ActionOption("Send SMS", "SEND_SMS", emptyMap(),
        listOf(
            ParameterSchema("phoneNumber", "Phone Number", ParameterType.TEXT, ""),
            ParameterSchema("message", "Message", ParameterType.TEXT, "Hello from AutoDroid")
        )
    ),
    ActionOption("Speak Text", "SPEAK_TEXT", emptyMap(),
        listOf(ParameterSchema("text", "Text to Speak", ParameterType.TEXT, "Hello from AutoDroid"))
    ),
    
    // App control
    ActionOption("Launch App", "LAUNCH_APP", emptyMap(),
        listOf(ParameterSchema("packageName", "Package Name", ParameterType.TEXT, ""))
    ),
    ActionOption("Open URL", "OPEN_URL", emptyMap(),
        listOf(ParameterSchema("url", "URL", ParameterType.TEXT, "https://"))
    ),
    
    // Notifications & UI
    ActionOption("Show Notification", "NOTIFICATION", emptyMap(),
        listOf(
            ParameterSchema("title", "Title", ParameterType.TEXT, "AutoDroid"),
            ParameterSchema("message", "Message", ParameterType.TEXT, "Macro Executed")
        )
    ),
    ActionOption("Show Toast", "SHOW_TOAST", emptyMap(),
        listOf(ParameterSchema("message", "Message", ParameterType.TEXT, "Hello from AutoDroid!"))
    ),
    
    // Automation
    ActionOption("Delay", "DELAY", emptyMap(),
        listOf(ParameterSchema("delaySeconds", "Delay (Seconds)", ParameterType.NUMBER, 5))
    ),
    ActionOption("Vibrate", "VIBRATE", emptyMap(),
        listOf(ParameterSchema("duration", "Duration (ms)", ParameterType.NUMBER, 500))
    )
)

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
