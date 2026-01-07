package com.aditsyal.autodroid.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.aditsyal.autodroid.automation.trigger.providers.AppEventTriggerProvider
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
import com.aditsyal.autodroid.utils.MemoryMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AutomationAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var checkTriggersUseCase: CheckTriggersUseCase

    @Inject
    lateinit var appEventTriggerProvider: AppEventTriggerProvider

    @Inject
    lateinit var memoryMonitor: MemoryMonitor

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val accessibilityEvents = MutableSharedFlow<AccessibilityEvent>(extraBufferCapacity = 64)
    private var currentAppPackage: String? = null

    override fun onCreate() {
        super.onCreate()
        memoryMonitor.logMemoryUsage("AccessibilityService_Start")
        observeEvents()
    }

    @OptIn(FlowPreview::class)
    private fun observeEvents() {
        serviceScope.launch {
            accessibilityEvents
                .debounce(300L) // Throttling: matching review recommendation
                .collectLatest { event ->
                    processEvent(event)
                }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            accessibilityEvents.tryEmit(it)
        }
    }

    private suspend fun processEvent(event: AccessibilityEvent) {
        // Filter out irrelevant events early to reduce processing
        if (!isRelevantEvent(event)) {
            return
        }

        try {
            val packageName = event.packageName?.toString() ?: return

            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    // Detect app launch/close
                    if (currentAppPackage != packageName) {
                        // App changed - previous app was closed, new app was launched
                        currentAppPackage?.let { closedPackage ->
                            appEventTriggerProvider.onAppClosed(closedPackage)
                        }
                        appEventTriggerProvider.onAppLaunched(packageName)
                        currentAppPackage = packageName
                    }
                }
                AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED -> {
                    // Notification received
                    val notificationText = event.text?.joinToString(" ") ?: ""
                    val notificationTitle = event.contentDescription?.toString() ?: ""
                    appEventTriggerProvider.onNotificationReceived(
                        packageName = packageName,
                        title = notificationTitle,
                        text = notificationText
                    )
                }
                else -> {
                    // For other events, use the general trigger checking
                    val eventParams = mutableMapOf<String, Any>(
                        "packageName" to packageName,
                        "eventType" to event.eventType,
                        "className" to (event.className?.toString() ?: "")
                    )
                    checkTriggersUseCase("APP_EVENT", eventParams)
                }
            }

        } catch (e: Exception) {
            Timber.e(e, "Error processing accessibility event")
        }
    }

    private fun isRelevantEvent(event: AccessibilityEvent): Boolean {
        return event.eventType in listOf(
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED,
            AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_VIEW_CLICKED
        )
    }

    override fun onInterrupt() {
        Timber.d("AccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

