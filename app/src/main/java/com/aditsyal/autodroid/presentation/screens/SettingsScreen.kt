package com.aditsyal.autodroid.presentation.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.remember
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.aditsyal.autodroid.presentation.viewmodels.SettingsUiState
import com.aditsyal.autodroid.presentation.viewmodels.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onNavigateToVariables: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingToggleState by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshStatus()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    SettingsScreenContent(
        uiState = uiState,
        scrollBehavior = scrollBehavior,
        showPermissionDialog = showPermissionDialog,
        onBackClick = onBackClick,
        onNavigateToVariables = onNavigateToVariables,
        onRefreshStatus = { viewModel.refreshStatus() },
        onToggleSidebar = { newState: Boolean ->
            viewModel.toggleSidebar(newState)
        },
        onSetAmoledMode = { enabled: Boolean -> viewModel.setAmoledMode(enabled) },
        onSetHapticFeedback = { enabled: Boolean -> viewModel.setHapticFeedbackEnabled(enabled) },
        onSetDynamicColor = { enabled: Boolean -> viewModel.setDynamicColorEnabled(enabled) },
        onDismissPermissionDialog = { showPermissionDialog = false },
        onGrantPermission = {
            showPermissionDialog = false
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            context.startActivity(intent)
        }
    )
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
                contentDescription = if (isGranted) "Permission granted" else "Permission denied",
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreenContent(
    uiState: SettingsUiState,
    scrollBehavior: TopAppBarScrollBehavior,
    showPermissionDialog: Boolean = false,
    onBackClick: () -> Unit,
    onNavigateToVariables: () -> Unit,
    onRefreshStatus: () -> Unit,
    onToggleSidebar: (Boolean) -> Boolean,
    onSetAmoledMode: (Boolean) -> Unit,
    onSetHapticFeedback: (Boolean) -> Unit,
    onSetDynamicColor: (Boolean) -> Unit,
    onDismissPermissionDialog: () -> Unit,
    onGrantPermission: () -> Unit
) {
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") },
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
                    IconButton(
                        onClick = onRefreshStatus,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            modifier = Modifier.size(24.dp)
                        )
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
            SettingsSectionHeader("Data & Variables")

            ListItem(
                headlineContent = { Text("Global Variables") },
                supportingContent = { Text("Manage global variables used in macros") },
                leadingContent = {
                    Icon(Icons.Default.List, contentDescription = "Variables")
                },
                modifier = Modifier.clickable(
                    onClick = onNavigateToVariables,
                    indication = LocalIndication.current,
                    interactionSource = remember { MutableInteractionSource() }
                )
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("Sidebar Launcher") },
                supportingContent = { Text("Show a floating bubble for quick macro execution") },
                trailingContent = {
                    Switch(
                        checked = uiState.isSidebarEnabled,
                        onCheckedChange = { enabled -> onToggleSidebar(enabled) }
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsSectionHeader("Appearance")

            ListItem(
                headlineContent = { Text("Dynamic Colors") },
                supportingContent = { Text("Use Material You colors from your wallpaper (Android 12+)") },
                trailingContent = {
                    Switch(
                        checked = uiState.isDynamicColorEnabled,
                        onCheckedChange = onSetDynamicColor
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            ListItem(
                headlineContent = { Text("AMOLED Dark Mode") },
                supportingContent = { Text("Pure black background for maximum battery saving on OLED screens") },
                trailingContent = {
                    Switch(
                        checked = uiState.isAmoledMode,
                        onCheckedChange = onSetAmoledMode
                    )
                }
            )

            ListItem(
                headlineContent = { Text("Haptic Feedback") },
                supportingContent = { Text("Vibrate when tapping buttons and executing macros") },
                trailingContent = {
                    Switch(
                        checked = uiState.isHapticFeedbackEnabled,
                        onCheckedChange = onSetHapticFeedback
                    )
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

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
                onClick = {}
            )

            PermissionListItem(
                label = "Battery Optimization",
                description = "Should be disabled for reliable background operation",
                isGranted = uiState.isBatteryOptimizationDisabled,
                onClick = {}
            )

            PermissionListItem(
                label = "Notifications",
                description = "Required for status and foreground service visibility",
                isGranted = uiState.isNotificationPermissionGranted,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(32.dp))
        }

        if (showPermissionDialog) {
            AlertDialog(
                onDismissRequest = onDismissPermissionDialog,
                title = { Text("Grant Overlay Permission") },
                text = { Text("To show the floating sidebar launcher, you need to grant the \"Draw over other apps\" permission.") },
                confirmButton = {
                    Button(onClick = onGrantPermission) {
                        Text("Grant Permission")
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismissPermissionDialog) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
