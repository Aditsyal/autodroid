package com.aditsyal.autodroid.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.presentation.viewmodels.DryRunPreviewViewModel
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DryRunPreviewScreen(
    macroId: Long,
    onBackClick: () -> Unit,
    viewModel: DryRunPreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Trigger macro loading and simulation when screen loads
    androidx.compose.runtime.LaunchedEffect(macroId) {
        viewModel.loadMacroAndSimulate(macroId)
    }

    val onRetry = { viewModel.retrySimulation() }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Dry Run Preview")
                        val macroName = uiState.result?.macro?.name ?: "Loading..."
                        Text(
                            text = macroName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (uiState.result != null) {
                        IconButton(onClick = onRetry) {
                            Icon(Icons.Default.Refresh, contentDescription = "Retry")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingScreen(modifier = Modifier.padding(padding))
            }
            uiState.error != null -> {
                ErrorScreen(
                    error = uiState.error!!,
                    onRetry = onRetry,
                    modifier = Modifier.padding(padding)
                )
            }
            uiState.result != null -> {
                DryRunContent(
                    result = uiState.result!!,
                    selectedStepIndex = uiState.selectedStepIndex,
                    onStepClick = { viewModel.selectStep(it) },
                    onStepClose = { viewModel.clearSelection() },
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun LoadingScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Analyzing macro execution...",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "This may take a moment",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Simulation Failed",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun DryRunContent(
    result: com.aditsyal.autodroid.domain.usecase.DryRunUseCase.DryRunResult,
    selectedStepIndex: Int?,
    onStepClick: (Int) -> Unit,
    onStepClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Summary Card
        SummaryCard(
            result = result,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Blocking Issues Warning
        if (result.blockingIssues.isNotEmpty()) {
            BlockingIssuesCard(
                issues = result.blockingIssues,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Step Details
        selectedStepIndex?.let { index ->
            val step = result.steps.getOrNull(index)
            if (step != null) {
                StepDetailCard(
                    step = step,
                    onClose = onStepClose,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        // Steps List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Execution Steps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            itemsIndexed(result.steps) { index, step ->
                StepListItem(
                    step = step,
                    isSelected = selectedStepIndex == index,
                    onClick = { onStepClick(index) }
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(
    result: com.aditsyal.autodroid.domain.usecase.DryRunUseCase.DryRunResult,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Simulation Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                SuccessIndicator(probability = result.overallSuccessProbability)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SummaryItem(
                    label = "Duration",
                    value = formatDuration(result.totalEstimatedDuration),
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Battery Impact",
                    value = String.format("%.2f%%", result.totalBatteryImpact),
                    modifier = Modifier.weight(1f)
                )
                SummaryItem(
                    label = "Steps",
                    value = result.steps.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BlockingIssuesCard(
    issues: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Potential Issues",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            issues.forEach { issue ->
                Text(
                    text = "• $issue",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun StepDetailCard(
    step: com.aditsyal.autodroid.domain.usecase.DryRunUseCase.DryRunStep,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step ${step.stepNumber}: ${step.title}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Close details"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StepMetric(
                    label = "Duration",
                    value = formatDuration(step.estimatedDuration),
                    modifier = Modifier.weight(1f)
                )
                StepMetric(
                    label = "Battery",
                    value = String.format("%.3f%%", step.batteryImpact),
                    modifier = Modifier.weight(1f)
                )
                StepMetric(
                    label = "Success",
                    value = String.format("%.0f%%", step.successProbability * 100),
                    modifier = Modifier.weight(1f)
                )
            }

            if (step.warnings.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                step.warnings.forEach { warning ->
                    Row(
                        modifier = Modifier.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = warning,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepListItem(
    step: com.aditsyal.autodroid.domain.usecase.DryRunUseCase.DryRunStep,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    StepTypeIcon(step.type)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${step.stepNumber}. ${step.title}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            },
            supportingContent = {
                Column {
                    Text(
                        text = step.description,
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (step.warnings.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${step.warnings.size} warning(s)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            },
            trailingContent = {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDuration(step.estimatedDuration),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format("%.0f%%", step.successProbability * 100),
                        style = MaterialTheme.typography.bodySmall,
                        color = getSuccessColor(step.successProbability)
                    )
                }
            }
        )
    }
}

@Composable
private fun StepTypeIcon(type: com.aditsyal.autodroid.domain.usecase.DryRunUseCase.StepType) {
    val (icon, color) = when (type) {
        com.aditsyal.autodroid.domain.usecase.DryRunUseCase.StepType.TRIGGER_CHECK -> {
            Icons.Default.PlayArrow to MaterialTheme.colorScheme.primary
        }
        com.aditsyal.autodroid.domain.usecase.DryRunUseCase.StepType.CONSTRAINT_EVALUATION -> {
            Icons.Default.CheckCircle to MaterialTheme.colorScheme.secondary
        }
        com.aditsyal.autodroid.domain.usecase.DryRunUseCase.StepType.ACTION_EXECUTION -> {
            Icons.Default.Info to MaterialTheme.colorScheme.tertiary
        }
        com.aditsyal.autodroid.domain.usecase.DryRunUseCase.StepType.DELAY -> {
            Icons.Default.Refresh to MaterialTheme.colorScheme.outline
        }
    }

    Surface(
        modifier = Modifier.size(24.dp),
        shape = CircleShape,
        color = color.copy(alpha = 0.1f)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = color
            )
        }
    }
}

@Composable
private fun SuccessIndicator(probability: Float) {
    val color = getSuccessColor(probability)
    val text = when {
        probability >= 0.95f -> "Excellent"
        probability >= 0.85f -> "Good"
        probability >= 0.70f -> "Fair"
        else -> "Poor"
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepMetric(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.Start) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getSuccessColor(probability: Float): Color {
    return when {
        probability >= 0.95f -> Color(0xFF4CAF50) // Green
        probability >= 0.85f -> Color(0xFF8BC34A) // Light Green
        probability >= 0.70f -> Color(0xFFFFC107) // Yellow
        else -> Color(0xFFF44336) // Red
    }
}

private fun formatDuration(duration: kotlin.time.Duration): String {
    val totalMillis = duration.inWholeMilliseconds

    return when {
        totalMillis < 1000 -> "${totalMillis}ms"
        totalMillis < 60000 -> String.format("%.1fs", totalMillis / 1000.0)
        else -> String.format("%.1fm", totalMillis / 60000.0)
    }
}