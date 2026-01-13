package com.aditsyal.autodroid.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DateRangePickerState
import androidx.compose.material3.rememberDateRangePickerState
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.presentation.viewmodels.ExecutionHistoryUiState
import com.aditsyal.autodroid.presentation.viewmodels.ExecutionHistoryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExecutionHistoryScreen(
    onBackClick: () -> Unit,
    viewModel: ExecutionHistoryViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filterStatus by viewModel.statusFilter.collectAsState()
    val startDate by viewModel.startDate.collectAsState()
    val endDate by viewModel.endDate.collectAsState()
    var showFilterMenu by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
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
                    Row {
                        Box {
                            IconButton(
                                onClick = { showFilterMenu = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.FilterList,
                                    contentDescription = "Filter",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showFilterMenu,
                                onDismissRequest = { showFilterMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("All") },
                                    onClick = {
                                        viewModel.setStatusFilter(null)
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Success") },
                                    onClick = {
                                        viewModel.setStatusFilter("SUCCESS")
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Failure") },
                                    onClick = {
                                        viewModel.setStatusFilter("FAILURE")
                                        showFilterMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Skipped") },
                                    onClick = {
                                        viewModel.setStatusFilter("SKIPPED")
                                        showFilterMenu = false
                                    }
                                )
                            }
                        }
                        IconButton(
                            onClick = { showDateRangePicker = true },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                Icons.Default.DateRange,
                                contentDescription = "Date range filter",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Box {
                            IconButton(
                                onClick = { showExportMenu = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.FileDownload,
                                    contentDescription = "Export history",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export as CSV") },
                                    onClick = {
                                        viewModel.exportHistoryAsCsv(context)
                                        showExportMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export as JSON") },
                                    onClick = {
                                        viewModel.exportHistoryAsJson(context)
                                        showExportMenu = false
                                    }
                                )
                            }
                        }
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
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search macros...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search execution history")
                },
                singleLine = true,
                shape = MaterialTheme.shapes.small
            )

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = uiState) {
                    is ExecutionHistoryUiState.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    is ExecutionHistoryUiState.Error -> {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    is ExecutionHistoryUiState.Success -> {
                        if (state.logs.isEmpty()) {
                            EmptyHistoryState()
                        } else {
                            LazyColumn(
                                contentPadding = PaddingValues(bottom = 32.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    items = state.logs,
                                    key = { it.id }
                                ) { log ->
                                    ExecutionLogListItem(log)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDateRangePicker) {
        val dateRangePickerState = rememberDateRangePickerState()
        DatePickerDialog(
            onDismissRequest = { showDateRangePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis?.let { it + 86399999 } // Add 23:59:59.999 to include full day
                        viewModel.setDateRange(startMillis, endMillis)
                        showDateRangePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setDateRange(null, null)
                        showDateRangePicker = false
                    }
                ) {
                    Text("Clear")
                }
            }
        ) {
            DateRangePicker(
                state = dateRangePickerState,
                title = {
                    Text(
                        text = "Select date range",
                        modifier = Modifier.padding(16.dp)
                    )
                },
                headline = {
                    val start = dateRangePickerState.selectedStartDateMillis
                    val end = dateRangePickerState.selectedEndDateMillis
                    if (start != null && end != null) {
                        Text(
                            text = "${formatDateForDisplay(start)} - ${formatDateForDisplay(end)}",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                },
                modifier = Modifier.weight(1f, false)
            )
        }
    }
}

@Composable
private fun ExecutionLogListItem(log: ExecutionLogDTO) {
    var expanded by remember { mutableStateOf(false) }
    val isSuccess = log.executionStatus == "SUCCESS"
    val isSkipped = log.executionStatus == "SKIPPED"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = { expanded = !expanded }
    ) {
        ListItem(
            headlineContent = {
                Text(
                    log.macroName ?: "Macro ID: ${log.macroId}",
                    fontWeight = FontWeight.Bold
                )
            },
            supportingContent = {
                Column {
                    Text(formatDate(log.executedAt), style = MaterialTheme.typography.bodySmall)
                    if (!isSuccess && !isSkipped && log.errorMessage != null) {
                        Text(
                            text = log.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }
                }
            },
            leadingContent = {
                Icon(
                    imageVector = when {
                        isSuccess -> Icons.Default.CheckCircle
                        isSkipped -> Icons.Default.SkipNext
                        else -> Icons.Default.Error
                    },
                    contentDescription = when {
                        isSuccess -> "Execution successful"
                        isSkipped -> "Execution skipped"
                        else -> "Execution failed"
                    },
                    tint = when {
                        isSuccess -> com.aditsyal.autodroid.presentation.theme.Success
                        isSkipped -> MaterialTheme.colorScheme.secondary
                        else -> MaterialTheme.colorScheme.error
                    }
                )
            },
            trailingContent = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (log.executionDurationMs > 0) {
                        Text(
                            text = "${log.executionDurationMs}ms",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) "Collapse" else "Expand",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        )

        if (expanded && log.actions.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text(
                    "Actions Executed:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                log.actions.sortedBy { it.executionOrder }.forEach { action ->
                    ActionItem(action = action)
                }
            }
        }
    }
}

@Composable
private fun ActionItem(action: ActionDTO) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${action.executionOrder + 1}. ${action.actionType}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.weight(1f)
        )
        if (action.delayAfter > 0) {
            Text(
                text = "${action.delayAfter}ms delay",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyHistoryState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "No history found",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun formatDateForDisplay(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
