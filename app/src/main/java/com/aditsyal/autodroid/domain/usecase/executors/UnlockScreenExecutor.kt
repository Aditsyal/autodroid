package com.aditsyal.autodroid.domain.usecase.executors

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class UnlockScreenExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val useBiometricPrompt = config["useBiometricPrompt"]?.toString()?.toBoolean() ?: false
            val dismissKeyguard = config["dismissKeyguard"]?.toString()?.toBoolean() ?: true

            unlockScreen(useBiometricPrompt, dismissKeyguard)
            Timber.i("Screen unlock executed successfully")
        }.onFailure { e ->
            Timber.e(e, "Screen unlock failed")
        }
    }

    private fun unlockScreen(useBiometricPrompt: Boolean, dismissKeyguard: Boolean) {
        try {
            if (dismissKeyguard) {
                // Try to dismiss keyguard (unlock screen)
                if (isDeviceAdminActive()) {
                    // Use device admin to unlock if available
                    // Note: Device admin doesn't have direct unlock method, but we can try to reset password
                    Timber.d("Device admin active, attempting keyguard dismissal")

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
                        // For older versions, try to disable keyguard temporarily
                        @Suppress("DEPRECATION")
                        val keyguardLock = keyguardManager.newKeyguardLock("AutoDroidUnlock")
                        keyguardLock.disableKeyguard()
                        Timber.d("Keyguard disabled for older Android version")
                    } else {
                        // For newer versions, we cannot dismiss keyguard from service context
                        Timber.w("Cannot dismiss keyguard from application context on Android 5.1+ - requires Activity")
                        throw UnsupportedOperationException("Keyguard dismissal requires Activity context on Android 5.1+")
                    }
                } else {
                    // No device admin, try alternative methods
                    Timber.w("No device admin privileges available for screen unlock")
                    throw SecurityException("Device admin privileges required to unlock screen")
                }
            }

            if (useBiometricPrompt) {
                // This would require additional biometric authentication
                // For now, we'll just log that biometric unlock was requested
                Timber.d("Biometric unlock requested - not implemented in this version")
                // TODO: Implement biometric authentication flow if needed
            }

        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            throw RuntimeException("Unable to unlock screen: ${e.message}")
        }
    }

    private fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
    }

    // Utility method to check if screen is currently locked
    fun isScreenLocked(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            keyguardManager.isDeviceLocked
        } else {
            @Suppress("DEPRECATION")
            keyguardManager.isKeyguardLocked
        }
    }

    // Method to check if keyguard dismissal is available
    fun canDismissKeyguard(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1
    }

    // DeviceAdminReceiver class (must be registered in AndroidManifest.xml)
    class DeviceAdminReceiver : android.app.admin.DeviceAdminReceiver() {
        override fun onDisabled(context: Context, intent: android.content.Intent) {
            super.onDisabled(context, intent)
            Timber.w("Device admin disabled")
        }

        override fun onEnabled(context: Context, intent: android.content.Intent) {
            super.onEnabled(context, intent)
            Timber.i("Device admin enabled")
        }
    }

    companion object {
        // Permission required for device admin
        const val DEVICE_ADMIN_PERMISSION = "android.permission.BIND_DEVICE_ADMIN"

        // Intent action for requesting device admin
        const val ACTION_ADD_DEVICE_ADMIN = "android.app.action.ADD_DEVICE_ADMIN"
    }
}