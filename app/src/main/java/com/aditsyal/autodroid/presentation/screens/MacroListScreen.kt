package com.aditsyal.autodroid.presentation.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import com.aditsyal.autodroid.presentation.theme.MotionTokens
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.presentation.components.DeleteConfirmationDialog
import com.aditsyal.autodroid.presentation.components.MacroCard
import com.aditsyal.autodroid.presentation.viewmodels.MacroListUiState
import com.aditsyal.autodroid.presentation.viewmodels.MacroListViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun MacroListScreen(
    onAddMacro: () -> Unit,
    onImportMacros: () -> Unit,
    onExportMacros: () -> Unit,
    onViewMacro: (Long) -> Unit,
    onEditMacro: (Long) -> Unit,
    onShowHistory: () -> Unit,
    onShowConflicts: () -> Unit,
    onShowSettings: () -> Unit,
    onShowTemplates: () -> Unit,
    viewModel: MacroListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error, uiState.lastActionMessage) {
        uiState.error?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessages()
        }
        uiState.lastActionMessage?.let {
            scope.launch { snackbarHostState.showSnackbar(it) }
            viewModel.clearMessages()
        }
    }

    MacroListScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        scope = scope,
        onAddMacro = onAddMacro,
        onImportMacros = onImportMacros,
        onExportMacros = onExportMacros,
        onViewMacro = onViewMacro,
        onEditMacro = onEditMacro,
        onShowHistory = onShowHistory,
        onShowConflicts = onShowConflicts,
        onShowSettings = onShowSettings,
        onShowTemplates = onShowTemplates,
        onToggleMacro = viewModel::toggleMacro,
        onExecuteMacro = viewModel::executeMacro,
        onDeleteMacro = viewModel::deleteMacro
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroListScreenContent(
    uiState: MacroListUiState,
    snackbarHostState: SnackbarHostState,
    scope: CoroutineScope,
    onAddMacro: () -> Unit,
    onImportMacros: () -> Unit,
    onExportMacros: () -> Unit,
    onViewMacro: (Long) -> Unit,
    onEditMacro: (Long) -> Unit,
    onShowHistory: () -> Unit,
    onShowConflicts: () -> Unit,
    onShowSettings: () -> Unit,
    onShowTemplates: () -> Unit,
    onToggleMacro: (Long, Boolean) -> Unit,
    onExecuteMacro: (Long) -> Unit,
    onDeleteMacro: (Long) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    var pendingDeleteMacroId by remember { mutableStateOf<Long?>(null) }
    var isFabExpanded by remember { mutableStateOf(false) }

    val showScrollToTop by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = {
                    Column {
                        Text(
                            "Autodroid",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onShowHistory,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "View history",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = onShowConflicts,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = "Show conflicts",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = onShowTemplates,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.LibraryBooks,
                            contentDescription = "Browse templates",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    IconButton(
                        onClick = onShowSettings,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
    val fabRotation by animateFloatAsState(
        targetValue = if (isFabExpanded) 45f else 0f,
        animationSpec = MotionTokens.MotionSpec.FabExpand,
        label = "fab_rotation"
    )

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.animateContentSize()
            ) {
                AnimatedVisibility(
                    visible = isFabExpanded,
                    enter = fadeIn(animationSpec = MotionTokens.MotionSpec.FabExpand) +
                           slideInVertically(
                               animationSpec = MotionTokens.MotionSpec.FabExpandOffset,
                               initialOffsetY = { it }
                           ),
                    exit = fadeOut(animationSpec = MotionTokens.MotionSpec.FabExpand) +
                          slideOutVertically(
                              animationSpec = MotionTokens.MotionSpec.FabExpandOffset,
                              targetOffsetY = { it }
                          )
                ) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Export button (appears first)
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onExportMacros()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.animateContentSize(MotionTokens.MotionSpec.FabExpandSize)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Export", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.FileDownload, contentDescription = "Export Macros")
                            }
                        }

                        // Import button (delayed appearance)
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onImportMacros()
                            },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.animateContentSize(MotionTokens.MotionSpec.FabExpandSize)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Import", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.FileUpload, contentDescription = "Import Macros")
                            }
                        }

                        // Create button (most delayed appearance)
                        SmallFloatingActionButton(
                            onClick = {
                                isFabExpanded = false
                                onAddMacro()
                            },
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.animateContentSize(MotionTokens.MotionSpec.FabExpandSize)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Create", style = MaterialTheme.typography.labelMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Add, contentDescription = "Create Macro")
                            }
                        }
                    }
                }

                ExtendedFloatingActionButton(
                    onClick = { isFabExpanded = !isFabExpanded },
                    icon = {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = if (isFabExpanded) "Close menu" else "Actions",
                            modifier = Modifier.rotate(fabRotation)
                        )
                    },
                    text = {
                        AnimatedContent(
                            targetState = isFabExpanded,
                            transitionSpec = {
                                fadeIn(MotionTokens.MotionSpec.FabExpand) togetherWith fadeOut(MotionTokens.MotionSpec.FabExpand)
                            },
                            label = "fab_text"
                        ) { expanded ->
                            Text(if (expanded) "Close" else "Create")
                        }
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        pendingDeleteMacroId?.let { id ->
            val macro = uiState.macros.find { it.id == id }
            if (macro != null) {
                DeleteConfirmationDialog(
                    macroName = macro.name,
                    onConfirm = {
                        onDeleteMacro(id)
                        pendingDeleteMacroId = null
                    },
                    onDismiss = { pendingDeleteMacroId = null }
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AnimatedContent(
                targetState = when {
                    uiState.isLoading -> "loading"
                    uiState.macros.isEmpty() -> "empty"
                    else -> "content"
                },
                transitionSpec = {
                    fadeIn(MotionTokens.MotionSpec.StateChange) togetherWith fadeOut(MotionTokens.MotionSpec.StateChange)
                },
                label = "screen_state"
            ) { state ->
                when (state) {
                    "loading" -> LoadingState()
                    "empty" -> EmptyState(onAddMacro = onAddMacro)
                    "content" -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 12.dp,
                            end = 16.dp,
                            bottom = 88.dp // Extra padding to avoid FAB overlap
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = uiState.macros,
                            key = { it.id }
                        ) { macro ->
                            MacroCard(
                                macro = macro,
                                onToggle = onToggleMacro,
                                onExecute = onExecuteMacro,
                                onView = onViewMacro,
                                onEdit = onEditMacro,
                                onDelete = { pendingDeleteMacroId = macro.id }
                            )
                        }
                    }
                }
            }
            }

            // Optimized Scroll to Top Button
            AnimatedVisibility(
                visible = showScrollToTop,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .padding(bottom = 72.dp) // Above FAB
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        scope.launch {
                            listState.animateScrollToItem(0)
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Scroll to top")
                }
            }
        }
    }
}

@Composable
private fun LoadingState() {
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
                text = "Loading macros...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyState(onAddMacro: () -> Unit) {
    var isHovered by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AutoAwesome,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "No macros yet",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Create your first automation to get started with powerful task automation.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 32.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onAddMacro,
            modifier = Modifier.animateContentSize(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isHovered) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("Create Your First Macro")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Tip: Start with a simple automation like \"When I open WhatsApp, send a quick reply\"",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}
