package com.aditsyal.autodroid.data.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
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
) : Parcelable

@Parcelize
data class TriggerDTO(
    val id: Long = 0,
    val macroId: Long = 0,
    val triggerType: String,
    val triggerConfig: @RawValue Map<String, Any> = emptyMap()
) : Parcelable

@Parcelize
data class ActionDTO(
    val id: Long = 0,
    val actionType: String,
    val actionConfig: @RawValue Map<String, Any> = emptyMap(),
    val executionOrder: Int,
    val delayAfter: Long = 0
) : Parcelable

@Parcelize
data class ConstraintDTO(
    val id: Long = 0,
    val constraintType: String,
    val constraintConfig: @RawValue Map<String, Any> = emptyMap()
) : Parcelable

@Parcelize
data class ExecutionLogDTO(
    val id: Long = 0,
    val macroId: Long,
    val macroName: String? = null,
    val executedAt: Long = System.currentTimeMillis(),
    val executionStatus: String,
    val errorMessage: String? = null,
    val executionDurationMs: Long = 0,
    val actions: List<ActionDTO> = emptyList()
) : Parcelable

@Parcelize
data class VariableDTO(
    val id: Long = 0,
    val name: String,
    val value: String,
    val scope: String, // "LOCAL" or "GLOBAL"
    val macroId: Long? = null,
    val type: String = "STRING" // "STRING", "NUMBER", "BOOLEAN"
) : Parcelable

