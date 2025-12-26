package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.automation.trigger.TriggerManager
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import timber.log.Timber
import javax.inject.Inject

class UpdateMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val triggerManager: TriggerManager
) {
    suspend operator fun invoke(macro: MacroDTO) {
        // Get old macro to unregister old triggers
        val oldMacro = repository.getMacroById(macro.id)
        
        // Unregister all old triggers
        oldMacro?.triggers?.forEach { oldTrigger ->
            try {
                triggerManager.unregisterTrigger(oldTrigger)
                Timber.d("Unregistered old trigger ${oldTrigger.id} of type ${oldTrigger.triggerType}")
            } catch (e: Exception) {
                Timber.e(e, "Failed to unregister old trigger ${oldTrigger.id}")
            }
        }
        
        // Update macro in database
        repository.updateMacro(macro)
        
        // Register new triggers if macro is enabled
        if (macro.enabled && macro.triggers.isNotEmpty()) {
            Timber.d("Registering ${macro.triggers.size} triggers for updated macro ${macro.id}")
            // Get the saved macro with actual trigger IDs from database
            val savedMacro = repository.getMacroById(macro.id)
            savedMacro?.triggers?.forEach { savedTrigger ->
                try {
                    triggerManager.registerTrigger(savedTrigger)
                    Timber.d("Registered trigger ${savedTrigger.id} of type ${savedTrigger.triggerType}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to register trigger ${savedTrigger.id} for macro ${macro.id}")
                }
            }
        }
    }
}


