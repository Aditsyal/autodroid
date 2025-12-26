package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.automation.trigger.TriggerManager
import com.aditsyal.autodroid.domain.repository.MacroRepository
import timber.log.Timber
import javax.inject.Inject

class ToggleMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val triggerManager: TriggerManager
) {
    suspend operator fun invoke(macroId: Long, enabled: Boolean) {
        // Get macro to access its triggers
        val macro = repository.getMacroById(macroId)
        
        // Update macro enabled state in database
        repository.toggleMacro(macroId, enabled)
        
        if (macro != null) {
            if (enabled) {
                // Register all triggers when macro is enabled
                Timber.d("Enabling macro $macroId: Registering ${macro.triggers.size} triggers")
                macro.triggers.forEach { trigger ->
                    try {
                        triggerManager.registerTrigger(trigger)
                        Timber.d("Registered trigger ${trigger.id} of type ${trigger.triggerType}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to register trigger ${trigger.id} for macro $macroId")
                    }
                }
            } else {
                // Unregister all triggers when macro is disabled
                Timber.d("Disabling macro $macroId: Unregistering ${macro.triggers.size} triggers")
                macro.triggers.forEach { trigger ->
                    try {
                        triggerManager.unregisterTrigger(trigger)
                        Timber.d("Unregistered trigger ${trigger.id} of type ${trigger.triggerType}")
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to unregister trigger ${trigger.id} for macro $macroId")
                    }
                }
            }
        }
    }
}


