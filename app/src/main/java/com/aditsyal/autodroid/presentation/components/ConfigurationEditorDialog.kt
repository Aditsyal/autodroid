package com.aditsyal.autodroid.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun ConfigurationEditorDialog(
    title: String,
    parameters: List<ParameterSchema>,
    initialValues: Map<String, Any> = emptyMap(),
    onDismiss: () -> Unit,
    onSave: (Map<String, Any>) -> Unit
) {
    val configValues = remember { 
        mutableStateMapOf<String, Any>().apply {
            parameters.forEach { param ->
                put(param.key, initialValues[param.key] ?: param.defaultValue ?: "")
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                parameters.forEach { parameter ->
                    ConfigField(
                        parameter = parameter,
                        value = configValues[parameter.key] ?: "",
                        onValueChange = { configValues[parameter.key] = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSave(configValues.toMap()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ConfigField(
    parameter: ParameterSchema,
    value: Any,
    onValueChange: (Any) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = parameter.label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(4.dp))
        
        when (val type = parameter.type) {
            is ParameterType.TEXT -> {
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            is ParameterType.NUMBER -> {
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = { 
                        // Only allow numeric input
                        if (it.isEmpty() || it.toDoubleOrNull() != null || it.toLongOrNull() != null) {
                            onValueChange(it)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            is ParameterType.TOGGLE -> {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(if (value as? Boolean == true) "On" else "Off")
                    Switch(
                        checked = value as? Boolean ?: false,
                        onCheckedChange = onValueChange
                    )
                }
            }
            is ParameterType.TIME -> {
                // Simplified time input for now
                OutlinedTextField(
                    value = value.toString(),
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("HH:mm") },
                    singleLine = true
                )
            }
            is ParameterType.DROPDOWN -> {
                var expanded by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(value.toString())
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        type.options.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    onValueChange(option)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
