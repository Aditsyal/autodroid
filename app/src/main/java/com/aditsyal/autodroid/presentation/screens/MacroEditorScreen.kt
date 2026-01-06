package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.presentation.components.*
import com.aditsyal.autodroid.presentation.viewmodels.MacroEditorViewModel

@Composable
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
    var initializedMacroId by remember { mutableStateOf<Long?>(null) }

    var showTriggerPicker by remember { mutableStateOf(false) }
    var showActionPicker by remember { mutableStateOf(false) }
    var showConstraintPicker by remember { mutableStateOf(false) }

    var pendingTriggerOption by remember { mutableStateOf<TriggerOption?>(null) }
    var pendingActionOption by remember { mutableStateOf<ActionOption?>(null) }
    var pendingConstraintOption by remember { mutableStateOf<ConstraintOption?>(null) }

    LaunchedEffect(macroId, templateId) {
        when {
            templateId != null -> viewModel.loadFromTemplate(templateId)
            macroId != null && macroId != 0L -> viewModel.loadMacro(macroId)
            else -> {
                // New macro
            }
        }
    }

    LaunchedEffect(uiState.currentMacro) {
        uiState.currentMacro?.let { macro ->
            if (macro.id != initializedMacroId) {
                name = macro.name
                description = macro.description
                enabled = macro.enabled
                initializedMacroId = macro.id
            }
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

    MacroEditorScreenContent(
        macroId = macroId,
        name = name,
        onNameChange = { name = it },
        description = description,
        onDescriptionChange = { description = it },
        enabled = enabled,
        onEnabledChange = { enabled = it },
        uiState = uiState,
        onBack = onBack,
        onSave = {
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
        },
        onAddTrigger = { showTriggerPicker = true },
        onAddAction = { showActionPicker = true },
        onAddConstraint = { showConstraintPicker = true },
        onRemoveTrigger = viewModel::removeTrigger,
        onRemoveAction = viewModel::removeAction,
        onRemoveConstraint = viewModel::removeConstraint
    )

    if (showTriggerPicker) {
        TriggerPickerDialog(
            onDismiss = { showTriggerPicker = false },
            onTriggerSelected = { option ->
                if (option.parameters.isEmpty()) {
                    viewModel.addTrigger(TriggerDTO(triggerType = option.type, triggerConfig = option.config))
                } else {
                    pendingTriggerOption = option
                }
                showTriggerPicker = false
            }
        )
    }

    if (pendingTriggerOption != null) {
        val option = pendingTriggerOption!!
        ConfigurationEditorDialog(
            title = "Configure ${option.label}",
            parameters = option.parameters,
            initialValues = option.config,
            onDismiss = { pendingTriggerOption = null },
            onSave = { config ->
                viewModel.addTrigger(TriggerDTO(triggerType = option.type, triggerConfig = option.config + config))
                pendingTriggerOption = null
            }
        )
    }

    if (showActionPicker) {
        ActionPickerDialog(
            onDismiss = { showActionPicker = false },
            onActionSelected = { option ->
                if (option.parameters.isEmpty()) {
                    viewModel.addAction(ActionDTO(actionType = option.type, actionConfig = option.config, executionOrder = 0))
                } else {
                    pendingActionOption = option
                }
                showActionPicker = false
            }
        )
    }

    if (pendingActionOption != null) {
        val option = pendingActionOption!!
        ConfigurationEditorDialog(
            title = "Configure ${option.label}",
            parameters = option.parameters,
            initialValues = option.config,
            onDismiss = { pendingActionOption = null },
            onSave = { config ->
                viewModel.addAction(ActionDTO(actionType = option.type, actionConfig = option.config + config, executionOrder = 0))
                pendingActionOption = null
            }
        )
    }

    if (showConstraintPicker) {
        ConstraintPickerDialog(
            onDismiss = { showConstraintPicker = false },
            onConstraintSelected = { option ->
                if (option.parameters.isEmpty()) {
                    viewModel.addConstraint(ConstraintDTO(constraintType = option.type, constraintConfig = option.config))
                } else {
                    pendingConstraintOption = option
                }
                showConstraintPicker = false
            }
        )
    }

    if (pendingConstraintOption != null) {
        val option = pendingConstraintOption!!
        ConfigurationEditorDialog(
            title = "Configure ${option.label}",
            parameters = option.parameters,
            initialValues = option.config,
            onDismiss = { pendingConstraintOption = null },
            onSave = { config ->
                viewModel.addConstraint(ConstraintDTO(constraintType = option.type, constraintConfig = option.config + config))
                pendingConstraintOption = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroEditorScreenContent(
    macroId: Long?,
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    uiState: com.aditsyal.autodroid.presentation.viewmodels.MacroEditorUiState,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onAddTrigger: () -> Unit,
    onAddAction: () -> Unit,
    onAddConstraint: () -> Unit,
    onRemoveTrigger: (TriggerDTO) -> Unit,
    onRemoveAction: (ActionDTO) -> Unit,
    onRemoveConstraint: (ConstraintDTO) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        if (macroId == null || macroId == 0L) "Create Macro" else "Edit Macro",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = onSave,
                        enabled = !uiState.isSaving,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = "Save macro",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Save")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Name") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text("e.g., Silent Mode at Night") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    label = { Text("Description") },
                    textStyle = MaterialTheme.typography.bodyLarge,
                    placeholder = { Text("What does this macro do?") },
                    shape = MaterialTheme.shapes.medium,
                    maxLines = 4
                )

                RowSwitch(
                    label = "Enabled",
                    checked = enabled,
                    onCheckedChange = onEnabledChange
                )
            }

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            EditorSection(
                title = "Triggers",
                description = "Define when this macro should start",
                onAddClick = onAddTrigger
            ) {
                uiState.currentMacro?.triggers?.forEach { trigger ->
                    TriggerItem(trigger = trigger, onDelete = { onRemoveTrigger(trigger) })
                }
                if (uiState.currentMacro?.triggers?.isEmpty() == true) {
                    EmptySectionText("No triggers added yet")
                }
            }

            EditorSection(
                title = "Actions",
                description = "What should happen when triggered",
                onAddClick = onAddAction
            ) {
                uiState.currentMacro?.actions?.forEach { action ->
                    ActionItem(action = action, onDelete = { onRemoveAction(action) })
                }
                if (uiState.currentMacro?.actions?.isEmpty() == true) {
                    EmptySectionText("No actions added yet")
                }
            }

            EditorSection(
                title = "Constraints",
                description = "Conditions that must be met",
                onAddClick = onAddConstraint
            ) {
                uiState.currentMacro?.constraints?.forEach { constraint ->
                    ConstraintItem(constraint = constraint, onDelete = { onRemoveConstraint(constraint) })
                }
                if (uiState.currentMacro?.constraints?.isEmpty() == true) {
                    EmptySectionText("Optional: No constraints added")
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
private fun EditorSection(
    title: String,
    description: String,
    onAddClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            FilledTonalButton(
                onClick = onAddClick,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add item", modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add")
            }
        }
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun EmptySectionText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun TriggerItem(
    trigger: TriggerDTO,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { 
            val label = trigger.triggerConfig["event"]?.toString() ?: trigger.triggerType
            Text(
                label.replace("_", " ").lowercase().capitalize(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            ) 
        },
        supportingContent = { 
            Text(
                trigger.triggerType,
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun ActionItem(
    action: ActionDTO,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                action.actionType.replace("_", " ").lowercase().capitalize(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            ) 
        },
        supportingContent = { 
            val config = action.actionConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(
                config.ifBlank { "No configuration" },
                style = MaterialTheme.typography.bodyMedium
            ) 
        },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun ConstraintItem(
    constraint: ConstraintDTO,
    onDelete: () -> Unit
) {
    ListItem(
        headlineContent = { 
            Text(
                constraint.constraintType.replace("_", " ").lowercase().capitalize(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        },
        supportingContent = { 
            val config = constraint.constraintConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(
                config.ifBlank { "No configuration" },
                style = MaterialTheme.typography.bodyMedium
            )
        },
        trailingContent = {
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

fun String.capitalize() = this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (checked) "Macro will run when triggered" else "Macro is currently paused",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
