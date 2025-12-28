package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.presentation.components.ActionPickerDialog
import com.aditsyal.autodroid.presentation.components.ConstraintPickerDialog
import com.aditsyal.autodroid.presentation.components.TriggerPickerDialog
import com.aditsyal.autodroid.presentation.viewmodels.MacroEditorViewModel
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MacroEditorScreen(
    macroId: Long?,
    templateId: Long? = null,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    viewModel: MacroEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }

    var showTriggerPicker by remember { mutableStateOf(false) }
    var showActionPicker by remember { mutableStateOf(false) }
    var showConstraintPicker by remember { mutableStateOf(false) }

    LaunchedEffect(macroId, templateId) {
        when {
            templateId != null -> viewModel.loadFromTemplate(templateId)
            macroId != null && macroId != 0L -> viewModel.loadMacro(macroId)
            else -> {
                // New macro, do nothing - viewModel already has empty state
            }
        }
    }

    LaunchedEffect(uiState.currentMacro) {
        uiState.currentMacro?.let { macro ->
            name = macro.name
            description = macro.description
            enabled = macro.enabled
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) {
            onSaved()
            viewModel.clearTransientState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (macroId == null || macroId == 0L) "Create Macro" else "Edit Macro",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            saveMacro(
                                macroId = macroId,
                                name = name,
                                description = description,
                                enabled = enabled,
                                uiState = uiState,
                                viewModel = viewModel
                            )
                        },
                        enabled = !uiState.isSaving
                    ) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Details",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Name") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                label = { Text("Description") },
                maxLines = 5
            )

            RowSwitch(
                label = "Enabled",
                checked = enabled,
                onCheckedChange = { enabled = it }
            )

            HorizontalDivider()

            SectionHeader(
                title = "Triggers",
                onAddClick = { showTriggerPicker = true }
            )

            uiState.currentMacro?.triggers?.forEach { trigger ->
                TriggerItem(trigger = trigger, onDelete = { viewModel.removeTrigger(trigger) })
            }

            if (uiState.currentMacro?.triggers?.isEmpty() == true) {
                Text(
                    "No triggers added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(
                title = "Actions",
                onAddClick = { showActionPicker = true }
            )

            uiState.currentMacro?.actions?.forEach { action ->
                ActionItem(action = action, onDelete = { viewModel.removeAction(action) })
            }

            if (uiState.currentMacro?.actions?.isEmpty() == true) {
                Text(
                    "No actions added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            SectionHeader(
                title = "Constraints",
                onAddClick = { showConstraintPicker = true }
            )

            uiState.currentMacro?.constraints?.forEach { constraint ->
                ConstraintItem(constraint = constraint, onDelete = { viewModel.removeConstraint(constraint) })
            }

            if (uiState.currentMacro?.constraints?.isEmpty() == true) {
                Text(
                    "No constraints added",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    saveMacro(
                        macroId = macroId,
                        name = name,
                        description = description,
                        enabled = enabled,
                        uiState = uiState,
                        viewModel = viewModel
                    )
                },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (uiState.isSaving) "Saving..." else "Save")
            }
        }
    }

    if (showTriggerPicker) {
        TriggerPickerDialog(
            onDismiss = { showTriggerPicker = false },
            onTriggerSelected = {
                viewModel.addTrigger(it)
                showTriggerPicker = false
            }
        )
    }

    if (showActionPicker) {
        ActionPickerDialog(
            onDismiss = { showActionPicker = false },
            onActionSelected = {
                viewModel.addAction(it)
                showActionPicker = false
            }
        )
    }

    if (showConstraintPicker) {
        ConstraintPickerDialog(
            onDismiss = { showConstraintPicker = false },
            onConstraintSelected = {
                viewModel.addConstraint(it)
                showConstraintPicker = false
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add $title")
        }
    }
}

@Composable
private fun TriggerItem(
    trigger: TriggerDTO,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { 
            val label = trigger.triggerConfig["event"]?.toString() ?: trigger.triggerType
            Text(label.replace("_", " ").lowercase().capitalize()) 
        },
        supportingContent = { Text(trigger.triggerType) },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun ActionItem(
    action: ActionDTO,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { Text(action.actionType.replace("_", " ").lowercase().capitalize()) },
        supportingContent = { 
            val config = action.actionConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(config.ifBlank { "No configuration" }) 
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

@Composable
private fun ConstraintItem(
    constraint: ConstraintDTO,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(constraint.constraintType.replace("_", " ").lowercase().capitalize())
        },
        supportingContent = { 
            val config = constraint.constraintConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(config.ifBlank { "No configuration" })
        },
        trailingContent = {
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    )
}

fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

private fun saveMacro(
    macroId: Long?,
    name: String,
    description: String,
    enabled: Boolean,
    uiState: com.aditsyal.autodroid.presentation.viewmodels.MacroEditorUiState,
    viewModel: MacroEditorViewModel
) {
    val existing = uiState.currentMacro
    val macro = MacroDTO(
        id = existing?.id ?: macroId ?: 0L,
        name = name,
        description = description,
        enabled = enabled,
        createdAt = existing?.createdAt ?: System.currentTimeMillis(),
        lastExecuted = existing?.lastExecuted,
        triggers = existing?.triggers ?: emptyList(),
        actions = existing?.actions ?: emptyList(),
        constraints = existing?.constraints ?: emptyList()
    )
    viewModel.saveMacro(macro)
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.titleMedium)
            Text(
                text = if (checked) "This macro is active" else "This macro is disabled",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}


