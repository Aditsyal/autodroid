package com.aditsyal.autodroid.automation.trigger.providers

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.aditsyal.autodroid.automation.trigger.TriggerProvider
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Trigger provider for connectivity events:
 * - WiFi connected/disconnected
 * - WiFi SSID connected (specific network)
 * - Bluetooth connected/disconnected
 * - Bluetooth device connected (specific device)
 * - Mobile data enabled/disabled
 */
@Singleton
class ConnectivityTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, BroadcastReceiver() {

    override val type: String = "CONNECTIVITY"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
    private val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
    private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private var isRegistered = false
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerReceiversIfNeeded()
            Timber.d("Registered connectivity trigger ${trigger.id}: ${trigger.triggerConfig["event"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register connectivity trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterReceivers()
            }
            Timber.d("Unregistered connectivity trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister connectivity trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterReceivers()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear connectivity triggers")
        }
    }

    private fun registerReceiversIfNeeded() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            try {
                // Register for WiFi and Bluetooth events
                val filter = IntentFilter().apply {
                    addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
                    addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
                    addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                    addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
                    addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
                }
                context.registerReceiver(this, filter)
                isRegistered = true

                // Register network callback for mobile data (Android 7.0+)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    registerNetworkCallback()
                }

                Timber.d("ConnectivityTriggerProvider: Registered receivers")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register ConnectivityTriggerProvider receivers")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun registerNetworkCallback() {
        if (networkCallback == null) {
            val callback = object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    checkMobileDataTriggers(true)
                }

                override fun onLost(network: Network) {
                    checkMobileDataTriggers(false)
                }
            }
            networkCallback = callback

            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
                .build()

            connectivityManager?.registerNetworkCallback(request, callback)
        }
    }

    private fun unregisterReceivers() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
                isRegistered = false
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister ConnectivityTriggerProvider receiver")
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            networkCallback?.let {
                try {
                    connectivityManager?.unregisterNetworkCallback(it)
                    networkCallback = null
                } catch (e: Exception) {
                    Timber.e(e, "Failed to unregister network callback")
                }
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return

        val pendingResult = goAsync()
        scope.launch {
            try {
                when (intent.action) {
                    WifiManager.NETWORK_STATE_CHANGED_ACTION -> {
                        val networkInfo = intent.getParcelableExtra<android.net.NetworkInfo>(WifiManager.EXTRA_NETWORK_INFO)
                        if (networkInfo?.isConnected == true) {
                            val ssid = getCurrentWifiSSID()
                            Timber.d("WiFi connected: $ssid")
                            notifyWifiTriggers(true, ssid)
                        } else {
                            Timber.d("WiFi disconnected")
                            notifyWifiTriggers(false, null)
                        }
                    }
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
                        when (wifiState) {
                            WifiManager.WIFI_STATE_ENABLED -> {
                                Timber.d("WiFi enabled")
                                notifyWifiTriggers(true, getCurrentWifiSSID())
                            }
                            WifiManager.WIFI_STATE_DISABLED -> {
                                Timber.d("WiFi disabled")
                                notifyWifiTriggers(false, null)
                            }
                        }
                    }
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                        when (state) {
                            BluetoothAdapter.STATE_ON -> {
                                Timber.d("Bluetooth enabled")
                                notifyBluetoothTriggers(true)
                            }
                            BluetoothAdapter.STATE_OFF -> {
                                Timber.d("Bluetooth disabled")
                                notifyBluetoothTriggers(false)
                            }
                        }
                    }
                    BluetoothDevice.ACTION_ACL_CONNECTED -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                it.name ?: "Unknown Device"
                            } else {
                                "Unknown Device (no permission)"
                            }
                            Timber.d("Bluetooth device connected: $deviceName")
                            notifyBluetoothDeviceTriggers(it, true)
                        }
                    }
                    BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        device?.let {
                            val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                                it.name ?: "Unknown Device"
                            } else {
                                "Unknown Device (no permission)"
                            }
                            Timber.d("Bluetooth device disconnected: $deviceName")
                            notifyBluetoothDeviceTriggers(it, false)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in ConnectivityTriggerProvider.onReceive")
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun getCurrentWifiSSID(): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ requires location permission
                wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            } else {
                @Suppress("DEPRECATION")
                wifiManager?.connectionInfo?.ssid?.replace("\"", "")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get WiFi SSID")
            null
        }
    }

    private fun notifyWifiTriggers(connected: Boolean, ssid: String?) {
        val event = if (connected) "WIFI_CONNECTED" else "WIFI_DISCONNECTED"
        activeTriggers.values
            .filter { it.triggerConfig["event"] == event }
            .forEach { trigger ->
                val requiredSSID = trigger.triggerConfig["ssid"]?.toString()
                if (requiredSSID == null || ssid == requiredSSID) {
                    notifyTrigger(trigger, mapOf("ssid" to (ssid ?: "")))
                }
            }
    }

    private fun notifyBluetoothTriggers(enabled: Boolean) {
        val event = if (enabled) "BLUETOOTH_CONNECTED" else "BLUETOOTH_DISCONNECTED"
        activeTriggers.values
            .filter { it.triggerConfig["event"] == event }
            .forEach { trigger ->
                notifyTrigger(trigger)
            }
    }

    private fun notifyBluetoothDeviceTriggers(device: BluetoothDevice, connected: Boolean) {
        val event = if (connected) "BLUETOOTH_DEVICE_CONNECTED" else "BLUETOOTH_DEVICE_DISCONNECTED"
        val deviceName = if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            device.name ?: "Unknown Device"
        } else {
            "Unknown Device (no permission)"
        }

        activeTriggers.values
            .filter { it.triggerConfig["event"] == event }
            .forEach { trigger ->
                val requiredAddress = trigger.triggerConfig["deviceAddress"]?.toString()
                if (requiredAddress == null || device.address == requiredAddress) {
                    notifyTrigger(trigger, mapOf(
                        "deviceName" to deviceName,
                        "deviceAddress" to device.address
                    ))
                }
            }
    }

    private fun checkMobileDataTriggers(enabled: Boolean) {
        val event = if (enabled) "MOBILE_DATA_ENABLED" else "MOBILE_DATA_DISABLED"
        activeTriggers.values
            .filter { it.triggerConfig["event"] == event }
            .forEach { trigger ->
                notifyTrigger(trigger)
            }
    }

    private fun notifyTrigger(trigger: TriggerDTO, additionalData: Map<String, Any> = emptyMap()) {
        scope.launch {
            try {
                checkTriggersUseCase(type, additionalData + mapOf("fired_trigger_id" to trigger.id))
            } catch (e: Exception) {
                Timber.e(e, "Failed to check trigger ${trigger.id}")
            }
        }
    }
}

