package com.aditsyal.autodroid.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.aditsyal.autodroid.domain.usecase.CheckTriggersUseCase
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

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val accessibilityEvents = MutableSharedFlow<AccessibilityEvent>(extraBufferCapacity = 64)

    override fun onCreate() {
        super.onCreate()
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
        try {
            val eventParams = mutableMapOf<String, Any>(
                "packageName" to (event.packageName?.toString() ?: ""),
                "eventType" to event.eventType,
                "className" to (event.className?.toString() ?: "")
            )
            
            checkTriggersUseCase("APP_EVENT", eventParams)
            
        } catch (e: Exception) {
            Timber.e(e, "Error processing accessibility event")
        }
    }

    override fun onInterrupt() {
        Timber.d("AccessibilityService interrupted")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
}

