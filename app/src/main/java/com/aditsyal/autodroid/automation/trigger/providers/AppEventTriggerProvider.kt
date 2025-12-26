package com.aditsyal.autodroid.automation.trigger.providers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
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
 * Trigger provider for app-related events:
 * - App launched
 * - App closed
 * - App installed
 * - App uninstalled
 * - Notification received (from any/specific app)
 */
@Singleton
class AppEventTriggerProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val checkTriggersUseCase: CheckTriggersUseCase
) : TriggerProvider, BroadcastReceiver() {

    override val type: String = "APP_EVENT"
    private val scope = CoroutineScope(Dispatchers.IO)
    private val activeTriggers = mutableMapOf<Long, TriggerDTO>()
    private var isRegistered = false

    override suspend fun registerTrigger(trigger: TriggerDTO) {
        try {
            activeTriggers[trigger.id] = trigger
            registerReceiverIfNeeded()
            Timber.d("Registered app event trigger ${trigger.id}: ${trigger.triggerConfig["event"]}")
        } catch (e: Exception) {
            Timber.e(e, "Failed to register app event trigger ${trigger.id}")
        }
    }

    override suspend fun unregisterTrigger(triggerId: Long) {
        try {
            activeTriggers.remove(triggerId)
            if (activeTriggers.isEmpty()) {
                unregisterReceiver()
            }
            Timber.d("Unregistered app event trigger $triggerId")
        } catch (e: Exception) {
            Timber.e(e, "Failed to unregister app event trigger $triggerId")
        }
    }

    override suspend fun clearTriggers() {
        try {
            activeTriggers.clear()
            unregisterReceiver()
        } catch (e: Exception) {
            Timber.e(e, "Failed to clear app event triggers")
        }
    }

    private fun registerReceiverIfNeeded() {
        if (!isRegistered && activeTriggers.isNotEmpty()) {
            try {
                val filter = IntentFilter().apply {
                    addAction(Intent.ACTION_PACKAGE_ADDED)
                    addAction(Intent.ACTION_PACKAGE_REMOVED)
                    addDataScheme("package")
                }
                context.registerReceiver(this, filter)
                isRegistered = true
                Timber.d("AppEventTriggerProvider: Registered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to register AppEventTriggerProvider receiver")
            }
        }
    }

    private fun unregisterReceiver() {
        if (isRegistered) {
            try {
                context.unregisterReceiver(this)
                isRegistered = false
                Timber.d("AppEventTriggerProvider: Unregistered broadcast receiver")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister AppEventTriggerProvider receiver")
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
                    Intent.ACTION_PACKAGE_ADDED -> {
                        val packageName = intent.data?.schemeSpecificPart
                        if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                            Timber.d("App installed: $packageName")
                            notifyAppInstalledTrigger(packageName)
                        }
                    }
                    Intent.ACTION_PACKAGE_REMOVED -> {
                        val packageName = intent.data?.schemeSpecificPart
                        if (packageName != null && !intent.getBooleanExtra(Intent.EXTRA_REPLACING, false)) {
                            Timber.d("App uninstalled: $packageName")
                            notifyAppUninstalledTrigger(packageName)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in AppEventTriggerProvider.onReceive")
            } finally {
                pendingResult.finish()
            }
        }
    }

    /**
     * Called by AccessibilityService when app is launched
     */
    fun onAppLaunched(packageName: String) {
        scope.launch {
            try {
                Timber.d("App launched: $packageName")
                activeTriggers.values
                    .filter { it.triggerConfig["event"] == "APP_LAUNCHED" }
                    .forEach { trigger ->
                        val requiredPackage = trigger.triggerConfig["packageName"]?.toString()
                        if (requiredPackage == null || packageName == requiredPackage) {
                            notifyTrigger(trigger, mapOf("packageName" to packageName))
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process app launched event")
            }
        }
    }

    /**
     * Called by AccessibilityService when app is closed
     */
    fun onAppClosed(packageName: String) {
        scope.launch {
            try {
                Timber.d("App closed: $packageName")
                activeTriggers.values
                    .filter { it.triggerConfig["event"] == "APP_CLOSED" }
                    .forEach { trigger ->
                        val requiredPackage = trigger.triggerConfig["packageName"]?.toString()
                        if (requiredPackage == null || packageName == requiredPackage) {
                            notifyTrigger(trigger, mapOf("packageName" to packageName))
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process app closed event")
            }
        }
    }

    /**
     * Called when notification is received
     */
    fun onNotificationReceived(packageName: String, title: String? = null, text: String? = null) {
        scope.launch {
            try {
                Timber.d("Notification received from: $packageName")
                activeTriggers.values
                    .filter { it.triggerConfig["event"] == "NOTIFICATION_RECEIVED" }
                    .forEach { trigger ->
                        val requiredPackage = trigger.triggerConfig["packageName"]?.toString()
                        if (requiredPackage == null || packageName == requiredPackage) {
                            notifyTrigger(trigger, mapOf(
                                "packageName" to packageName,
                                "title" to (title ?: ""),
                                "text" to (text ?: "")
                            ))
                        }
                    }
            } catch (e: Exception) {
                Timber.e(e, "Failed to process notification received event")
            }
        }
    }

    private fun notifyAppInstalledTrigger(packageName: String) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "APP_INSTALLED" }
            .forEach { trigger ->
                val requiredPackage = trigger.triggerConfig["packageName"]?.toString()
                if (requiredPackage == null || packageName == requiredPackage) {
                    notifyTrigger(trigger, mapOf("packageName" to packageName))
                }
            }
    }

    private fun notifyAppUninstalledTrigger(packageName: String) {
        activeTriggers.values
            .filter { it.triggerConfig["event"] == "APP_UNINSTALLED" }
            .forEach { trigger ->
                val requiredPackage = trigger.triggerConfig["packageName"]?.toString()
                if (requiredPackage == null || packageName == requiredPackage) {
                    notifyTrigger(trigger, mapOf("packageName" to packageName))
                }
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

