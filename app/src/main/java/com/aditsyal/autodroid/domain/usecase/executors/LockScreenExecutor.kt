package com.aditsyal.autodroid.domain.usecase.executors

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class LockScreenExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private val devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    private val adminComponent = ComponentName(context, DeviceAdminReceiver::class.java)

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val forceLock = config["forceLock"]?.toString()?.toBoolean() ?: true

            if (forceLock) {
                lockDevice()
            } else {
                // Check if we have admin privileges before attempting to lock
                if (isDeviceAdminActive()) {
                    lockDevice()
                } else {
                    throw SecurityException("Device admin not active. Cannot lock screen without admin privileges.")
                }
            }

            Timber.i("Screen locked successfully")
        }.onFailure { e ->
            Timber.e(e, "Failed to lock screen")
        }
    }

    private fun lockDevice() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Android 9+ has better security, use device policy manager if available
                if (isDeviceAdminActive()) {
                    devicePolicyManager.lockNow()
                } else {
                    throw SecurityException("Device admin required for Android 9+ screen locking")
                }
            } else {
                // For older versions, try device policy manager first
                if (isDeviceAdminActive()) {
                    devicePolicyManager.lockNow()
                } else {
                    // Fallback: try to use keyguard manager (limited functionality)
                    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
                    @Suppress("DEPRECATION")
                    if (keyguardManager.isKeyguardLocked) {
                        // Already locked, nothing to do
                        Timber.d("Screen already locked")
                    } else {
                        throw SecurityException("Cannot lock screen: no device admin and keyguard not locked")
                    }
                }
            }
        } catch (e: SecurityException) {
            throw e
        } catch (e: Exception) {
            // Fallback for some devices that might not support device policy locking
            Timber.w(e, "Device policy lock failed, attempting alternative methods")

            // Try sending a screen off broadcast (won't actually lock, but turns screen off)
            try {
                val intent = android.content.Intent("android.intent.action.SCREEN_OFF")
                context.sendBroadcast(intent)
                Timber.d("Sent screen off broadcast as fallback")
            } catch (e2: Exception) {
                Timber.e(e2, "All screen locking methods failed")
                throw RuntimeException("Unable to lock screen: ${e.message}")
            }
        }
    }

    private fun isDeviceAdminActive(): Boolean {
        return devicePolicyManager.isAdminActive(adminComponent)
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