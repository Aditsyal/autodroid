package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState
import com.aditsyal.autodroid.presentation.viewmodels.VariableManagementUiState
import com.aditsyal.autodroid.presentation.viewmodels.VariableManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariableManagementScreen(
    onBackClick: () -> Unit,
    viewModel: VariableManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dialogState by viewModel.dialogState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Variables") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showAddDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Variable")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is VariableManagementUiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                is VariableManagementUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is VariableManagementUiState.Success -> {
                    if (state.variables.isEmpty()) {
                        Text(
                            text = "No variables found",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.variables, key = { it.id }) { variable ->
                                VariableItem(
                                    variable = variable,
                                    onEdit = { viewModel.showEditDialog(variable) },
                                    onDelete = { viewModel.deleteVariable(variable) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (val state = dialogState) {
        is VariableDialogState.Add -> {
            VariableDialog(
                title = "Add Variable",
                onDismiss = { viewModel.hideDialog() },
                onConfirm = { name, value, type ->
                    viewModel.createVariable(name, value, type)
                }
            )
        }
        is VariableDialogState.Edit -> {
            VariableDialog(
                title = "Edit Variable",
                initialName = state.variable.name,
                initialValue = state.variable.value,
                initialType = state.variable.type,
                isNameEditable = false,
                onDismiss = { viewModel.hideDialog() },
                onConfirm = { _, value, type ->
                    viewModel.updateVariable(state.variable, value, type)
                }
            )
        }
        VariableDialogState.Hidden -> {}
    }
}

@Composable
fun VariableItem(
    variable: VariableDTO,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        ListItem(
            headlineContent = { Text(variable.name, style = MaterialTheme.typography.titleMedium) },
            supportingContent = {
                Column {
                    Text("Value: ${variable.value}")
                    Text("Type: ${variable.type}", style = MaterialTheme.typography.labelSmall)
                }
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
        )
    }
}

@Composable
fun VariableDialog(
    title: String,
    initialName: String = "",
    initialValue: String = "",
    initialType: String = "STRING",
    isNameEditable: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var value by remember { mutableStateOf(initialValue) }
    var type by remember { mutableStateOf(initialType) }
    var expanded by remember { mutableStateOf(false) }
    val types = listOf("STRING", "NUMBER", "BOOLEAN")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (isNameEditable) name = it },
                    label = { Text("Name") },
                    enabled = isNameEditable,
                    singleLine = true
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Value") },
                    singleLine = true
                )
                Box {
                    OutlinedTextField(
                        value = type,
                        onValueChange = {},
                        label = { Text("Type") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, "Select Type") // Assuming icon exists or default arrow
                            }
                        },
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        types.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    type = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, value, type) },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariableManagementScreenContent(
    uiState: VariableManagementUiState,
    dialogState: com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState,
    onBackClick: () -> Unit,
    onShowAddDialog: () -> Unit,
    onShowEditDialog: (VariableDTO) -> Unit,
    onHideDialog: () -> Unit,
    onCreateVariable: (String, String, String) -> Unit,
    onUpdateVariable: (VariableDTO, String, String) -> Unit,
    onDeleteVariable: (VariableDTO) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Variables") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onShowAddDialog) {
                Icon(Icons.Default.Add, contentDescription = "Add Variable")
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState) {
                is VariableManagementUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is VariableManagementUiState.Error -> {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                is VariableManagementUiState.Success -> {
                    if (state.variables.isEmpty()) {
                        Text(
                            text = "No variables found",
                            modifier = Modifier.align(Alignment.Center),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.variables, key = { it.id }) { variable ->
                                VariableItem(
                                    variable = variable,
                                    onEdit = { onShowEditDialog(variable) },
                                    onDelete = { onDeleteVariable(variable) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    when (val state = dialogState) {
        is com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Add -> {
            VariableDialog(
                title = "Add Variable",
                onDismiss = onHideDialog,
                onConfirm = onCreateVariable
            )
        }
        is com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Edit -> {
            VariableDialog(
                title = "Edit Variable",
                initialName = state.variable.name,
                initialValue = state.variable.value,
                initialType = state.variable.type,
                isNameEditable = false,
                onDismiss = onHideDialog,
                onConfirm = { _, value, type ->
                    onUpdateVariable(state.variable, value, type)
                }
            )
        }
        com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden -> {}
    }
}
