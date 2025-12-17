package com.aditsyal.autodroid.utils

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

sealed class PermissionResult {
    object Granted : PermissionResult()
    data class NeedRationale(val permission: String) : PermissionResult()
    data class Denied(val permission: String) : PermissionResult()
}

@Singleton
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkPermissionsUseCase: CheckPermissionsUseCase
) {
    fun checkPermission(permission: CheckPermissionsUseCase.PermissionType): PermissionResult {
        val result = checkPermissionsUseCase.checkPermission(permission)
        return when (result) {
            is CheckPermissionsUseCase.PermissionResult.Granted -> PermissionResult.Granted
            else -> {
                // Simplified for MVP, in a real app we'd check shouldShowRequestPermissionRationale here if it was an Activity context
                PermissionResult.Denied(permission.manifestPermission)
            }
        }
    }

    fun requestPermission(permission: String): Flow<PermissionResult> = flow {
        val result = ContextCompat.checkSelfPermission(context, permission)
        if (result == PackageManager.PERMISSION_GRANTED) {
            emit(PermissionResult.Granted)
        } else {
            // In a real app with Activity context, we'd check rationale here.
            // Since this is a manager, we emit Denied/Rationale based on state.
            emit(PermissionResult.Denied(permission))
        }
    }
}
