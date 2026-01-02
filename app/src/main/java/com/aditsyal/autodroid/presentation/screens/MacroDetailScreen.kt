package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.presentation.viewmodels.MacroEditorViewModel

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun MacroDetailScreen(
    macroId: Long,
    onBack: () -> Unit,
    onEdit: (Long) -> Unit,
    viewModel: MacroEditorViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(macroId) {
        viewModel.loadMacro(macroId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Macro Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onEdit(macroId) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        val macro = uiState.currentMacro
        if (macro != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = macro.name,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = macro.description.ifBlank { "No description provided" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Status",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = if (macro.enabled) "Enabled" else "Disabled",
                        color = if (macro.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                HorizontalDivider()

                DetailSection(title = "Triggers") {
                    macro.triggers.forEach { trigger ->
                        DetailTriggerItem(trigger)
                    }
                    if (macro.triggers.isEmpty()) {
                        EmptyDetailText("No triggers configured")
                    }
                }

                DetailSection(title = "Actions") {
                    macro.actions.forEach { action ->
                        DetailActionItem(action)
                    }
                    if (macro.actions.isEmpty()) {
                        EmptyDetailText("No actions configured")
                    }
                }

                DetailSection(title = "Constraints") {
                    macro.constraints.forEach { constraint ->
                        DetailConstraintItem(constraint)
                    }
                    if (macro.constraints.isEmpty()) {
                        EmptyDetailText("No constraints configured")
                    }
                }
            }
        } else if (uiState.isLoading) {
            // Loading state could be handled here
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        content()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun EmptyDetailText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
    )
}

@Composable
private fun DetailTriggerItem(trigger: TriggerDTO) {
    ListItem(
        headlineContent = {
            val label = trigger.triggerConfig["event"]?.toString() ?: trigger.triggerType
            Text(label.replace("_", " ").lowercase().capitalize())
        },
        supportingContent = { Text(trigger.triggerType) }
    )
}

@Composable
private fun DetailActionItem(action: ActionDTO) {
    ListItem(
        headlineContent = { Text(action.actionType.replace("_", " ").lowercase().capitalize()) },
        supportingContent = {
            val config = action.actionConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(config.ifBlank { "No configuration" })
        }
    )
}

@Composable
private fun DetailConstraintItem(constraint: ConstraintDTO) {
    ListItem(
        headlineContent = {
            Text(constraint.constraintType.replace("_", " ").lowercase().capitalize())
        },
        supportingContent = {
            val config = constraint.constraintConfig.entries.joinToString { "${it.key}: ${it.value}" }
            Text(config.ifBlank { "No configuration" })
        }
    )
}
