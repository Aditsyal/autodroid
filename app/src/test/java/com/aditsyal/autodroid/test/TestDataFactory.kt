package com.aditsyal.autodroid.test

import com.aditsyal.autodroid.data.models.*

/**
 * Factory class for creating test data objects with consistent values.
 * This ensures all tests use the same data patterns and makes test data
 * generation centralized and reusable.
 *
 * Usage:
 * val macro = TestDataFactory.createTestMacro(id = 1L, name = "Custom Macro")
 * val trigger = TestDataFactory.createTestTrigger(triggerType = "LOCATION")
 */
object TestDataFactory {

    fun createTestMacro(
        id: Long = 1L,
        name: String = "Test Macro",
        description: String = "Test macro description",
        enabled: Boolean = true,
        triggers: List<TriggerDTO> = listOf(createTestTrigger()),
        actions: List<ActionDTO> = listOf(createTestAction()),
        constraints: List<ConstraintDTO> = emptyList(),
        createdAt: Long = System.currentTimeMillis(),
        lastExecuted: Long? = null
    ) = MacroDTO(
        id = id,
        name = name,
        description = description,
        enabled = enabled,
        triggers = triggers,
        actions = actions,
        constraints = constraints,
        createdAt = createdAt,
        lastExecuted = lastExecuted
    )

    fun createTestTrigger(
        id: Long = 1L,
        macroId: Long = 1L,
        triggerType: String = "TIME",
        triggerConfig: Map<String, Any> = mapOf("hour" to 9, "minute" to 0)
    ) = TriggerDTO(
        id = id,
        macroId = macroId,
        triggerType = triggerType,
        triggerConfig = triggerConfig
    )

    fun createTestAction(
        id: Long = 1L,
        actionType: String = "TOAST",
        actionConfig: Map<String, Any> = mapOf("message" to "Test action executed"),
        executionOrder: Int = 0,
        delayAfter: Long = 0
    ) = ActionDTO(
        id = id,
        actionType = actionType,
        actionConfig = actionConfig,
        executionOrder = executionOrder,
        delayAfter = delayAfter
    )

    fun createTestConstraint(
        id: Long = 1L,
        constraintType: String = "BATTERY_LEVEL",
        constraintConfig: Map<String, Any> = mapOf("minLevel" to 20)
    ) = ConstraintDTO(
        id = id,
        constraintType = constraintType,
        constraintConfig = constraintConfig
    )

    fun createTestExecutionLog(
        id: Long = 1L,
        macroId: Long = 1L,
        macroName: String? = "Test Macro",
        executedAt: Long = System.currentTimeMillis(),
        executionStatus: String = "SUCCESS",
        errorMessage: String? = null,
        executionDurationMs: Long = 1000,
        actions: List<ActionDTO> = listOf(createTestAction())
    ) = ExecutionLogDTO(
        id = id,
        macroId = macroId,
        macroName = macroName,
        executedAt = executedAt,
        executionStatus = executionStatus,
        errorMessage = errorMessage,
        executionDurationMs = executionDurationMs,
        actions = actions
    )

    // Utility functions for common test scenarios
    fun createMacroList(count: Int = 3): List<MacroDTO> =
        (1..count).map { id ->
            createTestMacro(
                id = id.toLong(),
                name = "Test Macro $id",
                triggers = listOf(createTestTrigger(id = id.toLong())),
                actions = listOf(createTestAction(id = id.toLong()))
            )
        }

    fun createTriggerList(count: Int = 2): List<TriggerDTO> =
        (1..count).map { id ->
            createTestTrigger(
                id = id.toLong(),
                triggerType = if (id % 2 == 0) "LOCATION" else "TIME"
            )
        }

    fun createActionList(count: Int = 2): List<ActionDTO> =
        (1..count).map { id ->
            createTestAction(
                id = id.toLong(),
                actionType = if (id % 2 == 0) "NOTIFICATION" else "TOAST",
                executionOrder = id
            )
        }
}