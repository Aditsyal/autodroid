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
    ActionOption("Show Toast", "SHOW_TOAST", mapOf("message" to "Hello from AutoDroid!")),
    ActionOption("Show Notification", "SHOW_NOTIFICATION", mapOf("title" to "AutoDroid", "message" to "Macro Executed")),
    ActionOption("Log to History", "LOG_HISTORY", emptyMap())
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
