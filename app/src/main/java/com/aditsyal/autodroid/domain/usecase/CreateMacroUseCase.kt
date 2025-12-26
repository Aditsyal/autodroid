package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.automation.trigger.TriggerManager
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import timber.log.Timber
import javax.inject.Inject

class CreateMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val triggerManager: TriggerManager
) {
    suspend operator fun invoke(macro: MacroDTO): Long {
        val macroId = repository.createMacro(macro)
        
        // Register triggers with system services if macro is enabled
        if (macro.enabled && macro.triggers.isNotEmpty()) {
            Timber.d("Registering ${macro.triggers.size} triggers for new macro $macroId")
            // Get the saved macro with actual trigger IDs from database
            val savedMacro = repository.getMacroById(macroId)
            savedMacro?.triggers?.forEach { savedTrigger ->
                try {
                    triggerManager.registerTrigger(savedTrigger)
                    Timber.d("Registered trigger ${savedTrigger.id} of type ${savedTrigger.triggerType}")
                } catch (e: Exception) {
                    Timber.e(e, "Failed to register trigger ${savedTrigger.id} for macro $macroId")
                }
            }
        }
        
        return macroId
    }
}


