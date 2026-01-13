package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.presentation.viewmodels.MacroEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroDetailScreen(
    macroId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDryRun: (Long) -> Unit = {},
    viewModel: MacroEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    LaunchedEffect(macroId) {
        viewModel.loadMacro(macroId)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Macro Details") },
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
                    IconButton(
                        onClick = { onDryRun(macroId) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Dry Run",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = { onEdit(macroId) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        val macro = uiState.currentMacro
        if (macro != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = macro.name,
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        color = if (macro.enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.extraSmall
                    ) {
                        Text(
                            text = if (macro.enabled) "Active" else "Disabled",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = if (macro.enabled) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = macro.description.ifBlank { "No description provided" },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

                DetailSection(
                    title = "Triggers",
                    description = "When this macro starts"
                ) {
                    macro.triggers.forEach { trigger ->
                        DetailTriggerItem(trigger)
                    }
                    if (macro.triggers.isEmpty()) {
                        EmptyDetailText("No triggers configured")
                    }
                }

                DetailSection(
                    title = "Actions",
                    description = "What this macro does"
                ) {
                    macro.actions.forEach { action ->
                        DetailActionItem(action)
                    }
                    if (macro.actions.isEmpty()) {
                        EmptyDetailText("No actions configured")
                    }
                }

                DetailSection(
                    title = "Constraints",
                    description = "Conditions for execution"
                ) {
                    macro.constraints.forEach { constraint ->
                        DetailConstraintItem(constraint)
                    }
                    if (macro.constraints.isEmpty()) {
                        EmptyDetailText("No constraints configured")
                    }
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        } else if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    description: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        content()
    }
}

@Composable
private fun EmptyDetailText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun DetailTriggerItem(trigger: TriggerDTO) {
    ListItem(
        headlineContent = {
            val label = trigger.triggerConfig["event"]?.toString() ?: trigger.triggerType
            Text(label.replace("_", " ").lowercase().capitalize(), fontWeight = FontWeight.Medium)
        },
        supportingContent = { Text(trigger.triggerType, style = MaterialTheme.typography.bodySmall) },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun DetailActionItem(action: ActionDTO) {
    ListItem(
        headlineContent = { 
            Text(action.actionType.replace("_", " ").lowercase().capitalize(), fontWeight = FontWeight.Medium) 
        },
        supportingContent = {
            val config = action.actionConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(config.ifBlank { "No configuration" }, style = MaterialTheme.typography.bodySmall)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}

@Composable
private fun DetailConstraintItem(constraint: ConstraintDTO) {
    ListItem(
        headlineContent = {
            Text(constraint.constraintType.replace("_", " ").lowercase().capitalize(), fontWeight = FontWeight.Medium)
        },
        supportingContent = {
            val config = constraint.constraintConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(config.ifBlank { "No configuration" }, style = MaterialTheme.typography.bodySmall)
        },
        colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
