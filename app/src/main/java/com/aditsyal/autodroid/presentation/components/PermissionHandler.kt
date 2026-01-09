package com.aditsyal.autodroid.presentation.components

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.aditsyal.autodroid.presentation.viewmodels.PermissionHandlerViewModel
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import com.aditsyal.autodroid.domain.usecase.ManageBatteryOptimizationUseCase
import com.aditsyal.autodroid.domain.usecase.PermissionDisplayInfo
import timber.log.Timber

/**
 * Composable for handling runtime permissions with rationale dialogs
 * Follows the review's recommendations for proper permission management
 */
@Composable
fun PermissionHandler(
    permission: CheckPermissionsUseCase.PermissionType,
    checkPermissionsUseCase: CheckPermissionsUseCase,
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {},
    viewModel: PermissionHandlerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showRationaleDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            Timber.d("Permissions granted for ${permission::class.simpleName}")
            onPermissionGranted()
        } else {
            Timber.w("Permissions denied for ${permission::class.simpleName}")
            showSettingsDialog = true
            onPermissionDenied()
        }
    }

    val permissionInfo = checkPermissionsUseCase.getPermissionDisplayInfo(permission)

    // Handle accessibility service separately
    if (permission is CheckPermissionsUseCase.PermissionType.AccessibilityService) {
        HandleAccessibilityPermission(
            permissionInfo = permissionInfo,
            onPermissionGranted = onPermissionGranted,
            onPermissionDenied = onPermissionDenied
        )
        return
    }

    // Check permission status and show dialogs as needed
    when (checkPermissionsUseCase.checkPermission(permission)) {
        CheckPermissionsUseCase.PermissionResult.Granted -> {
            onPermissionGranted()
        }
        CheckPermissionsUseCase.PermissionResult.Denied -> {
            if (permission.manifestPermission.isNotEmpty()) {
                showRationaleDialog = true
            }
        }
        CheckPermissionsUseCase.PermissionResult.NeedsRationale -> {
            showRationaleDialog = true
        }
        CheckPermissionsUseCase.PermissionResult.NotRequested -> {
            // First time requesting
            showRationaleDialog = true
        }
    }

    // Rationale dialog
    if (showRationaleDialog) {
        PermissionRationaleDialog(
            permissionInfo = permissionInfo,
            onGrantClick = {
                showRationaleDialog = false
                if (permission.manifestPermission.isNotEmpty()) {
                    permissionLauncher.launch(arrayOf(permission.manifestPermission))
                }
            },
            onDenyClick = {
                showRationaleDialog = false
                onPermissionDenied()
            }
        )
    }

    // Settings dialog for when user denies permission
    if (showSettingsDialog) {
        PermissionSettingsDialog(
            permissionInfo = permissionInfo,
            onOpenSettingsClick = {
                showSettingsDialog = false
                openAppSettings(context)
            },
            onCancelClick = {
                showSettingsDialog = false
            }
        )
    }
}

@Composable
private fun HandleAccessibilityPermission(
    permissionInfo: PermissionDisplayInfo,
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onPermissionDenied()
            },
            title = { Text("Enable ${permissionInfo.title}") },
            text = {
                Text("${permissionInfo.description}\n\n${permissionInfo.rationale}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        openAccessibilitySettings(context)
                        // Note: We can't know immediately if user granted permission
                        // The caller should re-check permission status
                    }
                ) {
                    Text("Open Settings")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onPermissionDenied()
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun PermissionRationaleDialog(
    permissionInfo: PermissionDisplayInfo,
    onGrantClick: () -> Unit,
    onDenyClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDenyClick,
        title = { Text("Permission Required") },
        text = {
            Text("${permissionInfo.title}\n\n${permissionInfo.rationale}")
        },
        confirmButton = {
            TextButton(onClick = onGrantClick) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDenyClick) {
                Text("Deny")
            }
        }
    )
}

@Composable
private fun PermissionSettingsDialog(
    permissionInfo: PermissionDisplayInfo,
    onOpenSettingsClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelClick,
        title = { Text("Permission Required") },
        text = {
            Text("${permissionInfo.title} was denied. Please enable it in app settings to use this feature.")
        },
        confirmButton = {
            TextButton(onClick = onOpenSettingsClick) {
                Text("Open Settings")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelClick) {
                Text("Cancel")
            }
        }
    )
}

private fun openAppSettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.parse("package:${context.packageName}")
    }
    context.startActivity(intent)
}

private fun openAccessibilitySettings(context: android.content.Context) {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    context.startActivity(intent)
}

/**
 * Composable for handling battery optimization settings
 * Critical for reliable background automation
 */
@Composable
fun BatteryOptimizationHandler(
    manageBatteryOptimizationUseCase: ManageBatteryOptimizationUseCase,
    onOptimizationDisabled: () -> Unit = {},
    onOptimizationEnabled: () -> Unit = {}
) {
    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }

    val optimizationInfo = manageBatteryOptimizationUseCase.getBatteryOptimizationInfo()

    // Check optimization status
    when (manageBatteryOptimizationUseCase.isBatteryOptimizationDisabled()) {
        ManageBatteryOptimizationUseCase.BatteryOptimizationResult.Disabled -> {
            onOptimizationDisabled()
        }
        ManageBatteryOptimizationUseCase.BatteryOptimizationResult.Enabled -> {
            showDialog = true
            onOptimizationEnabled()
        }
        ManageBatteryOptimizationUseCase.BatteryOptimizationResult.NotSupported -> {
            // Not supported on this Android version, continue normally
            onOptimizationDisabled()
        }
        ManageBatteryOptimizationUseCase.BatteryOptimizationResult.Unknown -> {
            // Unknown state, show dialog to be safe
            showDialog = true
        }
    }

    // Battery optimization dialog
    if (showDialog && optimizationInfo.isSupported) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Disable ${optimizationInfo.title}") },
            text = {
                Text("${optimizationInfo.description}\n\n${optimizationInfo.rationale}")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        manageBatteryOptimizationUseCase.requestDisableBatteryOptimization()
                    }
                ) {
                    Text("Disable Optimization")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDialog = false }
                ) {
                    Text("Later")
                }
            }
        )
    }
}
