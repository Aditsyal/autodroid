package com.aditsyal.autodroid.domain.usecase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.wifi.WifiManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aditsyal.autodroid.R
import com.aditsyal.autodroid.data.models.ActionDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Handles the actual execution of individual actions.
 */
class ExecuteActionUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val ACTION_CHANNEL_ID = "action_channel"
        private const val ACTION_CHANNEL_NAME = "Automation Actions"
    }

    init {
        createNotificationChannel()
    }

    suspend operator fun invoke(action: ActionDTO) {
        Timber.d("Executing action: ${action.actionType} with config: ${action.actionConfig}")
        
        when (action.actionType) {
            "WIFI_TOGGLE" -> toggleWifi(action.actionConfig)
            "BLUETOOTH_TOGGLE" -> toggleBluetooth(action.actionConfig)
            "VOLUME_CONTROL" -> controlVolume(action.actionConfig)
            "NOTIFICATION" -> showNotification(action.actionConfig)
            else -> Timber.w("Unknown action type: ${action.actionType}")
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleWifi(config: Map<String, Any>) {
        val enable = config["enabled"] as? Boolean ?: true
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        if (wifiManager == null) {
            Timber.e("WifiManager not available")
            return
        }

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = enable
                Timber.i("WiFi set to: $enable")
            } else {
                Timber.w("Toggling WiFi is restricted on Android Q+")
                // In a real app, we might launch a panel intent here if UI is visible,
                // or just notify the user that we can't do it automatically.
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle WiFi")
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleBluetooth(config: Map<String, Any>) {
        val enable = config["enabled"] as? Boolean ?: true
        
        // Check for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Timber.e("Missing BLUETOOTH_CONNECT permission")
                return
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                Timber.e("Missing BLUETOOTH_ADMIN permission")
                return
            }
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            Timber.e("BluetoothAdapter not available")
            return
        }

        try {
            if (enable) {
                if (!bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.enable()
                    Timber.i("Bluetooth enabled")
                }
            } else {
                if (bluetoothAdapter.isEnabled) {
                    bluetoothAdapter.disable()
                    Timber.i("Bluetooth disabled")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle Bluetooth")
        }
    }

    private fun controlVolume(config: Map<String, Any>) {
        val streamTypeStr = config["stream"]?.toString() ?: "MUSIC"
        val volumeLevel = config["level"]?.toString()?.toIntOrNull() ?: 50 // Percentage
        
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) {
            Timber.e("AudioManager not available")
            return
        }

        val streamType = when (streamTypeStr) {
            "RING" -> AudioManager.STREAM_RING
            "NOTIFICATION" -> AudioManager.STREAM_NOTIFICATION
            "ALARM" -> AudioManager.STREAM_ALARM
            "VOICE_CALL" -> AudioManager.STREAM_VOICE_CALL
            else -> AudioManager.STREAM_MUSIC
        }

        try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val targetVolume = (maxVolume * (volumeLevel / 100f)).toInt()
            
            audioManager.setStreamVolume(streamType, targetVolume, AudioManager.FLAG_SHOW_UI)
            Timber.i("Volume set to $targetVolume (approx $volumeLevel%) for stream $streamTypeStr")
        } catch (e: Exception) {
            Timber.e(e, "Failed to control volume")
        }
    }

    private fun showNotification(config: Map<String, Any>) {
        val title = config["title"]?.toString() ?: "Automation Executed"
        val message = config["message"]?.toString() ?: "Action completed successfully"
        val channelId = config["channelId"]?.toString() ?: ACTION_CHANNEL_ID
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            Timber.w("Missing POST_NOTIFICATIONS permission")
            return
        }

        try {
            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_launcher_foreground) // Ideally use a proper notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build()

            NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notification)
            Timber.i("Notification shown: $title - $message")
        } catch (e: Exception) {
            Timber.e(e, "Failed to show notification")
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                ACTION_CHANNEL_ID,
                ACTION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channels for automation action notifications"
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
