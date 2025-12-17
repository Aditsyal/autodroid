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
        
        val firedTriggerId = eventData["fired_trigger_id"] as? Long
        val triggers = if (firedTriggerId != null) {
            listOfNotNull(repository.getTriggerById(firedTriggerId))
        } else {
            repository.getEnabledTriggersByType(triggerType)
        }
        
        triggers.forEach { trigger ->
            if (isTriggerMatch(trigger, eventData)) {
                Timber.d("Trigger matched: ${trigger.id} for macro: ${trigger.macroId}")
                executeMacroUseCase(trigger.macroId)
            }
        }
    }

    private fun isTriggerMatch(trigger: TriggerDTO, eventData: Map<String, Any>): Boolean {
        if (trigger.triggerConfig.isEmpty()) return true
        
        // Example logic: if triggerConfig has "level", and eventData has "level", compare them.
        // This supports triggers like "Battery Level < 20%"
        trigger.triggerConfig.forEach { (key, expectedValue) ->
            val actualValue = eventData[key]
            if (actualValue != null) {
                if (!compareValues(actualValue, expectedValue)) return false
            }
        }
        
        return true
    }

    private fun compareValues(actual: Any, expected: Any?): Boolean {
        if (expected == null) return true
        
        // Handle Map-based expected values (e.g., {"operator": "less_than", "value": 20})
        if (expected is Map<*, *>) {
            val operator = expected["operator"]?.toString()
            val expectedValue = expected["value"]
            
            return when (operator) {
                "greater_than" -> compareNumbers(actual, expectedValue) { a, b -> a > b }
                "less_than" -> compareNumbers(actual, expectedValue) { a, b -> a < b }
                "equals" -> actual.toString() == expectedValue?.toString()
                "contains" -> actual.toString().contains(expectedValue?.toString() ?: "")
                "not_equals" -> actual.toString() != expectedValue?.toString()
                else -> actual.toString() == expectedValue?.toString()
            }
        }
        
        // Default to strict equality
        return when {
            actual is Number && expected is Number -> actual.toDouble() == expected.toDouble()
            else -> actual.toString() == expected.toString()
        }
    }

    private fun compareNumbers(actual: Any, expected: Any?, comparator: (Double, Double) -> Boolean): Boolean {
        val a = actual.toString().toDoubleOrNull()
        val b = expected?.toString()?.toDoubleOrNull()
        return if (a != null && b != null) comparator(a, b) else false
    }
}
