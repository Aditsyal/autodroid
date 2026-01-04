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
fun ConstraintPickerDialog(
    onDismiss: () -> Unit,
    onConstraintSelected: (ConstraintOption) -> Unit
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
                text = "Select Constraint",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn {
                items(constraintOptions) { option ->
                    ListItem(
                        headlineContent = { Text(option.label) },
                        supportingContent = { Text(option.type.replace("_", " ").lowercase().capitalize()) },
                        modifier = Modifier.clickable {
                            onConstraintSelected(option)
                        },
                        colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
                    )
                }
            }
        }
    }
}

data class ConstraintOption(
    val label: String,
    val type: String,
    val config: Map<String, Any> = emptyMap(),
    val parameters: List<ParameterSchema> = emptyList()
)

val constraintOptions = listOf(
    // Time constraints
    ConstraintOption("Time Range", "TIME_RANGE", emptyMap(),
        listOf(
            ParameterSchema("startTime", "Start Time", ParameterType.TIME, "08:00"),
            ParameterSchema("endTime", "End Time", ParameterType.TIME, "17:00")
        )
    ),
    ConstraintOption("Day of Week", "DAY_OF_WEEK", emptyMap(),
        listOf(ParameterSchema("days", "Allowed Days", ParameterType.DROPDOWN(listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")), listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")))
    ),
    
    // Device state constraints
    ConstraintOption("Battery Level", "BATTERY_LEVEL", emptyMap(),
        listOf(
            ParameterSchema("value", "Battery Level (%)", ParameterType.NUMBER, 20),
            ParameterSchema("operator", "Operator", ParameterType.DROPDOWN(listOf("greater_than", "less_than", "equals")), "greater_than")
        )
    ),
    ConstraintOption("Charging Status", "CHARGING_STATUS", emptyMap(),
        listOf(ParameterSchema("isCharging", "Is Charging", ParameterType.TOGGLE, true))
    ),
    
    // Connectivity constraints
    ConstraintOption("WiFi Connected", "WIFI_CONNECTED", emptyMap(),
        listOf(ParameterSchema("ssid", "SSID (Optional)", ParameterType.TEXT, ""))
    )
)

private fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }
