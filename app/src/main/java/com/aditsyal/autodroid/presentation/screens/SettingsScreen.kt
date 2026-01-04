package com.aditsyal.autodroid.presentation.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshStatus() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSectionHeader("Service Status")

            StatusListItem(
                label = "Background Monitoring",
                description = "Periodic trigger checking via WorkManager",
                isActive = uiState.isWorkManagerRunning
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader("Permissions")

            PermissionListItem(
                label = "Accessibility Service",
                description = "Required for UI automation and app event detection",
                isGranted = uiState.isAccessibilityEnabled,
                onClick = {
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    context.startActivity(intent)
                }
            )

            PermissionListItem(
                label = "Battery Optimization",
                description = "Should be disabled for reliable background operation",
                isGranted = uiState.isBatteryOptimizationDisabled,
                onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        context.startActivity(intent)
                    }
                }
            )

            PermissionListItem(
                label = "Notifications",
                description = "Required for status and foreground service visibility",
                isGranted = uiState.isNotificationPermissionGranted,
                onClick = {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun StatusListItem(
    label: String,
    description: String,
    isActive: Boolean
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(description) },
        trailingContent = {
            Surface(
                color = if (isActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.extraSmall
            ) {
                Text(
                    text = if (isActive) "Active" else "Inactive",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    )
}

@Composable
private fun PermissionListItem(
    label: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(label) },
        supportingContent = { Text(description) },
        leadingContent = {
            Icon(
                imageVector = if (isGranted) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (isGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        },
        trailingContent = {
            TextButton(onClick = onClick) {
                Text(if (isGranted) "Modify" else "Enable")
            }
        },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
