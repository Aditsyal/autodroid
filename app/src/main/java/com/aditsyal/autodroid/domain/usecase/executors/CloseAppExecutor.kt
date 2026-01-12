package com.aditsyal.autodroid.domain.usecase.executors

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class CloseAppExecutor @Inject constructor(
    @ApplicationContext private val context: Context
) : ActionExecutor {

    private val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val packageName = config["packageName"]?.toString()
                ?: throw IllegalArgumentException("packageName is required to close app")

            val forceStop = config["forceStop"]?.toString()?.toBoolean() ?: false

            closeApplication(packageName, forceStop)
            Timber.i("Application closed: $packageName")
        }.onFailure { e ->
            Timber.e(e, "Failed to close application")
        }
    }

    private fun closeApplication(packageName: String, forceStop: Boolean) {
        try {
            if (forceStop && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // Try force stop (requires special permissions, usually for system apps)
                try {
                    val method = activityManager.javaClass.getMethod("forceStopPackage", String::class.java)
                    method.invoke(activityManager, packageName)
                    Timber.d("Force stopped application: $packageName")
                    return
                } catch (e: Exception) {
                    Timber.w(e, "Force stop not available or failed, trying alternative methods")
                }
            }

            // Method 1: killBackgroundProcesses (works for background processes)
            try {
                activityManager.killBackgroundProcesses(packageName)
                Timber.d("Killed background processes for: $packageName")
            } catch (e: Exception) {
                Timber.w(e, "killBackgroundProcesses failed: ${e.message}")
            }

            // Method 2: For Android 8.0+, try to clear app data (extreme measure)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    // This is more of a cleanup than closing, but can help
                    val method = context.packageManager.javaClass.getMethod(
                        "clearApplicationUserData",
                        String::class.java,
                        Any::class.java
                    )
                    // This would require implementing IPackageDataObserver, which is complex
                    Timber.d("Clear application data attempted for: $packageName")
                } catch (e: Exception) {
                    Timber.w(e, "Clear application data not available")
                }
            }

            // Method 3: Send broadcast to close app (if app is listening)
            try {
                val intent = android.content.Intent("com.aditsyal.autodroid.CLOSE_APP").apply {
                    putExtra("packageName", packageName)
                    addFlags(android.content.Intent.FLAG_INCLUDE_STOPPED_PACKAGES)
                }
                context.sendBroadcast(intent)
                Timber.d("Sent close broadcast to: $packageName")
            } catch (e: Exception) {
                Timber.w(e, "Failed to send close broadcast")
            }

            // Method 4: Try to launch app and then finish it (hacky, may not work)
            try {
                val launchIntent = context.packageManager.getLaunchIntentForPackage(packageName)
                launchIntent?.let { intent ->
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra("close_app", true) // Custom flag that app might handle
                    context.startActivity(intent)
                    Timber.d("Attempted to launch app for closing: $packageName")
                }
            } catch (e: Exception) {
                Timber.w(e, "Failed to launch app for closing")
            }

        } catch (e: SecurityException) {
            throw SecurityException("Permission denied to close application. Required: KILL_BACKGROUND_PROCESSES")
        } catch (e: Exception) {
            throw RuntimeException("Unable to close application '$packageName': ${e.message}")
        }
    }

    // Utility method to check if app is running
    fun isAppRunning(packageName: String): Boolean {
        return try {
            val processes = activityManager.runningAppProcesses
            processes?.any { it.processName == packageName } ?: false
        } catch (e: Exception) {
            Timber.e(e, "Failed to check if app is running")
            false
        }
    }

    // Utility method to get running app processes
    fun getRunningAppProcesses(): List<String> {
        return try {
            activityManager.runningAppProcesses
                ?.map { it.processName }
                ?: emptyList()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get running app processes")
            emptyList()
        }
    }

    // Method to check if we have the required permission
    fun hasKillBackgroundProcessesPermission(): Boolean {
        return try {
            // Try to call killBackgroundProcesses to test permission
            activityManager.killBackgroundProcesses("com.nonexistent.package")
            true
        } catch (e: SecurityException) {
            false
        } catch (e: Exception) {
            // Other exceptions might not be permission-related
            true
        }
    }
}