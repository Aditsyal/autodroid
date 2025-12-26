package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aditsyal.autodroid.data.models.ActionDTO

data class ActionOption(
    val label: String,
    val type: String,
    val config: Map<String, Any> = emptyMap()
)

val actionOptions = listOf(
    // System settings
    ActionOption("Set Brightness", "SET_BRIGHTNESS", mapOf("brightness" to 50)),
    ActionOption("Volume Control", "VOLUME_CONTROL", mapOf("stream" to "MUSIC", "level" to 50)),
    ActionOption("Toggle Airplane Mode", "TOGGLE_AIRPLANE_MODE", mapOf("enabled" to true)),
    ActionOption("Toggle GPS", "TOGGLE_GPS", mapOf("enabled" to true)),
    ActionOption("Set Screen Timeout", "SET_SCREEN_TIMEOUT", mapOf("timeoutSeconds" to 300)),
    ActionOption("WiFi Toggle", "WIFI_TOGGLE", mapOf("enabled" to true)),
    ActionOption("Bluetooth Toggle", "BLUETOOTH_TOGGLE", mapOf("enabled" to true)),
    
    // Device control
    ActionOption("Lock Screen", "LOCK_SCREEN", emptyMap()),
    ActionOption("Sleep Device", "SLEEP_DEVICE", emptyMap()),
    ActionOption("Vibrate", "VIBRATE", mapOf("duration" to 500)),
    ActionOption("Enable Do Not Disturb", "ENABLE_DO_NOT_DISTURB", emptyMap()),
    ActionOption("Disable Do Not Disturb", "DISABLE_DO_NOT_DISTURB", emptyMap()),
    
    // Communication
    ActionOption("Send SMS", "SEND_SMS", mapOf("phoneNumber" to "+1234567890", "message" to "Hello")),
    ActionOption("Send Email", "SEND_EMAIL", mapOf("to" to "example@email.com", "subject" to "Subject", "body" to "Body")),
    ActionOption("Make Call", "MAKE_CALL", mapOf("phoneNumber" to "+1234567890")),
    ActionOption("Speak Text", "SPEAK_TEXT", mapOf("text" to "Hello from AutoDroid")),
    
    // App control
    ActionOption("Launch App", "LAUNCH_APP", mapOf("packageName" to "com.android.settings")),
    ActionOption("Close App", "CLOSE_APP", mapOf("packageName" to "com.android.settings")),
    ActionOption("Open URL", "OPEN_URL", mapOf("url" to "https://example.com")),
    
    // Media
    ActionOption("Play Sound", "PLAY_SOUND", mapOf("soundType" to "NOTIFICATION")),
    ActionOption("Stop Sound", "STOP_SOUND", emptyMap()),
    ActionOption("Change Wallpaper", "CHANGE_WALLPAPER", mapOf("imageUri" to "content://...")),
    
    // Notifications & UI
    ActionOption("Show Notification", "NOTIFICATION", mapOf("title" to "AutoDroid", "message" to "Macro Executed")),
    ActionOption("Show Toast", "SHOW_TOAST", mapOf("message" to "Hello from AutoDroid!")),
    
    // Automation
    ActionOption("Delay", "DELAY", mapOf("delaySeconds" to 5)),
    ActionOption("HTTP Request", "HTTP_REQUEST", mapOf("url" to "https://api.example.com", "method" to "GET")),
    ActionOption("Set Variable", "SET_VARIABLE", mapOf("variableName" to "myVar", "value" to "Hello", "scope" to "LOCAL")),
    ActionOption("Log to History", "LOG_HISTORY", mapOf("message" to "Custom log entry"))
)

@Composable
fun ActionPickerDialog(
    onDismiss: () -> Unit,
    onActionSelected: (ActionDTO) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Action") },
        text = {
            LazyColumn {
                items(actionOptions) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        modifier = Modifier.clickable {
                            onActionSelected(
                                ActionDTO(
                                    actionType = option.type,
                                    actionConfig = option.config,
                                    executionOrder = 0 // Will be set by ViewModel
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
