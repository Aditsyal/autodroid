package com.aditsyal.autodroid.domain.usecase

import android.app.ActivityManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.BatteryManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.aditsyal.autodroid.data.models.ConstraintDTO
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject

class EvaluateConstraintsUseCase @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    operator fun invoke(constraints: List<ConstraintDTO>): Boolean {
        if (constraints.isEmpty()) return true
        
        Timber.d("Evaluating ${constraints.size} constraints")
        return constraints.all { isConstraintSatisfied(it) }
    }

    private fun isConstraintSatisfied(constraint: ConstraintDTO): Boolean {
        return try {
            when (constraint.constraintType) {
                // Time constraints
                "TIME_RANGE" -> checkTimeRange(constraint.constraintConfig)
                "DAY_OF_WEEK" -> checkDayOfWeek(constraint.constraintConfig)
                "EXCLUDE_WEEKENDS" -> checkExcludeWeekends(constraint.constraintConfig)
                "SPECIFIC_DATE" -> checkSpecificDate(constraint.constraintConfig)
                
                // Device state constraints
                "BATTERY_LEVEL" -> checkBatteryLevel(constraint.constraintConfig)
                "BATTERY_LEVEL_RANGE" -> checkBatteryLevelRange(constraint.constraintConfig)
                "CHARGING_STATUS" -> checkChargingStatus(constraint.constraintConfig)
                "SCREEN_STATE" -> checkScreenState(constraint.constraintConfig)
                "DEVICE_LOCKED" -> checkDeviceLocked(constraint.constraintConfig)
                
                // Connectivity constraints
                "WIFI_CONNECTED" -> checkWifiConnected(constraint.constraintConfig)
                "WIFI_DISCONNECTED" -> checkWifiDisconnected()
                "MOBILE_DATA_ACTIVE" -> checkMobileDataActive()
                "BLUETOOTH_CONNECTED" -> checkBluetoothConnected(constraint.constraintConfig)
                
                // Location constraints
                "INSIDE_GEOFENCE" -> checkInsideGeofence(constraint.constraintConfig)
                "OUTSIDE_GEOFENCE" -> checkOutsideGeofence(constraint.constraintConfig)
                
                // Context constraints
                "APP_RUNNING" -> checkAppRunning(constraint.constraintConfig)
                "HEADPHONES_CONNECTED" -> checkHeadphonesConnected()
                "DO_NOT_DISTURB_ENABLED" -> checkDoNotDisturbEnabled()
                
                // Legacy constraints
                "AIRPLANE_MODE" -> checkAirplaneMode(constraint.constraintConfig)
                
                else -> {
                    Timber.w("Unknown constraint type: ${constraint.constraintType}")
                    true // Default to true for unsupported constraints to avoid unintended blocking
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error evaluating constraint: ${constraint.constraintType}")
            true // Default to true on error to avoid blocking execution
        }
    }

    private fun checkAirplaneMode(config: Map<String, Any>): Boolean {
        val expected = config["enabled"] as? Boolean ?: return true
        val actual = android.provider.Settings.Global.getInt(
            context.contentResolver,
            android.provider.Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
        return actual == expected
    }

    private fun checkBatteryLevel(config: Map<String, Any>): Boolean {
        val filter = android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(android.os.BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level == -1 || scale == -1) return true
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        
        return compareValues(batteryPct, config)
    }

    // Time constraints
    private fun checkTimeRange(config: Map<String, Any>): Boolean {
        val startTime = config["startTime"]?.toString() ?: return true
        val endTime = config["endTime"]?.toString() ?: return true
        
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentTime = currentHour * 60 + currentMinute
        
        val startParts = startTime.split(":")
        val endParts = endTime.split(":")
        if (startParts.size != 2 || endParts.size != 2) return true
        
        val startHour = startParts[0].toIntOrNull() ?: return true
        val startMin = startParts[1].toIntOrNull() ?: return true
        val endHour = endParts[0].toIntOrNull() ?: return true
        val endMin = endParts[1].toIntOrNull() ?: return true
        
        val startTimeMinutes = startHour * 60 + startMin
        val endTimeMinutes = endHour * 60 + endMin
        
        return if (startTimeMinutes <= endTimeMinutes) {
            currentTime in startTimeMinutes..endTimeMinutes
        } else {
            // Handles overnight ranges (e.g., 22:00 - 06:00)
            currentTime >= startTimeMinutes || currentTime <= endTimeMinutes
        }
    }

    private fun checkDayOfWeek(config: Map<String, Any>): Boolean {
        val allowedDays = config["days"] as? List<*> ?: return true
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        
        val dayNumbers = allowedDays.mapNotNull { day ->
            when (day.toString().uppercase()) {
                "MONDAY", "MON" -> Calendar.MONDAY
                "TUESDAY", "TUE" -> Calendar.TUESDAY
                "WEDNESDAY", "WED" -> Calendar.WEDNESDAY
                "THURSDAY", "THU" -> Calendar.THURSDAY
                "FRIDAY", "FRI" -> Calendar.FRIDAY
                "SATURDAY", "SAT" -> Calendar.SATURDAY
                "SUNDAY", "SUN" -> Calendar.SUNDAY
                else -> null
            }
        }
        
        return dayNumbers.contains(currentDay)
    }

    private fun checkExcludeWeekends(config: Map<String, Any>): Boolean {
        val exclude = config["exclude"] as? Boolean ?: true
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val isWeekend = dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        
        return if (exclude) !isWeekend else isWeekend
    }

    private fun checkSpecificDate(config: Map<String, Any>): Boolean {
        val date = config["date"]?.toString()?.toLongOrNull() ?: return true
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        val targetCalendar = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        return calendar.timeInMillis == targetCalendar.timeInMillis
    }

    // Device state constraints
    private fun checkBatteryLevelRange(config: Map<String, Any>): Boolean {
        val minLevel = config["minLevel"]?.toString()?.toIntOrNull() ?: 0
        val maxLevel = config["maxLevel"]?.toString()?.toIntOrNull() ?: 100
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        
        if (level == -1 || scale == -1) return true
        val batteryPct = (level * 100 / scale.toFloat()).toInt()
        
        return batteryPct in minLevel..maxLevel
    }

    private fun checkChargingStatus(config: Map<String, Any>): Boolean {
        val isCharging = config["isCharging"] as? Boolean ?: return true
        
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, filter)
        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        
        val actuallyCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL
        
        return actuallyCharging == isCharging
    }

    private fun checkScreenState(config: Map<String, Any>): Boolean {
        val isOn = config["isOn"] as? Boolean ?: return true
        val powerManager = ContextCompat.getSystemService(context, PowerManager::class.java)
        val actuallyOn = powerManager?.isInteractive ?: false
        return actuallyOn == isOn
    }

    private fun checkDeviceLocked(config: Map<String, Any>): Boolean {
        val isLocked = config["isLocked"] as? Boolean ?: return true
        val keyguardManager = ContextCompat.getSystemService(context, android.app.KeyguardManager::class.java)
        val actuallyLocked = keyguardManager?.isKeyguardLocked ?: false
        return actuallyLocked == isLocked
    }

    // Connectivity constraints
    private fun checkWifiConnected(config: Map<String, Any>): Boolean {
        val requiredSSID = config["ssid"]?.toString()
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return false
        
        @Suppress("DEPRECATION")
        val wifiInfo = wifiManager.connectionInfo
        val isConnected = wifiInfo != null && wifiInfo.networkId != -1
        
        if (!isConnected) return false
        
        if (requiredSSID != null) {
            val currentSSID = wifiInfo.ssid?.replace("\"", "")
            return currentSSID == requiredSSID
        }
        
        return true
    }

    private fun checkWifiDisconnected(): Boolean {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return true
        @Suppress("DEPRECATION")
        val wifiInfo = wifiManager.connectionInfo
        return wifiInfo == null || wifiInfo.networkId == -1
    }

    private fun checkMobileDataActive(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            capabilities?.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ?: false
        } else {
            @Suppress("DEPRECATION")
            val networkInfo = connectivityManager.activeNetworkInfo
            networkInfo?.type == ConnectivityManager.TYPE_MOBILE && networkInfo.isConnected
        }
    }

    private fun checkBluetoothConnected(config: Map<String, Any>): Boolean {
        val requiredDevice = config["deviceAddress"]?.toString()
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = bluetoothManager?.adapter ?: return false
        
        if (!adapter.isEnabled) return false
        
        if (requiredDevice != null) {
            // Check if specific device is connected
            val bondedDevices = adapter.bondedDevices
            return bondedDevices.any { it.address == requiredDevice }
        }
        
        return adapter.isEnabled
    }

    // Location constraints
    private fun checkInsideGeofence(config: Map<String, Any>): Boolean {
        // This would require location tracking - simplified version
        // In a real implementation, you'd check against active geofences
        Timber.w("INSIDE_GEOFENCE constraint not fully implemented - requires location tracking")
        return true
    }

    private fun checkOutsideGeofence(config: Map<String, Any>): Boolean {
        // This would require location tracking - simplified version
        Timber.w("OUTSIDE_GEOFENCE constraint not fully implemented - requires location tracking")
        return true
    }

    // Context constraints
    private fun checkAppRunning(config: Map<String, Any>): Boolean {
        val packageName = config["packageName"]?.toString() ?: return true
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager ?: return true
        
        val runningTasks = activityManager.getRunningTasks(1)
        return runningTasks.isNotEmpty() && runningTasks[0].topActivity?.packageName == packageName
    }

    private fun checkHeadphonesConnected(): Boolean {
        val filter = IntentFilter(Intent.ACTION_HEADSET_PLUG)
        val receiver = context.registerReceiver(null, filter)
        val state = receiver?.getIntExtra("state", -1) ?: -1
        return state == 1 // 1 = plugged in
    }

    private fun checkDoNotDisturbEnabled(): Boolean {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager?.currentInterruptionFilter == android.app.NotificationManager.INTERRUPTION_FILTER_NONE
        } else {
            false
        }
    }

    private fun compareValues(actual: Int, config: Map<String, Any>): Boolean {
        val operator = config["operator"]?.toString() ?: "equals"
        val expected = config["value"]?.toString()?.toIntOrNull() ?: return true
        
        return when (operator) {
            "greater_than" -> actual > expected
            "less_than" -> actual < expected
            "equals" -> actual == expected
            "not_equals" -> actual != expected
            else -> actual == expected
        }
    }
}
