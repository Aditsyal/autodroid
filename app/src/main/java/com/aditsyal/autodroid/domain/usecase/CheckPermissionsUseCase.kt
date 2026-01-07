package com.aditsyal.autodroid.domain.usecase

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.aditsyal.autodroid.services.accessibility.AutomationAccessibilityService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for checking and managing app permissions
 * Follows the review's recommendations for runtime permission handling
 */
class CheckPermissionsUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed class PermissionResult {
        data object Granted : PermissionResult()
        data object Denied : PermissionResult()
        data object NeedsRationale : PermissionResult()
        data object NotRequested : PermissionResult()
    }

    sealed class PermissionType(val manifestPermission: String) {
        data object AccessibilityService : PermissionType("")
        data object FineLocation : PermissionType(Manifest.permission.ACCESS_FINE_LOCATION)
        data object CoarseLocation : PermissionType(Manifest.permission.ACCESS_COARSE_LOCATION)
        data object BackgroundLocation : PermissionType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        } else "")
        data object ForegroundService : PermissionType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Manifest.permission.FOREGROUND_SERVICE
        } else "")
        data object ForegroundServiceLocation : PermissionType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Manifest.permission.FOREGROUND_SERVICE_LOCATION
        } else "")
        data object ScheduleExactAlarm : PermissionType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Manifest.permission.SCHEDULE_EXACT_ALARM
        } else "")
        data object PostNotifications : PermissionType(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.POST_NOTIFICATIONS
        } else "")
        data object SendSms : PermissionType(Manifest.permission.SEND_SMS)
        data object ReadPhoneState : PermissionType(Manifest.permission.READ_PHONE_STATE)
    }

    /**
     * Check if a standard Android permission is granted
     */
    fun checkPermission(permission: PermissionType): PermissionResult {
        return when (permission) {
            is PermissionType.AccessibilityService -> checkAccessibilityService()
            else -> {
                if (permission.manifestPermission.isEmpty()) {
                    // Permission not applicable for this Android version
                    PermissionResult.Granted
                } else {
                    when (ContextCompat.checkSelfPermission(context, permission.manifestPermission)) {
                        PackageManager.PERMISSION_GRANTED -> PermissionResult.Granted
                        else -> PermissionResult.Denied
                    }
                }
            }
        }
    }

    /**
     * Check if accessibility service is enabled
     */
    private fun checkAccessibilityService(): PermissionResult {
        val accessibilityEnabled = try {
            Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED
            )
        } catch (e: Exception) {
            0
        }

        if (accessibilityEnabled != 1) {
            return PermissionResult.Denied
        }

        val expectedComponentName = ComponentName(context, AutomationAccessibilityService::class.java)
        val enabledServicesSetting = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return PermissionResult.Denied

        val colonSplitter = TextUtils.SimpleStringSplitter(':')
        colonSplitter.setString(enabledServicesSetting)

        while (colonSplitter.hasNext()) {
            val componentNameString = colonSplitter.next()
            val enabledService = ComponentName.unflattenFromString(componentNameString)
            if (enabledService != null && enabledService == expectedComponentName) {
                return PermissionResult.Granted
            }
        }

        return PermissionResult.Denied
    }

    /**
     * Get user-friendly permission names and descriptions
     */
    fun getPermissionDisplayInfo(permission: PermissionType): PermissionDisplayInfo {
        return when (permission) {
            PermissionType.AccessibilityService -> PermissionDisplayInfo(
                title = "Accessibility Service",
                description = "Required for UI automation features. Allows the app to interact with other apps and perform automated actions.",
                rationale = "This permission enables powerful automation features like automatically clicking buttons or filling forms in other apps."
            )
            PermissionType.FineLocation -> PermissionDisplayInfo(
                title = "Precise Location",
                description = "Required for location-based triggers that use GPS for precise positioning.",
                rationale = "Location triggers help you automate actions based on where you are, such as turning on WiFi when you arrive home."
            )
            PermissionType.BackgroundLocation -> PermissionDisplayInfo(
                title = "Background Location",
                description = "Allows location monitoring even when the app is not in use.",
                rationale = "Background location access ensures location-based automations work reliably even when the app is closed."
            )
            PermissionType.SendSms -> PermissionDisplayInfo(
                title = "Send SMS",
                description = "Required for automations that send text messages.",
                rationale = "Some automation rules may need to send SMS messages as part of their actions."
            )
            PermissionType.PostNotifications -> PermissionDisplayInfo(
                title = "Post Notifications",
                description = "Required to show automation status and results.",
                rationale = "Notifications help you know when automations run and whether they succeed or fail."
            )
            else -> PermissionDisplayInfo(
                title = permission.manifestPermission,
                description = "Required for automation features",
                rationale = "This permission is needed for the app to function properly."
            )
        }
    }
}

data class PermissionDisplayInfo(
    val title: String,
    val description: String,
    val rationale: String
)
