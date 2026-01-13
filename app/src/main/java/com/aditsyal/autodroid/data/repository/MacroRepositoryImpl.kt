package com.aditsyal.autodroid.data.repository

import com.aditsyal.autodroid.data.local.dao.ActionDao
import com.aditsyal.autodroid.data.local.dao.ConstraintDao
import com.aditsyal.autodroid.data.local.dao.ExecutionLogDao
import com.aditsyal.autodroid.data.local.dao.MacroDao
import com.aditsyal.autodroid.data.local.dao.TriggerDao
import com.aditsyal.autodroid.data.local.entities.ActionEntity
import com.aditsyal.autodroid.data.local.entities.ConstraintEntity
import com.aditsyal.autodroid.data.local.entities.ExecutionLogEntity
import com.aditsyal.autodroid.data.local.entities.ExecutionLogWithMacro
import com.aditsyal.autodroid.data.local.entities.ExecutionLogWithMacroDetails
import com.aditsyal.autodroid.data.local.entities.MacroEntity
import com.aditsyal.autodroid.data.local.entities.MacroWithDetails
import com.aditsyal.autodroid.data.local.entities.TriggerEntity
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.data.local.database.AutomationDatabase
import androidx.room.withTransaction
import com.google.gson.Gson
import com.aditsyal.autodroid.data.models.ConflictDTO
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class MacroRepositoryImpl @Inject constructor(
    private val database: AutomationDatabase,
    private val macroDao: MacroDao,
    private val triggerDao: TriggerDao,
    private val actionDao: ActionDao,
    private val constraintDao: ConstraintDao,
    private val executionLogDao: ExecutionLogDao
) : MacroRepository {

    private val gson = Gson()

    override fun getAllMacros(): Flow<List<MacroDTO>> =
        macroDao.getAllMacrosWithDetails().map { macros ->
            macros.map { it.toDTO() }
        }

    override suspend fun getMacroById(macroId: Long): MacroDTO? =
        macroDao.getMacroWithDetailsById(macroId)?.toDTO()

    override suspend fun createMacro(macro: MacroDTO): Long {
        return database.withTransaction {
            // Ensure macro ID is 0 for new macro
            val macroEntity = macro.toEntity().copy(id = 0)
            val macroId = macroDao.insertMacro(macroEntity)

            // Validate macroId is valid before inserting related entities
            if (macroId <= 0) {
                throw IllegalStateException("Failed to create macro: invalid macro ID")
            }

            // Insert related entities
            macro.triggers.forEach { trigger ->
                triggerDao.insertTrigger(trigger.copy(id = 0).toEntity(macroId))
            }
            macro.actions.forEach { action ->
                actionDao.insertAction(action.copy(id = 0).toEntity(macroId))
            }
            macro.constraints.forEach { constraint ->
                constraintDao.insertConstraint(constraint.copy(id = 0).toEntity(macroId))
            }
            macroId
        }
    }

    override suspend fun updateMacro(macro: MacroDTO) {
        database.withTransaction {
            val macroId = macro.id
            if (macroId <= 0) {
                throw IllegalArgumentException("Cannot update macro with invalid ID: $macroId")
            }

            // Verify macro exists before updating
            val existingMacro = macroDao.getMacroById(macroId)
            if (existingMacro == null) {
                throw IllegalStateException("Macro with ID $macroId does not exist")
            }

            macroDao.updateMacro(macro.toEntity())

            // Sync triggers: Delete and re-insert for simplicity
            triggerDao.deleteTriggersByMacroId(macroId)
            macro.triggers.forEach { trigger ->
                triggerDao.insertTrigger(trigger.copy(id = 0).toEntity(macroId))
            }

            // Sync actions
            actionDao.deleteActionsByMacroId(macroId)
            macro.actions.forEach { action ->
                actionDao.insertAction(action.copy(id = 0).toEntity(macroId))
            }

            // Sync constraints
            constraintDao.deleteConstraintsByMacroId(macroId)
            macro.constraints.forEach { constraint ->
                constraintDao.insertConstraint(constraint.copy(id = 0).toEntity(macroId))
            }
        }
    }

    override suspend fun deleteMacro(macroId: Long) =
        macroDao.deleteMacroById(macroId)

    override suspend fun toggleMacro(macroId: Long, enabled: Boolean) =
        macroDao.toggleMacro(macroId, enabled)

    override suspend fun updateExecutionInfo(macroId: Long, timestamp: Long) =
        macroDao.updateExecutionInfo(macroId, timestamp)

    override suspend fun addTrigger(macroId: Long, trigger: TriggerDTO): Long {
        // Validate macro exists
        val macro = macroDao.getMacroById(macroId)
        if (macro == null) {
            throw IllegalStateException("Cannot add trigger: Macro with ID $macroId does not exist")
        }
        return triggerDao.insertTrigger(trigger.copy(id = 0).toEntity(macroId))
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

    override suspend fun getTriggerById(triggerId: Long): TriggerDTO? =
        triggerDao.getTriggerById(triggerId)?.toDTO()

    override suspend fun getEnabledTriggersByType(triggerType: String): List<TriggerDTO> =
        triggerDao.getEnabledTriggersByType(triggerType).map { it.toDTO() }

    override suspend fun getAllEnabledTriggers(): List<TriggerDTO> =
        triggerDao.getAllEnabledTriggers().map { it.toDTO() }

    override suspend fun addAction(macroId: Long, action: ActionDTO): Long {
        // Validate macro exists
        val macro = macroDao.getMacroById(macroId)
        if (macro == null) {
            throw IllegalStateException("Cannot add action: Macro with ID $macroId does not exist")
        }
        return actionDao.insertAction(action.copy(id = 0).toEntity(macroId))
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
        // Validate macro exists
        val macro = macroDao.getMacroById(macroId)
        if (macro == null) {
            throw IllegalStateException("Cannot add constraint: Macro with ID $macroId does not exist")
        }
        return constraintDao.insertConstraint(constraint.copy(id = 0).toEntity(macroId))
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
        // Validate macro exists before logging
        val macro = macroDao.getMacroById(log.macroId)
        if (macro == null) {
            throw IllegalStateException("Cannot log execution: Macro with ID ${log.macroId} does not exist")
        }
        executionLogDao.insertExecutionLog(log.toEntity())
    }

    override fun getExecutionLogs(macroId: Long): Flow<List<ExecutionLogDTO>> =
        executionLogDao.getExecutionLogsByMacroId(macroId).map { logs ->
            logs.map { it.toDTO() }
        }

    override fun getMacroConflicts(): Flow<List<ConflictDTO>> =
        macroDao.getAllMacros().map { macros ->
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

    override fun getAllExecutionLogs(): Flow<List<ExecutionLogDTO>> =
        executionLogDao.getAllExecutionLogsWithMacroDetails().map { logs ->
            logs.map { it.toDTO() }
        }

    override suspend fun getMacrosPaginated(limit: Int, offset: Int): List<MacroDTO> =
        macroDao.getMacrosPaginated(limit, offset).map { it.toDTO() }

    override suspend fun getMacroCount(): Int =
        macroDao.getMacroCount()

    // Mapper extensions
    private fun MacroWithDetails.toDTO() = macro.toDTO(
        triggers = triggers.map { it.toDTO() },
        actions = actions.map { it.toDTO() },
        constraints = constraints.map { it.toDTO() }
    )

    private fun ExecutionLogWithMacroDetails.toDTO() = log.toDTO().copy(
        macroName = macro?.name,
        actions = actions.map { it.toDTO() }
    )

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

