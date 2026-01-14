package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
                    val interactionSource = remember { MutableInteractionSource() }
                    ListItem(
                        headlineContent = { Text(option.label) },
                        supportingContent = { Text(option.type.replace("_", " ").lowercase().capitalize()) },
                        modifier = Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) {
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
    
    // New Phase 1 actions
    ActionOption("HTTP Request/Webhook", "HTTP_REQUEST", emptyMap(),
        listOf(
            ParameterSchema("url", "URL", ParameterType.TEXT, "https://"),
            ParameterSchema("method", "Method", ParameterType.DROPDOWN(listOf("GET", "POST", "PUT", "DELETE")), "GET"),
            ParameterSchema("body", "Request Body (Optional)", ParameterType.TEXT, ""),
            ParameterSchema("timeout", "Timeout (seconds)", ParameterType.NUMBER, 30)
        )
    ),
    ActionOption("Lock Screen", "LOCK_SCREEN", emptyMap(),
        listOf(ParameterSchema("forceLock", "Force Lock", ParameterType.TOGGLE, true))
    ),
    ActionOption("Text-to-Speech", "TTS", emptyMap(),
        listOf(
            ParameterSchema("text", "Text to Speak", ParameterType.TEXT, "Hello from AutoDroid"),
            ParameterSchema("language", "Language", ParameterType.DROPDOWN(listOf("en", "es", "fr", "de", "it", "ja", "ko", "zh", "pt", "ru")), "en"),
            ParameterSchema("pitch", "Pitch (0.5-2.0)", ParameterType.NUMBER, 1.0),
            ParameterSchema("speechRate", "Speech Rate (0.1-5.0)", ParameterType.NUMBER, 1.0)
        )
    ),
    ActionOption("Set Screen Timeout", "SCREEN_TIMEOUT", emptyMap(),
        listOf(ParameterSchema("timeoutMinutes", "Timeout (minutes)", ParameterType.NUMBER, 5))
    ),
    ActionOption("Do Not Disturb", "DND", emptyMap(),
        listOf(
            ParameterSchema("action", "Action", ParameterType.DROPDOWN(listOf("enable", "disable", "toggle")), "enable"),
            ParameterSchema("interruptionFilter", "Filter Level", ParameterType.DROPDOWN(listOf("ALL", "NONE", "PRIORITY", "ALARMS")), "NONE")
        )
    ),
    ActionOption("Media Control", "MEDIA_CONTROL", emptyMap(),
        listOf(
            ParameterSchema("action", "Action", ParameterType.DROPDOWN(listOf("play_pause", "next", "previous", "stop")), "play_pause")
        )
    ),
    ActionOption("Close App", "CLOSE_APP", emptyMap(),
        listOf(
            ParameterSchema("packageName", "Package Name", ParameterType.TEXT, ""),
            ParameterSchema("forceStop", "Force Stop", ParameterType.TOGGLE, false)
        )
    ),
    ActionOption("Clear App Cache", "CLEAR_CACHE", emptyMap(),
        listOf(
            ParameterSchema("packageName", "Package Name", ParameterType.TEXT, ""),
            ParameterSchema("clearAllUserData", "Clear All User Data", ParameterType.TOGGLE, false)
        )
    ),
    ActionOption("Unlock Screen", "UNLOCK_SCREEN", emptyMap(),
        listOf(
            ParameterSchema("useBiometricPrompt", "Use Biometric", ParameterType.TOGGLE, false),
            ParameterSchema("dismissKeyguard", "Dismiss Keyguard", ParameterType.TOGGLE, true)
        )
    ),
    ActionOption("Start Music Player", "START_MUSIC", emptyMap(),
        listOf(
            ParameterSchema("package", "Music App Package (Optional)", ParameterType.TEXT, ""),
            ParameterSchema("action", "Action", ParameterType.DROPDOWN(listOf("launch", "play", "search")), "launch")
        )
    ),
    ActionOption("Close Notification", "CLOSE_NOTIFICATION", emptyMap(),
        listOf(
            ParameterSchema("action", "Action", ParameterType.DROPDOWN(listOf("dismiss", "cancel_all")), "cancel_all"),
            ParameterSchema("packageName", "Package Name (Optional)", ParameterType.TEXT, "")
        )
    ),
    ActionOption("Set Ringtone", "SET_RINGTONE", emptyMap(),
        listOf(
            ParameterSchema("ringtoneType", "Type", ParameterType.DROPDOWN(listOf("ringtone", "notification", "alarm")), "ringtone"),
            ParameterSchema("filePath", "File Path", ParameterType.TEXT, "")
        )
    ),
    ActionOption("Delete SMS", "DELETE_SMS", emptyMap(),
        listOf(
            ParameterSchema("action", "Action", ParameterType.DROPDOWN(listOf("delete_by_id", "delete_by_number", "delete_by_thread", "delete_all_from_number")), "delete_by_number"),
            ParameterSchema("phoneNumber", "Phone Number", ParameterType.TEXT, ""),
            ParameterSchema("messageId", "Message ID (for delete_by_id)", ParameterType.NUMBER, null)
        )
    ),
    ActionOption("Make Call", "MAKE_CALL", emptyMap(),
        listOf(
            ParameterSchema("phoneNumber", "Phone Number", ParameterType.TEXT, ""),
            ParameterSchema("useDialer", "Use Dialer (don't call)", ParameterType.TOGGLE, false)
        )
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
