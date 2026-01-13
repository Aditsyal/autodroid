package com.aditsyal.autodroid.presentation.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.data.models.TemplateDTO
import com.aditsyal.autodroid.data.models.toDTO
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import com.aditsyal.autodroid.presentation.viewmodels.TemplateLibraryUiState
import com.aditsyal.autodroid.presentation.viewmodels.TemplateLibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TemplateLibraryScreen(
    onBackClick: () -> Unit,
    onTemplateSelected: (Long) -> Unit,
    viewModel: TemplateLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var sortBy by remember { mutableStateOf("Popularity") }
    var showPreviewDialog by remember { mutableStateOf<TemplateDTO?>(null) }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Search and Filter Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Search bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search templates...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sort options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortBy == "Popularity",
                            onClick = { sortBy = "Popularity" },
                            label = { Text("Popular") }
                        )
                        FilterChip(
                            selected = sortBy == "Name",
                            onClick = { sortBy = "Name" },
                            label = { Text("Name") }
                        )
                        FilterChip(
                            selected = sortBy == "Recent",
                            onClick = { sortBy = "Recent" },
                            label = { Text("Recent") }
                        )
                    }
                }
            }

            // Template List
            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is com.aditsyal.autodroid.presentation.viewmodels.TemplateLibraryUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is com.aditsyal.autodroid.presentation.viewmodels.TemplateLibraryUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is com.aditsyal.autodroid.presentation.viewmodels.TemplateLibraryUiState.Success -> {
                        val filteredAndSortedTemplates: List<TemplateDTO> = remember(state.templates, searchQuery, selectedCategory, sortBy) {
                            state.templates
                                .map<TemplateEntity, TemplateDTO> { it.toDTO() } // Convert TemplateEntity to TemplateDTO
                                .filter { template: TemplateDTO ->
                                    // Search filter
                                    val matchesSearch = searchQuery.isBlank() ||
                                        template.name.contains(searchQuery, ignoreCase = true) ||
                                        template.description.contains(searchQuery, ignoreCase = true) ||
                                        template.category.contains(searchQuery, ignoreCase = true)

                                    // Category filter (if implemented)
                                    val matchesCategory = selectedCategory == null || template.category == selectedCategory

                                    matchesSearch && matchesCategory
                                }
                                .sortedWith(
                                    when (sortBy) {
                                        "Popularity" -> compareByDescending<TemplateDTO> { it.popularityScore }
                                        "Name" -> compareBy<TemplateDTO> { it.name }
                                        "Recent" -> compareByDescending<TemplateDTO> { it.createdAt }
                                        else -> compareByDescending<TemplateDTO> { it.popularityScore }
                                    }
                                )
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredAndSortedTemplates,
                                key = { it.id }
                            ) { template ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { showPreviewDialog = template },
                                    shape = MaterialTheme.shapes.medium,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = template.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = template.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${template.category} • ${template.popularityScore} uses",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Preview Dialog
            showPreviewDialog?.let { template ->
                TemplatePreviewDialog(
                    template = template,
                    onDismiss = { showPreviewDialog = null },
                    onImport = {
                        onTemplateSelected(template.id)
                        showPreviewDialog = null
                    }
                )
            }
        }
    }
}

@Composable
private fun TemplatePreviewDialog(
    template: TemplateDTO,
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(template.name) },
        text = {
            Column {
                Text(template.description)
                Text("Category: ${template.category}")
                Text("Actions: ${template.macro.actions.size}")
            }
        },
        confirmButton = {
            Button(onClick = onImport) {
                Text("Import")
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
fun TemplateLibraryScreenContent(
    uiState: TemplateLibraryUiState,
    onBackClick: () -> Unit,
    onTemplateSelected: (Long) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortByChange: (String) -> Unit,
    onShowPreview: (TemplateEntity?) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var searchQuery by remember { mutableStateOf("") }
    var sortBy by remember { mutableStateOf("Popularity") }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Templates") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = {
                            searchQuery = it
                            onSearchQueryChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search templates...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortBy == "Popularity",
                            onClick = {
                                sortBy = "Popularity"
                                onSortByChange("Popularity")
                            },
                            label = { Text("Popular") }
                        )
                        FilterChip(
                            selected = sortBy == "Name",
                            onClick = {
                                sortBy = "Name"
                                onSortByChange("Name")
                            },
                            label = { Text("Name") }
                        )
                        FilterChip(
                            selected = sortBy == "Recent",
                            onClick = {
                                sortBy = "Recent"
                                onSortByChange("Recent")
                            },
                            label = { Text("Recent") }
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val state = uiState) {
                    is TemplateLibraryUiState.Loading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                    is TemplateLibraryUiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.message, color = MaterialTheme.colorScheme.error)
                        }
                    }
                    is TemplateLibraryUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.templates,
                                key = { it.id }
                            ) { template ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onShowPreview(template) },
                                    shape = MaterialTheme.shapes.medium,
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text(
                                            text = template.name,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = template.description,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = "${template.category} • ${template.usageCount} uses",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

