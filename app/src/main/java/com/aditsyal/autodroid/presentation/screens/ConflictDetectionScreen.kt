package com.aditsyal.autodroid.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import com.aditsyal.autodroid.presentation.theme.MotionTokens
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.domain.usecase.ConflictDetectionUseCase
import com.aditsyal.autodroid.presentation.viewmodels.ConflictDetectionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConflictDetectionScreen(
    onBackClick: () -> Unit,
    viewModel: ConflictDetectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text("Conflict Detection")
                        val conflictCount = uiState.allConflicts.size
                        if (conflictCount > 0) {
                            Text(
                                "$conflictCount conflicts found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                "No conflicts detected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshConflicts() },
                        enabled = !uiState.isLoadingConflicts
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = when {
                uiState.isLoadingConflicts -> "loading"
                uiState.error != null -> "error"
                uiState.allConflicts.isEmpty() -> "no_conflicts"
                else -> "conflicts"
            },
            transitionSpec = {
                fadeIn(MotionTokens.MotionSpec.StateChange) togetherWith fadeOut(MotionTokens.MotionSpec.StateChange)
            },
            modifier = Modifier.padding(padding)
        ) { state ->
            when (state) {
                "loading" -> LoadingConflictsScreen()
                "error" -> ErrorConflictsScreen(
                    error = uiState.error!!,
                    onRetry = { viewModel.refreshConflicts() }
                )
                "no_conflicts" -> NoConflictsScreen()
                "conflicts" -> ConflictsListScreen(
                    conflicts = uiState.allConflicts,
                    onConflictClick = { /* Could navigate to macro details */ }
                )
            }
        }
    }
}

@Composable
private fun LoadingConflictsScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                strokeWidth = 4.dp
            )
            Text(
                text = "Analyzing macro conflicts...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorConflictsScreen(
    error: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
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
            text = "Failed to Load Conflicts",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = error,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        androidx.compose.material3.Button(onClick = onRetry) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Retry")
        }
    }
}

@Composable
private fun NoConflictsScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No Conflicts Detected",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "All your macros are working harmoniously together. Great job!",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ConflictsListScreen(
    conflicts: List<ConflictDetectionUseCase.Conflict>,
    onConflictClick: (ConflictDetectionUseCase.Conflict) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Detected Conflicts",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        items(conflicts) { conflict ->
            ConflictCard(
                conflict = conflict,
                onClick = { onConflictClick(conflict) }
            )
        }
    }
}

@Composable
private fun ConflictCard(
    conflict: ConflictDetectionUseCase.Conflict,
    onClick: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isExpanded) MotionTokens.Scale.Hover else 1f,
        animationSpec = MotionTokens.MotionSpec.ContentExpand,
        label = "conflict_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable { isExpanded = !isExpanded }
            .animateContentSize(MotionTokens.MotionSpec.ContentExpandSize),
        colors = CardDefaults.cardColors(
            containerColor = getConflictColor(conflict.severity).copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = getConflictColor(conflict.severity).copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        ConflictIcon(conflict.type)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = getConflictTypeText(conflict.type),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = getConflictColor(conflict.severity)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SeverityChip(conflict.severity)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = conflict.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(MotionTokens.MotionSpec.ContentExpand),
                    exit = fadeOut(MotionTokens.MotionSpec.ContentExpand)
                ) {
                    Column {
                        Text(
                            text = "Recommendation:",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = conflict.recommendation,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (conflict.affectedMacros.size > 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Affected Macros:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            conflict.affectedMacros.forEach { macro ->
                                Text(
                                    text = "• ${macro.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp, top = 2.dp)
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
private fun ConflictIcon(type: ConflictDetectionUseCase.ConflictType) {
    val (icon, color) = when (type) {
        ConflictDetectionUseCase.ConflictType.TRIGGER_OVERLAP -> {
            Icons.Default.Warning to MaterialTheme.colorScheme.primary
        }
        ConflictDetectionUseCase.ConflictType.ACTION_CONFLICT -> {
            Icons.Default.Error to MaterialTheme.colorScheme.error
        }
        ConflictDetectionUseCase.ConflictType.CONSTRAINT_VIOLATION -> {
            Icons.Default.Info to MaterialTheme.colorScheme.secondary
        }
        ConflictDetectionUseCase.ConflictType.RESOURCE_CONTENTION -> {
            Icons.Default.Warning to MaterialTheme.colorScheme.tertiary
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
private fun SeverityChip(severity: ConflictDetectionUseCase.Severity) {
    val (text, color) = when (severity) {
        ConflictDetectionUseCase.Severity.LOW -> "Low" to MaterialTheme.colorScheme.tertiary
        ConflictDetectionUseCase.Severity.MEDIUM -> "Medium" to MaterialTheme.colorScheme.errorContainer
        ConflictDetectionUseCase.Severity.HIGH -> "High" to MaterialTheme.colorScheme.errorContainer
        ConflictDetectionUseCase.Severity.CRITICAL -> "Critical" to MaterialTheme.colorScheme.error
    }

    AssistChip(
        onClick = { },
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            labelColor = color,
            containerColor = color.copy(alpha = 0.1f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = color.copy(alpha = 0.3f)
        )
    )
}

@Composable
private fun getConflictColor(severity: ConflictDetectionUseCase.Severity): Color {
    return when (severity) {
        ConflictDetectionUseCase.Severity.LOW -> MaterialTheme.colorScheme.tertiary
        ConflictDetectionUseCase.Severity.MEDIUM -> MaterialTheme.colorScheme.errorContainer
        ConflictDetectionUseCase.Severity.HIGH -> MaterialTheme.colorScheme.errorContainer
        ConflictDetectionUseCase.Severity.CRITICAL -> MaterialTheme.colorScheme.error
    }
}

private fun getConflictTypeText(type: ConflictDetectionUseCase.ConflictType): String {
    return when (type) {
        ConflictDetectionUseCase.ConflictType.TRIGGER_OVERLAP -> "Trigger Overlap"
        ConflictDetectionUseCase.ConflictType.ACTION_CONFLICT -> "Action Conflict"
        ConflictDetectionUseCase.ConflictType.CONSTRAINT_VIOLATION -> "Constraint Violation"
        ConflictDetectionUseCase.ConflictType.RESOURCE_CONTENTION -> "Resource Contention"
    }
}