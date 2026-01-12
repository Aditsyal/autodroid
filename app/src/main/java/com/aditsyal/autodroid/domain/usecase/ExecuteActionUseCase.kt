package com.aditsyal.autodroid.domain.usecase

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.net.wifi.WifiManager
import android.os.Build
import android.app.KeyguardManager
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import android.speech.tts.TextToSpeech
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.aditsyal.autodroid.R
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.domain.usecase.executors.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

/**
 * Handles the actual execution of individual actions.
 */
class ExecuteActionUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getVariableUseCase: GetVariableUseCase,
    private val setVariableUseCase: SetVariableUseCase,
    private val evaluateVariableUseCase: EvaluateVariableUseCase,
    private val wifiToggleExecutor: WifiToggleExecutor,
    private val bluetoothToggleExecutor: BluetoothToggleExecutor,
    private val volumeControlExecutor: VolumeControlExecutor,
    private val notificationExecutor: NotificationExecutor,
    private val sendSmsExecutor: SendSmsExecutor,
    private val launchAppExecutor: LaunchAppExecutor,
    private val openUrlExecutor: OpenUrlExecutor,
    private val setBrightnessExecutor: SetBrightnessExecutor,
    private val toggleAirplaneModeExecutor: com.aditsyal.autodroid.domain.usecase.executors.ToggleAirplaneModeExecutor,
    private val tetheringExecutor: com.aditsyal.autodroid.domain.usecase.executors.TetheringExecutor,
    private val delayExecutor: DelayExecutor,
    private val toastExecutor: com.aditsyal.autodroid.domain.usecase.executors.ToastExecutor,
    private val vibrateExecutor: com.aditsyal.autodroid.domain.usecase.executors.VibrateExecutor,
    private val playSoundExecutor: com.aditsyal.autodroid.domain.usecase.executors.PlaySoundExecutor,
    private val stopSoundExecutor: com.aditsyal.autodroid.domain.usecase.executors.StopSoundExecutor,
    // New Phase 1 executors
    private val httpRequestExecutor: HttpRequestExecutor,
    private val lockScreenExecutor: LockScreenExecutor,
    private val ttsExecutor: TtsExecutor,
    private val screenTimeoutExecutor: ScreenTimeoutExecutor,
    private val dndExecutor: DndExecutor,
    private val mediaControlExecutor: MediaControlExecutor,
    private val closeAppExecutor: CloseAppExecutor,
    private val clearCacheExecutor: ClearCacheExecutor,
    private val unlockScreenExecutor: UnlockScreenExecutor,
    private val startMusicExecutor: StartMusicExecutor,
    private val closeNotificationExecutor: CloseNotificationExecutor,
    private val setRingtoneExecutor: SetRingtoneExecutor,
    private val deleteSmsExecutor: DeleteSmsExecutor,
    private val makeCallExecutor: MakeCallExecutor
) {
    
    companion object {
        private const val ACTION_CHANNEL_ID = "action_channel"
        private const val ACTION_CHANNEL_NAME = "Automation Actions"
    }

    init {
        createNotificationChannel()
    }

    suspend operator fun invoke(action: ActionDTO, macroId: Long? = null): Result<Unit> {
        Timber.d("Executing action: ${action.actionType} with config: ${action.actionConfig}")

        return runCatching {
            // Replace variable placeholders in action config
            val processedConfig = replaceVariablePlaceholders(action.actionConfig, macroId)

            when (action.actionType) {
                // Refactored actions using executors
                "WIFI_TOGGLE" -> wifiToggleExecutor.execute(processedConfig).getOrThrow()
                "BLUETOOTH_TOGGLE" -> bluetoothToggleExecutor.execute(processedConfig).getOrThrow()
                "VOLUME_CONTROL" -> volumeControlExecutor.execute(processedConfig).getOrThrow()
                "NOTIFICATION" -> notificationExecutor.execute(processedConfig).getOrThrow()
                "SEND_SMS" -> sendSmsExecutor.execute(processedConfig).getOrThrow()
                "LAUNCH_APP" -> launchAppExecutor.execute(processedConfig).getOrThrow()
                "OPEN_URL" -> openUrlExecutor.execute(processedConfig).getOrThrow()
                "SET_BRIGHTNESS" -> setBrightnessExecutor.execute(processedConfig).getOrThrow()
                "TOGGLE_AIRPLANE_MODE" -> toggleAirplaneModeExecutor.execute(processedConfig).getOrThrow()
                "TETHERING" -> tetheringExecutor.execute(processedConfig).getOrThrow()
                "DELAY" -> delayExecutor.execute(processedConfig).getOrThrow()
                "SHOW_TOAST" -> toastExecutor.execute(processedConfig).getOrThrow()
                "VIBRATE" -> vibrateExecutor.execute(processedConfig).getOrThrow()

                // New Phase 1 actions
                "HTTP_REQUEST" -> httpRequestExecutor.execute(processedConfig).getOrThrow()
                "LOCK_SCREEN" -> lockScreenExecutor.execute(processedConfig).getOrThrow()
                "TTS" -> ttsExecutor.execute(processedConfig).getOrThrow()
                "SCREEN_TIMEOUT" -> screenTimeoutExecutor.execute(processedConfig).getOrThrow()
                "DND" -> dndExecutor.execute(processedConfig).getOrThrow()
                "MEDIA_CONTROL" -> mediaControlExecutor.execute(processedConfig).getOrThrow()
                "CLOSE_APP" -> closeAppExecutor.execute(processedConfig).getOrThrow()
                "CLEAR_CACHE" -> clearCacheExecutor.execute(processedConfig).getOrThrow()
                "UNLOCK_SCREEN" -> unlockScreenExecutor.execute(processedConfig).getOrThrow()
                "START_MUSIC" -> startMusicExecutor.execute(processedConfig).getOrThrow()
                "CLOSE_NOTIFICATION" -> closeNotificationExecutor.execute(processedConfig).getOrThrow()
                "SET_RINGTONE" -> setRingtoneExecutor.execute(processedConfig).getOrThrow()
                "DELETE_SMS" -> deleteSmsExecutor.execute(processedConfig).getOrThrow()
                "MAKE_CALL" -> makeCallExecutor.execute(processedConfig).getOrThrow()

                // Existing actions (to be refactored later)
                "LOG_HISTORY" -> logToHistory(processedConfig)

                // System settings
                "TOGGLE_GPS" -> toggleGPS(processedConfig)
                "SET_SCREEN_TIMEOUT" -> setScreenTimeout(processedConfig)

                // Device control
                "LOCK_SCREEN" -> lockScreen()
                "SLEEP_DEVICE" -> sleepDevice()
                "ENABLE_DO_NOT_DISTURB" -> setDoNotDisturb(processedConfig, true)
                "DISABLE_DO_NOT_DISTURB" -> setDoNotDisturb(processedConfig, false)

                // Communication
                "SEND_EMAIL" -> sendEmail(processedConfig)
                "MAKE_CALL" -> makeCall(processedConfig)
                "SPEAK_TEXT" -> speakText(processedConfig)

                // App control
                "CLOSE_APP" -> closeApp(processedConfig)
                
                // Media
                "PLAY_SOUND" -> playSoundExecutor.execute(processedConfig).getOrThrow()
                "STOP_SOUND" -> stopSoundExecutor.execute(processedConfig).getOrThrow()
                "CHANGE_WALLPAPER" -> changeWallpaper(processedConfig)
                
                // Automation
                "DELAY" -> delayAction(processedConfig)
                "HTTP_REQUEST" -> sendHttpRequest(processedConfig)
                "SET_VARIABLE" -> setVariable(processedConfig, macroId)
                
                // Logic control (handled in ExecuteMacroUseCase, but included here for completeness)
                "IF_CONDITION", "WHILE_LOOP", "FOR_LOOP", "BREAK", "CONTINUE", "END_IF", "END_WHILE", "END_FOR" -> {
                    // These are handled by ExecuteMacroUseCase, no action needed here
                    Timber.d("Logic control action: ${action.actionType} - handled by macro executor")
                }
                
                else -> {
                    val errorMsg = "Unknown action type: ${action.actionType}"
                    Timber.w(errorMsg)
                    throw IllegalArgumentException(errorMsg)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleWifi(config: Map<String, Any>) {
        val enable = config["enabled"] as? Boolean ?: true
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager

        if (wifiManager == null) {
            val errorMsg = "WifiManager not available"
            Timber.e(errorMsg)
            throw IllegalStateException(errorMsg)
        }

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                wifiManager.isWifiEnabled = enable
                Timber.i("WiFi set to: $enable")
            } else {
                val errorMsg = "Toggling WiFi is restricted on Android Q+. Please use system settings."
                Timber.w(errorMsg)
                throw UnsupportedOperationException(errorMsg)
            }
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied: Failed to toggle WiFi")
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle WiFi")
            throw e
        }
    }

    @Suppress("DEPRECATION")
    private fun toggleBluetooth(config: Map<String, Any>) {
        val enable = config["enabled"] as? Boolean ?: true
        
        // Check for permissions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                val errorMsg = "Missing BLUETOOTH_CONNECT permission"
                Timber.e(errorMsg)
                throw SecurityException(errorMsg)
            }
        } else {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                val errorMsg = "Missing BLUETOOTH_ADMIN permission"
                Timber.e(errorMsg)
                throw SecurityException(errorMsg)
            }
        }

        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
        val bluetoothAdapter = bluetoothManager?.adapter ?: BluetoothAdapter.getDefaultAdapter()

        if (bluetoothAdapter == null) {
            val errorMsg = "BluetoothAdapter not available on this device"
            Timber.e(errorMsg)
            throw IllegalStateException(errorMsg)
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
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied: Failed to toggle Bluetooth")
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle Bluetooth")
            throw e
        }
    }

    private fun controlVolume(config: Map<String, Any>) {
        val streamTypeStr = config["stream"]?.toString() ?: "MUSIC"
        val volumeLevel = config["level"]?.toString()?.toIntOrNull() ?: 50 // Percentage
        
        if (volumeLevel !in 0..100) {
            val errorMsg = "Invalid volume level: $volumeLevel (must be 0-100)"
            Timber.e(errorMsg)
            throw IllegalArgumentException(errorMsg)
        }
        
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audioManager == null) {
            val errorMsg = "AudioManager not available"
            Timber.e(errorMsg)
            throw IllegalStateException(errorMsg)
        }

        val streamType = when (streamTypeStr.uppercase()) {
            "RING" -> AudioManager.STREAM_RING
            "NOTIFICATION" -> AudioManager.STREAM_NOTIFICATION
            "ALARM" -> AudioManager.STREAM_ALARM
            "VOICE_CALL" -> AudioManager.STREAM_VOICE_CALL
            "MUSIC" -> AudioManager.STREAM_MUSIC
            else -> {
                Timber.w("Unknown stream type: $streamTypeStr, defaulting to MUSIC")
                AudioManager.STREAM_MUSIC
            }
        }

        try {
            val maxVolume = audioManager.getStreamMaxVolume(streamType)
            val targetVolume = (maxVolume * (volumeLevel / 100f)).toInt().coerceIn(0, maxVolume)
            
            audioManager.setStreamVolume(streamType, targetVolume, AudioManager.FLAG_SHOW_UI)
            Timber.i("Volume set to $targetVolume (approx $volumeLevel%) for stream $streamTypeStr")
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied: Failed to control volume")
            throw e
        } catch (e: Exception) {
            Timber.e(e, "Failed to control volume")
            throw e
        }
    }

    private fun showNotification(config: Map<String, Any>) {
        val title = config["title"]?.toString() ?: "Automation Executed"
        val message = config["message"]?.toString() ?: "Action completed successfully"
        val channelId = config["channelId"]?.toString() ?: ACTION_CHANNEL_ID
        
        // Check permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Timber.w("Missing POST_NOTIFICATIONS permission - notification will not be shown")
                // Don't throw exception for missing notification permission - it's not critical
                return
            }
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
        } catch (e: SecurityException) {
            Timber.e(e, "Permission denied: Failed to show notification")
            // Don't throw - notification permission is optional
        } catch (e: Exception) {
            Timber.e(e, "Failed to show notification")
            // Don't throw - notification failures shouldn't break automation
        }
    }
    
    private suspend fun showToast(config: Map<String, Any>) {
        val message = config["message"]?.toString() ?: "Automation executed"
        val duration = when (config["duration"]?.toString()?.lowercase()) {
            "long" -> android.widget.Toast.LENGTH_LONG
            else -> android.widget.Toast.LENGTH_SHORT
        }

        try {
            withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, message, duration).show()
                Timber.i("Toast shown: $message")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to show toast")
            throw e
        }
    }

    private fun logToHistory(config: Map<String, Any>) {
        val message = config["message"]?.toString() ?: "Manual log entry"
        Timber.i("Custom log entry: $message")
    }

    // System settings actions
    private fun setBrightness(config: Map<String, Any>) {
        val brightness = config["brightness"]?.toString()?.toIntOrNull() ?: 50
        if (brightness !in 0..100) {
            throw IllegalArgumentException("Brightness must be 0-100")
        }
        
        try {
            val brightnessValue = (brightness / 100f * 255).toInt().coerceIn(0, 255)
            Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, brightnessValue)
            Timber.i("Brightness set to $brightness%")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set brightness")
            throw e
        }
    }

    private fun toggleAirplaneMode(config: Map<String, Any>) {
        val enable = config["enabled"] as? Boolean ?: true
        try {
            Settings.Global.putInt(
                context.contentResolver,
                Settings.Global.AIRPLANE_MODE_ON,
                if (enable) 1 else 0
            )
            // Broadcast the change
            val intent = Intent(Intent.ACTION_AIRPLANE_MODE_CHANGED).apply {
                putExtra("state", enable)
                // Make explicit to avoid implicit broadcast security issues
                setComponent(android.content.ComponentName(context, "com.aditsyal.autodroid.receivers.DeviceStateReceiver"))
            }
            context.sendBroadcast(intent)
            Timber.i("Airplane mode set to: $enable")
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle airplane mode")
            throw e
        }
    }

    private fun toggleGPS(config: Map<String, Any>) {
        val enable = config["enabled"] as? Boolean ?: true
        try {
            Settings.Secure.putInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                if (enable) Settings.Secure.LOCATION_MODE_HIGH_ACCURACY else Settings.Secure.LOCATION_MODE_OFF
            )
            Timber.i("GPS set to: $enable")
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle GPS")
            throw e
        }
    }

    private fun setScreenTimeout(config: Map<String, Any>) {
        val timeoutSeconds = config["timeoutSeconds"]?.toString()?.toIntOrNull() ?: 30
        try {
            Settings.System.putInt(
                context.contentResolver,
                Settings.System.SCREEN_OFF_TIMEOUT,
                timeoutSeconds * 1000
            )
            Timber.i("Screen timeout set to $timeoutSeconds seconds")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set screen timeout")
            throw e
        }
    }

    // Device control actions
    private fun lockScreen() {
        try {
            // Note: Locking the screen programmatically requires system permissions (DEVICE_POWER)
            // Regular apps cannot lock the screen directly
            // Alternative: Launch lock screen settings or use accessibility service
            val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
            if (keyguardManager != null && keyguardManager.isKeyguardLocked) {
                Timber.i("Keyguard is already locked")
            } else {
                // Try to open lock screen settings as fallback
                try {
                    val intent = Intent(Settings.ACTION_SECURITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Timber.i("Opened security settings (screen lock requires system permissions)")
                } catch (e: Exception) {
                    Timber.w(e, "Cannot lock screen - requires system permissions")
                }
            }
        } catch (e: SecurityException) {
            Timber.w(e, "Lock screen requires system permissions")
            // Don't throw - this is expected for regular apps
        } catch (e: Exception) {
            Timber.e(e, "Failed to lock screen")
            // Don't throw - this is a limitation of regular apps
        }
    }

    private fun sleepDevice() {
        try {
            // Note: goToSleep() requires DEVICE_POWER permission which is system-only
            // Regular apps cannot put device to sleep
            // Alternative: Use PowerManager to turn off screen if we had the permission
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
            if (powerManager != null) {
                // This would require DEVICE_POWER permission
                Timber.w("Sleep device requires DEVICE_POWER system permission - not available to regular apps")
            } else {
                Timber.w("PowerManager not available")
            }
            Timber.i("Sleep device requested (requires system permissions)")
        } catch (e: SecurityException) {
            Timber.w(e, "Sleep device requires system permissions")
            // Don't throw - this is expected for regular apps
        } catch (e: Exception) {
            Timber.e(e, "Failed to sleep device")
            throw e
        }
    }

    private fun setDoNotDisturb(config: Map<String, Any>, enable: Boolean) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (enable) {
                    notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
                } else {
                    notificationManager?.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                }
                Timber.i("Do Not Disturb set to: $enable")
            } else {
                throw UnsupportedOperationException("Do Not Disturb requires Android M+")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set Do Not Disturb")
            throw e
        }
    }

    // Communication actions
    private fun sendEmail(config: Map<String, Any>) {
        val to = config["to"]?.toString() ?: ""
        val subject = config["subject"]?.toString() ?: ""
        val body = config["body"]?.toString() ?: ""
        
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Send Email"))
            Timber.i("Email intent launched")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send email")
            throw e
        }
    }

    private fun makeCall(config: Map<String, Any>) {
        val phoneNumber = config["phoneNumber"]?.toString()
        
        if (phoneNumber == null) {
            throw IllegalArgumentException("Phone number is required for call")
        }
        
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            throw SecurityException("CALL_PHONE permission required")
        }
        
        try {
            val intent = Intent(Intent.ACTION_CALL).apply {
                data = Uri.parse("tel:$phoneNumber")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Timber.i("Call initiated to $phoneNumber")
        } catch (e: Exception) {
            Timber.e(e, "Failed to make call")
            throw e
        }
    }

    private suspend fun speakText(config: Map<String, Any>) {
        val text = config["text"]?.toString() ?: ""
        if (text.isEmpty()) {
            throw IllegalArgumentException("Text is required for TTS")
        }
        
        try {
            var ttsInstance: TextToSpeech? = null
            ttsInstance = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    ttsInstance?.language = Locale.getDefault()
                    ttsInstance?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
                    Timber.i("TTS: Speaking '$text'")
                } else {
                    Timber.e("TTS initialization failed")
                }
            }
            // Keep reference to prevent GC
            delay(1000) // Give TTS time to initialize
            // Clean up after speaking
            delay(2000) // Wait for speech to complete
            ttsInstance?.stop()
            ttsInstance?.shutdown()
        } catch (e: Exception) {
            Timber.e(e, "Failed to speak text")
            throw e
        }
    }

    // App control actions
    private fun closeApp(config: Map<String, Any>) {
        val packageName = config["packageName"]?.toString()

        if (packageName == null) {
            throw IllegalArgumentException("Package name is required")
        }

        try {
            val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? android.app.ActivityManager
            activityManager?.killBackgroundProcesses(packageName)
            Timber.i("App closed: $packageName")
        } catch (e: Exception) {
            Timber.e(e, "Failed to close app: $packageName")
            throw e
        }
    }

    // Media actions

    private fun changeWallpaper(config: Map<String, Any>) {
        val imageUri = config["imageUri"]?.toString()
        
        if (imageUri == null) {
            throw IllegalArgumentException("Image URI is required")
        }
        
        try {
            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setDataAndType(Uri.parse(imageUri), "image/*")
                putExtra("mimeType", "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Set as wallpaper"))
            Timber.i("Wallpaper change intent launched")
        } catch (e: Exception) {
            Timber.e(e, "Failed to change wallpaper")
            throw e
        }
    }

    // Automation actions
    private suspend fun delayAction(config: Map<String, Any>) {
        val delayMs = config["delayMs"]?.toString()?.toLongOrNull() 
            ?: config["delaySeconds"]?.toString()?.toLongOrNull()?.times(1000)
            ?: 1000L
        
        Timber.d("Delaying for ${delayMs}ms")
        delay(delayMs)
    }

    private suspend fun sendHttpRequest(config: Map<String, Any>) {
        val url = config["url"]?.toString()
        val method = config["method"]?.toString() ?: "GET"
        val headers = config["headers"] as? Map<*, *>
        val body = config["body"]?.toString()
        
        if (url == null) {
            throw IllegalArgumentException("URL is required for HTTP request")
        }
        
        try {
            withContext(Dispatchers.IO) {
                val connection = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                connection.requestMethod = method
                
                headers?.forEach { (key, value) ->
                    connection.setRequestProperty(key.toString(), value.toString())
                }
                
                if (body != null && method in listOf("POST", "PUT", "PATCH")) {
                    connection.doOutput = true
                    connection.outputStream.use { it.write(body.toByteArray()) }
                }
                
                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                
                Timber.i("HTTP $method to $url: Response $responseCode")
                Timber.d("Response body: $response")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to send HTTP request")
            throw e
        }
    }

    // Variable support
    private suspend fun replaceVariablePlaceholders(config: Map<String, Any>, macroId: Long?): Map<String, Any> {
        val result = mutableMapOf<String, Any>()
        
        config.forEach { (key, value) ->
            when (value) {
                is String -> {
                    // Replace {variable_name} placeholders
                    val processed = replacePlaceholdersInString(value, macroId)
                    result[key] = processed
                }
                is Map<*, *> -> {
                    // Recursively process nested maps
                    @Suppress("UNCHECKED_CAST")
                    result[key] = replaceVariablePlaceholders(value as Map<String, Any>, macroId)
                }
                else -> {
                    result[key] = value
                }
            }
        }
        
        return result
    }
    
    private suspend fun replacePlaceholdersInString(text: String, macroId: Long?): String {
        val pattern = Regex("\\{([^}]+)\\}")
        var result = text
        pattern.findAll(text).forEach { matchResult ->
            val variableName = matchResult.groupValues[1]
            val variable = getVariableUseCase(variableName, macroId)
            val replacement = variable?.value ?: matchResult.value
            result = result.replace(matchResult.value, replacement)
        }
        return result
    }
    
    private suspend fun setVariable(config: Map<String, Any>, macroId: Long?) {
        val variableName = config["variableName"]?.toString()
        val value = config["value"]?.toString()
        val scope = config["scope"]?.toString() ?: "LOCAL"
        val operation = config["operation"]?.toString()
        
        if (variableName == null || value == null) {
            throw IllegalArgumentException("Variable name and value are required")
        }
        
        try {
            val finalValue = if (operation != null) {
                evaluateVariableUseCase(variableName, operation, value, macroId) ?: value
            } else {
                value
            }
            
            val variable = VariableDTO(
                name = variableName,
                value = finalValue,
                scope = scope,
                macroId = if (scope == "LOCAL") macroId else null,
                type = config["type"]?.toString() ?: "STRING"
            )
            
            setVariableUseCase(variable)
            Timber.i("Variable set: $variableName = $finalValue (scope: $scope)")
        } catch (e: Exception) {
            Timber.e(e, "Failed to set variable: $variableName")
            throw e
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
