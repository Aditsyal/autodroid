package com.aditsyal.autodroid.data.models

data class MacroDTO(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastExecuted: Long? = null,
    val triggers: List<TriggerDTO> = emptyList(),
    val actions: List<ActionDTO> = emptyList(),
    val constraints: List<ConstraintDTO> = emptyList()
)

data class TriggerDTO(
    val id: Long = 0,
    val triggerType: String,
    val triggerConfig: Map<String, Any> = emptyMap()
)

data class ActionDTO(
    val id: Long = 0,
    val actionType: String,
    val actionConfig: Map<String, Any> = emptyMap(),
    val executionOrder: Int,
    val delayAfter: Long = 0
)

data class ConstraintDTO(
    val id: Long = 0,
    val constraintType: String,
    val constraintConfig: Map<String, Any> = emptyMap()
)

data class ExecutionLogDTO(
    val id: Long = 0,
    val macroId: Long,
    val executedAt: Long = System.currentTimeMillis(),
    val executionStatus: String,
    val errorMessage: String? = null,
    val executionDurationMs: Long = 0
)

