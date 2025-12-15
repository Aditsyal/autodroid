package com.aditsyal.autodroid.domain.usecase

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for managing battery optimization settings
 * Critical for reliable background automation as recommended in the review
 */
class ManageBatteryOptimizationUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)

    sealed class BatteryOptimizationResult {
        data object Disabled : BatteryOptimizationResult() // Optimization is disabled (good for us)
        data object Enabled : BatteryOptimizationResult()  // Optimization is enabled (bad for us)
        data object NotSupported : BatteryOptimizationResult() // API not available
        data object Unknown : BatteryOptimizationResult()
    }

    /**
     * Check if battery optimization is disabled for this app
     * Returns true if optimization is disabled (which is what we want)
     */
    fun isBatteryOptimizationDisabled(): BatteryOptimizationResult {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true) {
                    BatteryOptimizationResult.Disabled
                } else {
                    BatteryOptimizationResult.Enabled
                }
            } catch (e: Exception) {
                BatteryOptimizationResult.Unknown
            }
        } else {
            BatteryOptimizationResult.NotSupported
        }
    }

    /**
     * Request to disable battery optimization for this app
     * This shows a system dialog asking user to disable battery optimization
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun requestDisableBatteryOptimization() {
        try {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = Uri.parse("package:${context.packageName}")
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to battery settings
            openBatterySettings()
        }
    }

    /**
     * Open battery settings where user can manually disable optimization
     */
    private fun openBatterySettings() {
        try {
            val intent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to general settings
            val intent = Intent(Settings.ACTION_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    /**
     * Get user-friendly information about battery optimization
     */
    fun getBatteryOptimizationInfo(): BatteryOptimizationInfo {
        return BatteryOptimizationInfo(
            title = "Battery Optimization",
            description = "Disable battery optimization to ensure automations run reliably in the background.",
            rationale = "Android's battery optimization may prevent automations from running when the screen is off or the app is in the background. Disabling optimization ensures your automations work consistently.",
            isSupported = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
        )
    }
}

data class BatteryOptimizationInfo(
    val title: String,
    val description: String,
    val rationale: String,
    val isSupported: Boolean
)
