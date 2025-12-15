package com.aditsyal.autodroid.services.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class AutomationAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var triggerDao: TriggerDao

    @Inject
    lateinit var executeMacroUseCase: ExecuteMacroUseCase

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val accessibilityEvents = MutableSharedFlow<AccessibilityEvent>(extraBufferCapacity = 64)

    override fun onCreate() {
        super.onCreate()
        observeEvents()
    }

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
            // Fetch triggers that care about interaction/app events
            val triggers = triggerDao.getEnabledTriggersByType("APP_EVENT")
            
            triggers.forEach { trigger ->
                // TODO: Real matching logic based on triggerConfig JSON
                // For MVP: Simple package name match if config contains it
                if (trigger.triggerConfig.contains(event.packageName ?: "")) {
                    Timber.d("Trigger matched: \${trigger.id} for package \${event.packageName}")
                    executeMacroUseCase(trigger.macroId)
                }
            }
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

