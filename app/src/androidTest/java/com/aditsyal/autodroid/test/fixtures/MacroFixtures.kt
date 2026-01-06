package com.aditsyal.autodroid.test.fixtures

import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.google.gson.Gson

object MacroFixtures {
    private val gson = Gson()
    
    fun createSimpleMacro(
        name: String = "Test Macro",
        enabled: Boolean = true
    ): MacroDTO {
        return MacroDTO(
            name = name,
            description = "Test description",
            enabled = enabled,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )
    }
    
    fun createMacroWithTrigger(
        name: String = "Macro with Trigger",
        triggerType: String = "TIME",
        triggerConfig: Map<String, Any> = mapOf("time" to "12:00")
    ): MacroDTO {
        return MacroDTO(
            name = name,
            triggers = listOf(
                TriggerDTO(
                    triggerType = triggerType,
                    triggerConfig = triggerConfig
                )
            ),
            actions = emptyList(),
            constraints = emptyList()
        )
    }
    
    fun createMacroWithAction(
        name: String = "Macro with Action",
        actionType: String = "SHOW_TOAST",
        actionConfig: Map<String, Any> = mapOf("message" to "Test")
    ): MacroDTO {
        return MacroDTO(
            name = name,
            triggers = emptyList(),
            actions = listOf(
                ActionDTO(
                    actionType = actionType,
                    actionConfig = actionConfig,
                    executionOrder = 1
                )
            ),
            constraints = emptyList()
        )
    }
    
    fun createMacroWithConstraint(
        name: String = "Macro with Constraint",
        constraintType: String = "BATTERY_LEVEL",
        constraintConfig: Map<String, Any> = mapOf("operator" to "greater_than", "value" to 50)
    ): MacroDTO {
        return MacroDTO(
            name = name,
            triggers = emptyList(),
            actions = emptyList(),
            constraints = listOf(
                ConstraintDTO(
                    constraintType = constraintType,
                    constraintConfig = constraintConfig
                )
            )
        )
    }
    
    fun createCompleteMacro(
        name: String = "Complete Macro",
        triggerType: String = "TIME",
        actionType: String = "SHOW_TOAST",
        constraintType: String? = null
    ): MacroDTO {
        val triggers = listOf(
            TriggerDTO(
                triggerType = triggerType,
                triggerConfig = mapOf("time" to "12:00")
            )
        )
        
        val actions = listOf(
            ActionDTO(
                actionType = actionType,
                actionConfig = mapOf("message" to "Test message"),
                executionOrder = 1
            )
        )
        
        val constraints = constraintType?.let {
            listOf(
                ConstraintDTO(
                    constraintType = it,
                    constraintConfig = mapOf("operator" to "greater_than", "value" to 50)
                )
            )
        } ?: emptyList()
        
        return MacroDTO(
            name = name,
            triggers = triggers,
            actions = actions,
            constraints = constraints
        )
    }
}

