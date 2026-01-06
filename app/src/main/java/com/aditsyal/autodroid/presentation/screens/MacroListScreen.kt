package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.presentation.components.MacroCard
import com.aditsyal.autodroid.presentation.viewmodels.MacroListUiState
import com.aditsyal.autodroid.presentation.viewmodels.MacroListViewModel
import kotlinx.coroutines.launch

@Composable
fun MacroListScreen(
    onAddMacro: () -> Unit,
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
        onAddMacro = onAddMacro,
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
    onAddMacro: () -> Unit,
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
            ExtendedFloatingActionButton(
                onClick = onAddMacro,
                icon = { Icon(Icons.Default.Add, contentDescription = "Create new macro") },
                text = { Text("Create Macro") }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingState()
                }

                uiState.macros.isEmpty() -> {
                    EmptyState(onAddMacro = onAddMacro)
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp,
                            top = 12.dp,
                            end = 16.dp,
                            bottom = 88.dp // Extra padding to avoid FAB overlap
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.macros) { macro ->
                            MacroCard(
                                macro = macro,
                                onToggle = onToggleMacro,
                                onExecute = onExecuteMacro,
                                onView = onViewMacro,
                                onEdit = onEditMacro,
                                onDelete = onDeleteMacro
                            )
                        }
                    }
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
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyState(onAddMacro: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No macros yet",
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Create your first automation to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        Button(onClick = onAddMacro) {
            Text("Create macro")
        }
    }
}
