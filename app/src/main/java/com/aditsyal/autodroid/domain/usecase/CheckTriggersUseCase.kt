package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import javax.inject.Inject
import timber.log.Timber

class CheckTriggersUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val executeMacroUseCase: ExecuteMacroUseCase
) {
    suspend operator fun invoke(triggerType: String, eventData: Map<String, Any> = emptyMap()) {
        Timber.d("Checking triggers for type: $triggerType with data: $eventData")
        val triggers = repository.getEnabledTriggersByType(triggerType)
        
        triggers.forEach { trigger ->
            if (isTriggerMatch(trigger, eventData)) {
                Timber.d("Trigger matched: ${trigger.id} for macro: ${trigger.macroId}")
                executeMacroUseCase(trigger.macroId)
            }
        }
    }

    private fun isTriggerMatch(trigger: TriggerDTO, eventData: Map<String, Any>): Boolean {
        // Simple matching logic for MVP. 
        // Real implementation would verify conditions like "battery level < 15" vs "event level = 14".
        // For strictly event-based (e.g. "Screen On"), matching the type is often enough if config is empty.
        
        if (eventData.isEmpty()) return true
        
        // Example: if triggerConfig has "level", and eventData has "level", compare them.
        // This is a placeholder for complex logic.
        return true
    }
}
