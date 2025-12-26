package com.aditsyal.autodroid.presentation.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import com.aditsyal.autodroid.domain.usecase.ManageBatteryOptimizationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
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
            val isWorkManagerRunning = isWorkManagerRunning()
            val accessibilityStatus = checkPermissionsUseCase.checkPermission(CheckPermissionsUseCase.PermissionType.AccessibilityService)
            val batteryOptimizationStatus = manageBatteryOptimizationUseCase.isBatteryOptimizationDisabled()
            val notificationStatus = checkPermissionsUseCase.checkPermission(CheckPermissionsUseCase.PermissionType.PostNotifications)

            _uiState.update {
                it.copy(
                    isWorkManagerRunning = isWorkManagerRunning,
                    isAccessibilityEnabled = accessibilityStatus is CheckPermissionsUseCase.PermissionResult.Granted,
                    isBatteryOptimizationDisabled = batteryOptimizationStatus is ManageBatteryOptimizationUseCase.BatteryOptimizationResult.Disabled,
                    isNotificationPermissionGranted = notificationStatus is CheckPermissionsUseCase.PermissionResult.Granted
                )
            }
        }
    }

    private suspend fun isWorkManagerRunning(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val workManager = WorkManager.getInstance(context)
                val workInfos = workManager.getWorkInfosForUniqueWork("MacroTriggerWork").get()
                workInfos.any { workInfo ->
                    workInfo.state == WorkInfo.State.RUNNING ||
                    workInfo.state == WorkInfo.State.ENQUEUED
                }
            } catch (e: Exception) {
                false
            }
        }
    }
}

data class SettingsUiState(
    val isWorkManagerRunning: Boolean = false,
    val isAccessibilityEnabled: Boolean = false,
    val isBatteryOptimizationDisabled: Boolean = false,
    val isNotificationPermissionGranted: Boolean = false
)
