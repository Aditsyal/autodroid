package com.aditsyal.autodroid.presentation.viewmodels

import android.app.ActivityManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import com.aditsyal.autodroid.domain.usecase.ManageBatteryOptimizationUseCase
import com.aditsyal.autodroid.services.AutomationForegroundService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkPermissionsUseCase: CheckPermissionsUseCase,
    private val manageBatteryOptimizationUseCase: ManageBatteryOptimizationUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
    }

    fun refreshStatus() {
        viewModelScope.launch {
            val isServiceRunning = isServiceRunning(AutomationForegroundService::class.java)
            val accessibilityStatus = checkPermissionsUseCase.checkPermission(CheckPermissionsUseCase.PermissionType.AccessibilityService)
            val batteryOptimizationStatus = manageBatteryOptimizationUseCase.isBatteryOptimizationDisabled()
            val notificationStatus = checkPermissionsUseCase.checkPermission(CheckPermissionsUseCase.PermissionType.PostNotifications)

            _uiState.update {
                it.copy(
                    isServiceRunning = isServiceRunning,
                    isAccessibilityEnabled = accessibilityStatus is CheckPermissionsUseCase.PermissionResult.Granted,
                    isBatteryOptimizationDisabled = batteryOptimizationStatus is ManageBatteryOptimizationUseCase.BatteryOptimizationResult.Disabled,
                    isNotificationPermissionGranted = notificationStatus is CheckPermissionsUseCase.PermissionResult.Granted
                )
            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        @Suppress("DEPRECATION")
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

data class SettingsUiState(
    val isServiceRunning: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val isNotificationPermissionGranted: Boolean = false
)
