package com.aditsyal.autodroid.presentation.viewmodels

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkQuery
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import com.aditsyal.autodroid.domain.usecase.ManageBatteryOptimizationUseCase
import com.aditsyal.autodroid.data.repository.UserPreferencesRepository
import com.aditsyal.autodroid.workers.MacroTriggerWorker
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
    private val manageBatteryOptimizationUseCase: ManageBatteryOptimizationUseCase,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshStatus()
        viewModelScope.launch {
            userPreferencesRepository.amoledMode.collect { isAmoled ->
                _uiState.update { it.copy(isAmoledMode = isAmoled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.hapticFeedbackEnabled.collect { isHapticEnabled ->
                _uiState.update { it.copy(isHapticFeedbackEnabled = isHapticEnabled) }
            }
        }
        viewModelScope.launch {
            userPreferencesRepository.sidebarEnabled.collect { isSidebarEnabled ->
                _uiState.update { it.copy(isSidebarEnabled = isSidebarEnabled) }
            }
        }
    }

    fun toggleSidebar(enabled: Boolean): Boolean {
        if (enabled) {
            val permissionResult = checkPermissionsUseCase.checkPermission(CheckPermissionsUseCase.PermissionType.SystemOverlay)
            if (permissionResult is CheckPermissionsUseCase.PermissionResult.Granted) {
                userPreferencesRepository.setSidebarEnabled(enabled)
                context.startService(Intent(context, com.aditsyal.autodroid.services.overlay.SidebarService::class.java))
                return true
            }
            return false
        } else {
            userPreferencesRepository.setSidebarEnabled(enabled)
            context.stopService(Intent(context, com.aditsyal.autodroid.services.overlay.SidebarService::class.java))
            return true
        }
    }

    fun setAmoledMode(enabled: Boolean) {
        userPreferencesRepository.setAmoledMode(enabled)
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        userPreferencesRepository.setHapticFeedbackEnabled(enabled)
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
                val workInfos = workManager.getWorkInfosForUniqueWork(MacroTriggerWorker.WORK_NAME).get()
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
    val isNotificationPermissionGranted: Boolean = false,
    val isAmoledMode: Boolean = false,
    val isHapticFeedbackEnabled: Boolean = true,
    val isSidebarEnabled: Boolean = false
)
