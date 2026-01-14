package com.aditsyal.autodroid.presentation.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.presentation.viewmodels.ExportMacrosViewModel
import java.io.File

enum class ExportType {
    All, Single
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportMacrosScreen(
    onBackClick: () -> Unit,
    viewModel: ExportMacrosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val macros by viewModel.macros.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var selectedExportType by remember { mutableStateOf(ExportType.All) }
    var selectedMacroId by remember { mutableStateOf<Long?>(null) }

    ExportMacrosScreenContent(
        uiState = uiState,
        macros = macros,
        context = context,
        selectedExportType = selectedExportType,
        selectedMacroId = selectedMacroId,
        onExportTypeChange = { selectedExportType = it },
        onMacroSelect = { selectedMacroId = it },
        onBackClick = onBackClick,
        onExportAllMacros = viewModel::exportAllMacros,
        onExportSingleMacro = viewModel::exportSingleMacro,
        onShareExport = { ctx, uri -> shareExportFile(ctx, uri) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportMacrosScreenContent(
    uiState: ExportMacrosViewModel.ExportState,
    macros: List<MacroDTO>,
    context: android.content.Context,
    selectedExportType: ExportType = ExportType.All,
    selectedMacroId: Long? = null,
    onExportTypeChange: (ExportType) -> Unit = {},
    onMacroSelect: (Long?) -> Unit = {},
    onBackClick: () -> Unit = {},
    onExportAllMacros: () -> Unit = {},
    onExportSingleMacro: (Long) -> Unit = {},
    onShareExport: ((android.content.Context, android.net.Uri) -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Export Macros") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Instructions
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Export Macros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose what to export and share as JSON file.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export type selection
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Export Type",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedExportType == ExportType.All,
                                onClick = { onExportTypeChange(ExportType.All) }
                            )
                            Text(
                                text = "All Macros",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedExportType == ExportType.Single,
                                onClick = { onExportTypeChange(ExportType.Single) }
                            )
                            Text(
                                text = "Single Macro",
                                modifier = Modifier.padding(start = 8.dp)
                            )
                        }
                    }

                    if (selectedExportType == ExportType.Single) {
                        Spacer(modifier = Modifier.height(8.dp))
                        if (macros.isEmpty()) {
                            Text(
                                text = "No macros available. Create a macro first.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Select Macro",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    LazyColumn(
                                        modifier = Modifier.heightIn(max = 300.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(macros) { macro ->
                                            MacroSelectItem(
                                                macro = macro,
                                                isSelected = selectedMacroId == macro.id,
                                                onClick = { onMacroSelect(macro.id) }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export button
            val isExportEnabled = when {
                uiState is ExportMacrosViewModel.ExportState.Exporting -> false
                selectedExportType == ExportType.Single && selectedMacroId == null -> false
                else -> true
            }
            Button(
                onClick = {
                    when (selectedExportType) {
                        ExportType.All -> onExportAllMacros()
                        ExportType.Single -> selectedMacroId?.let { onExportSingleMacro(it) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = isExportEnabled
            ) {
                Text("Export Macros")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export status
            when (uiState) {
                is ExportMacrosViewModel.ExportState.Idle -> {
                    // Show nothing
                }

                is ExportMacrosViewModel.ExportState.Exporting -> {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator()
                            Column {
                                Text(
                                    text = "Exporting...",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "Generating export file",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }

                is ExportMacrosViewModel.ExportState.Success -> {
                    val result = (uiState as ExportMacrosViewModel.ExportState.Success).result

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Export Successful",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Text("File generated successfully")

                            if (result.macroCount > 0) {
                                Text("Macros exported: ${result.macroCount}")
                            }
                            if (result.variableCount > 0) {
                                Text("Variables exported: ${result.variableCount}")
                            }
                            if (result.templateCount > 0) {
                                Text("Templates exported: ${result.templateCount}")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Share button
                            OutlinedButton(
                                onClick = {
                                    result.uri?.let { uri ->
                                        if (onShareExport != null) {
                                            onShareExport(context, uri)
                                        } else {
                                            shareExportFile(context, uri)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text("Share Export File")
                            }
                        }
                    }
                }

                is ExportMacrosViewModel.ExportState.Error -> {
                    val error = (uiState as ExportMacrosViewModel.ExportState.Error).error
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Column {
                                Text(
                                    text = "Export Failed",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroSelectItem(
    macro: MacroDTO,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = macro.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (macro.description.isNotBlank()) {
                    Text(
                        text = macro.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${macro.actions.size} actions, ${macro.triggers.size} triggers",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun shareExportFile(context: android.content.Context, uri: android.net.Uri) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Share Export File"))
}
