package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onEditMacro: (Long) -> Unit,
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
        onEditMacro = onEditMacro,
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
    onEditMacro: (Long) -> Unit,
    onToggleMacro: (Long, Boolean) -> Unit,
    onExecuteMacro: (Long) -> Unit,
    onDeleteMacro: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Autodroid", style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Macros",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onAddMacro) {
                        Icon(Icons.Default.Add, contentDescription = "Add macro")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddMacro) {
                Icon(Icons.Default.Add, contentDescription = "Add macro")
            }
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
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.macros) { macro ->
                            MacroCard(
                                macro = macro,
                                onToggle = onToggleMacro,
                                onExecute = onExecuteMacro,
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
        androidx.compose.material3.CircularProgressIndicator()
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
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = "Create your first automation to get started.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        androidx.compose.material3.Button(onClick = onAddMacro) {
            Text("Create macro")
        }
    }
}


