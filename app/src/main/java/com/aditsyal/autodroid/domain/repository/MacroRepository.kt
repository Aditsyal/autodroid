package com.aditsyal.autodroid.domain.repository

import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import kotlinx.coroutines.flow.Flow

interface MacroRepository {
    fun getAllMacros(): Flow<List<MacroDTO>>
    suspend fun getMacroById(macroId: Long): MacroDTO?
    suspend fun createMacro(macro: MacroDTO): Long
    suspend fun updateMacro(macro: MacroDTO)
    suspend fun deleteMacro(macroId: Long)
    suspend fun toggleMacro(macroId: Long, enabled: Boolean)
    suspend fun updateExecutionInfo(macroId: Long, timestamp: Long)
    
    suspend fun addTrigger(macroId: Long, trigger: TriggerDTO): Long
    suspend fun updateTrigger(trigger: TriggerDTO)
    suspend fun deleteTrigger(triggerId: Long)
    suspend fun getEnabledTriggersByType(triggerType: String): List<TriggerDTO>
    
    suspend fun addAction(macroId: Long, action: ActionDTO): Long
    suspend fun updateAction(action: ActionDTO)
    suspend fun deleteAction(actionId: Long)
    
    suspend fun addConstraint(macroId: Long, constraint: ConstraintDTO): Long
    suspend fun updateConstraint(constraint: ConstraintDTO)
    suspend fun deleteConstraint(constraintId: Long)
    
    suspend fun logExecution(log: ExecutionLogDTO)
    fun getExecutionLogs(macroId: Long): Flow<List<ExecutionLogDTO>>
    fun getAllExecutionLogs(): Flow<List<ExecutionLogDTO>>
    fun getMacroConflicts(): Flow<List<com.aditsyal.autodroid.data.models.ConflictDTO>>
}

