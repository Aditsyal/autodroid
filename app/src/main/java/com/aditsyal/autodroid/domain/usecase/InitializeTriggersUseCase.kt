package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.automation.trigger.TriggerManager
import com.aditsyal.autodroid.domain.repository.MacroRepository
import timber.log.Timber
import javax.inject.Inject

class InitializeTriggersUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val triggerManager: TriggerManager
) {
    suspend operator fun invoke() {
        Timber.d("Initializing triggers: Loading all enabled triggers from database")
        try {
            val triggers = repository.getAllEnabledTriggers()
            Timber.d("Found ${triggers.size} enabled triggers to register")
            
            triggers.forEach { trigger ->
                try {
                    triggerManager.registerTrigger(trigger)
                    Timber.d("Registered trigger ${trigger.id} of type ${trigger.triggerType}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to register trigger ${trigger.id} of type ${trigger.triggerType}")
                }
            }
            
            Timber.i("Trigger initialization completed: ${triggers.size} triggers registered")
        } catch (e: Exception) {
            Timber.e(e, "Error initializing triggers")
        }
    }
}

