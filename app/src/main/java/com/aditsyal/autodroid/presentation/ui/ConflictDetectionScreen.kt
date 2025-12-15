package com.aditsyal.autodroid.presentation.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.ConflictDTO
import com.aditsyal.autodroid.presentation.viewmodels.ConflictDetectorViewModel

@Composable
fun ConflictDetectionScreen(
    onBackClick: () -> Unit,
    viewModel: ConflictDetectorViewModel = hiltViewModel()
) {
    val conflicts by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Macro Conflict Detection") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (conflicts.isEmpty()) {
                Text(
                    "No duplicate macro names found.",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                ConflictList(conflicts)
            }
        }
    }
}

@Composable
fun ConflictList(conflicts: List<ConflictDTO>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(conflicts) { conflict ->
            ConflictItem(conflict)
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
fun ConflictItem(conflict: ConflictDTO) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Macro Name: ${conflict.macroName}", style = MaterialTheme.typography.titleMedium)
            Text(text = "Duplicate Count: ${conflict.duplicateCount}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
