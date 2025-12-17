package com.aditsyal.autodroid.data.repository

import com.aditsyal.autodroid.data.local.dao.ActionDao
import com.aditsyal.autodroid.data.local.dao.ConstraintDao
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.data.local.entities.ActionEntity
import com.aditsyal.autodroid.data.local.entities.ConstraintEntity
import com.aditsyal.autodroid.data.local.entities.ExecutionLogEntity
import com.aditsyal.autodroid.data.local.entities.MacroEntity
import com.aditsyal.autodroid.data.local.entities.TriggerEntity
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.google.gson.Gson
import com.aditsyal.autodroid.data.models.ConflictDTO
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MacroRepositoryImpl @Inject constructor(
    private val macroDao: MacroDao,
    private val triggerDao: TriggerDao,
    private val actionDao: ActionDao,
    private val constraintDao: ConstraintDao,
    private val executionLogDao: ExecutionLogDao
) : MacroRepository {

    private val gson = Gson()

    override fun getAllMacros(): Flow<List<MacroDTO>> {
        return macroDao.getAllMacros().map { macros ->
            macros.map { macro ->
                val macroId = macro.id
                val triggers = triggerDao.getTriggersByMacroId(macroId).first()
                val actions = actionDao.getActionsByMacroId(macroId).first()
                val constraints = constraintDao.getConstraintsByMacroId(macroId).first()
                
                macro.toDTO(
                    triggers = triggers.map { it.toDTO() },
                    actions = actions.map { it.toDTO() },
                    constraints = constraints.map { it.toDTO() }
                )
            }
        }
    }

    override suspend fun getMacroById(macroId: Long): MacroDTO? {
        val macro = macroDao.getMacroById(macroId) ?: return null
        val triggers = triggerDao.getTriggersByMacroId(macroId).first()
        val actions = actionDao.getActionsByMacroId(macroId).first()
        val constraints = constraintDao.getConstraintsByMacroId(macroId).first()

        return macro.toDTO(
            triggers = triggers.map { it.toDTO() },
            actions = actions.map { it.toDTO() },
            constraints = constraints.map { it.toDTO() }
        )
    }

    override suspend fun createMacro(macro: MacroDTO): Long {
        val macroId = macroDao.insertMacro(macro.toEntity())
        
        // Insert related entities
        macro.triggers.forEach { trigger ->
            triggerDao.insertTrigger(trigger.toEntity(macroId))
        }
        macro.actions.forEach { action ->
            actionDao.insertAction(action.toEntity(macroId))
        }
        macro.constraints.forEach { constraint ->
            constraintDao.insertConstraint(constraint.toEntity(macroId))
        }
        
        return macroId
    }

    override suspend fun updateMacro(macro: MacroDTO) {
        macroDao.updateMacro(macro.toEntity())
    }

    override suspend fun deleteMacro(macroId: Long) {
        macroDao.deleteMacroById(macroId)
    }

    override suspend fun toggleMacro(macroId: Long, enabled: Boolean) {
        macroDao.toggleMacro(macroId, enabled)
    }

    override suspend fun updateExecutionInfo(macroId: Long, timestamp: Long) {
        macroDao.updateExecutionInfo(macroId, timestamp)
    }

    override suspend fun addTrigger(macroId: Long, trigger: TriggerDTO): Long {
        return triggerDao.insertTrigger(trigger.toEntity(macroId))
    }

    override suspend fun updateTrigger(trigger: TriggerDTO) {
        val entity = triggerDao.getTriggerById(trigger.id)
        entity?.let {
            triggerDao.updateTrigger(trigger.toEntity(it.macroId))
        }
    }

    override suspend fun deleteTrigger(triggerId: Long) {
        val entity = triggerDao.getTriggerById(triggerId)
        entity?.let { triggerDao.deleteTrigger(it) }
    }

    override suspend fun getEnabledTriggersByType(triggerType: String): List<TriggerDTO> {
        return triggerDao.getEnabledTriggersByType(triggerType).map { it.toDTO() }
    }

    override suspend fun addAction(macroId: Long, action: ActionDTO): Long {
        return actionDao.insertAction(action.toEntity(macroId))
    }

    override suspend fun updateAction(action: ActionDTO) {
        val entity = actionDao.getActionById(action.id)
        entity?.let {
            actionDao.updateAction(action.toEntity(it.macroId))
        }
    }

    override suspend fun deleteAction(actionId: Long) {
        val entity = actionDao.getActionById(actionId)
        entity?.let { actionDao.deleteAction(it) }
    }

    override suspend fun addConstraint(macroId: Long, constraint: ConstraintDTO): Long {
        return constraintDao.insertConstraint(constraint.toEntity(macroId))
    }

    override suspend fun updateConstraint(constraint: ConstraintDTO) {
        val entity = constraintDao.getConstraintById(constraint.id)
        entity?.let {
            constraintDao.updateConstraint(constraint.toEntity(it.macroId))
        }
    }

    override suspend fun deleteConstraint(constraintId: Long) {
        val entity = constraintDao.getConstraintById(constraintId)
        entity?.let { constraintDao.deleteConstraint(it) }
    }

    override suspend fun logExecution(log: ExecutionLogDTO) {
        executionLogDao.insertExecutionLog(log.toEntity())
    }

    override fun getExecutionLogs(macroId: Long): Flow<List<ExecutionLogDTO>> {
        return executionLogDao.getExecutionLogsByMacroId(macroId).map { logs ->
            logs.map { it.toDTO() }
        }
    }

    override fun getMacroConflicts(): Flow<List<ConflictDTO>> {
        return macroDao.getAllMacros().map { macros ->
            macros.groupBy { it.name }
                .filter { (_, list) -> list.size > 1 }
                .map { (name, list) ->
                    ConflictDTO(
                        macroId = list.first().id,
                        macroName = name,
                        duplicateCount = list.size
                    )
                }
        }
    }

    override fun getAllExecutionLogs(): Flow<List<ExecutionLogDTO>> {
        return executionLogDao.getAllExecutionLogs().map { logs ->
            logs.map { it.toDTO() }
        }
    }

    // Mapper extensions
    private fun MacroEntity.toDTO(
        triggers: List<TriggerDTO>,
        actions: List<ActionDTO>,
        constraints: List<ConstraintDTO>
    ) = MacroDTO(
        id = id,
        name = name,
        description = description,
        enabled = enabled,
        createdAt = createdAt,
        lastExecuted = lastExecuted,
        triggers = triggers,
        actions = actions,
        constraints = constraints
    )

    private fun MacroDTO.toEntity() = MacroEntity(
        id = id,
        name = name,
        description = description,
        enabled = enabled,
        createdAt = createdAt,
        lastExecuted = lastExecuted,
        executionCount = 0
    )

    private fun TriggerEntity.toDTO() = TriggerDTO(
        id = id,
        macroId = macroId,
        triggerType = triggerType,
        triggerConfig = parseJsonToMap(triggerConfig)
    )

    private fun TriggerDTO.toEntity(macroId: Long) = TriggerEntity(
        id = id,
        macroId = macroId,
        triggerType = triggerType,
        triggerConfig = gson.toJson(triggerConfig),
        enabled = true,
        createdAt = System.currentTimeMillis()
    )

    private fun ActionEntity.toDTO() = ActionDTO(
        id = id,
        actionType = actionType,
        actionConfig = parseJsonToMap(actionConfig),
        executionOrder = executionOrder,
        delayAfter = delayAfter
    )

    private fun ActionDTO.toEntity(macroId: Long) = ActionEntity(
        id = id,
        macroId = macroId,
        actionType = actionType,
        actionConfig = gson.toJson(actionConfig),
        executionOrder = executionOrder,
        delayAfter = delayAfter,
        enabled = true,
        createdAt = System.currentTimeMillis()
    )

    private fun ConstraintEntity.toDTO() = ConstraintDTO(
        id = id,
        constraintType = constraintType,
        constraintConfig = parseJsonToMap(constraintConfig)
    )

    private fun ConstraintDTO.toEntity(macroId: Long) = ConstraintEntity(
        id = id,
        macroId = macroId,
        constraintType = constraintType,
        constraintConfig = gson.toJson(constraintConfig),
        enabled = true,
        createdAt = System.currentTimeMillis()
    )

    private fun ExecutionLogEntity.toDTO() = ExecutionLogDTO(
        id = id,
        macroId = macroId,
        executedAt = executedAt,
        executionStatus = executionStatus,
        errorMessage = errorMessage,
        executionDurationMs = executionDurationMs
    )

    private fun ExecutionLogDTO.toEntity() = ExecutionLogEntity(
        id = id,
        macroId = macroId,
        executedAt = executedAt,
        executionStatus = executionStatus,
        errorMessage = errorMessage,
        executionDurationMs = executionDurationMs,
        actionsExecuted = 0
    )

    private fun parseJsonToMap(json: String): Map<String, Any> {
        return try {
            val type = object : TypeToken<Map<String, Any>>() {}.type
            gson.fromJson(json, type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

