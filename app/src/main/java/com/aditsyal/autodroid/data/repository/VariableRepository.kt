package com.aditsyal.autodroid.data.repository

import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import com.aditsyal.autodroid.data.models.VariableDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariableRepository @Inject constructor(
    private val variableDao: VariableDao
) {

    fun getAllVariables(): Flow<List<VariableDTO>> = variableDao.getAllVariables()
        .map { entities -> entities.map { it.toDTO() } }

    fun getGlobalVariables(): Flow<List<VariableDTO>> = variableDao.getAllGlobalVariables()
        .map { entities -> entities.map { it.toDTO() } }

    fun getVariablesByMacroId(macroId: Long): Flow<List<VariableDTO>> = variableDao.getVariablesByMacroId(macroId)
        .map { entities -> entities.map { it.toDTO() } }

    suspend fun getVariable(id: Long): VariableDTO? = variableDao.getVariableById(id)?.toDTO()

    suspend fun getVariable(name: String, scope: String, macroId: Long?): VariableDTO? = variableDao.getVariable(name, scope, macroId)?.toDTO()

    suspend fun createVariable(variable: VariableDTO): Long {
        val entity = variable.toEntity()
        return variableDao.insertVariable(entity)
    }

    suspend fun updateVariable(variable: VariableDTO) {
        val entity = variable.toEntity().copy(updatedAt = System.currentTimeMillis())
        variableDao.updateVariable(entity)
    }

    suspend fun deleteVariable(id: Long) {
        val variable = variableDao.getVariableById(id) ?: return
        variableDao.deleteVariable(variable)
    }

    suspend fun deleteVariablesByMacroId(macroId: Long) {
        variableDao.deleteVariablesByMacroId(macroId)
    }

    suspend fun deleteGlobalVariable(name: String) {
        variableDao.deleteGlobalVariable(name)
    }

    suspend fun setVariableValue(
        name: String,
        value: String,
        scope: String = "GLOBAL",
        macroId: Long? = null,
        type: String = "STRING"
    ): Long {
        val existing = variableDao.getVariable(name, scope, macroId)

        return if (existing != null) {
            val updated = existing.copy(
                value = value,
                type = type,
                updatedAt = System.currentTimeMillis()
            )
            variableDao.updateVariable(updated)
            existing.id
        } else {
            val newVariable = VariableEntity(
                name = name,
                value = value,
                scope = scope,
                macroId = macroId,
                type = type,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            variableDao.insertVariable(newVariable)
        }
    }

    suspend fun incrementVariable(
        name: String,
        amount: Int = 1,
        scope: String = "GLOBAL",
        macroId: Long? = null
    ): Int {
        val variable = variableDao.getVariable(name, scope, macroId)
            ?: throw IllegalArgumentException("Variable '$name' not found")

        val currentValue = variable.value.toIntOrNull() ?: 0
        val newValue = currentValue + amount
        val updated = variable.copy(
            value = newValue.toString(),
            type = "NUMBER",
            updatedAt = System.currentTimeMillis()
        )

        variableDao.updateVariable(updated)
        return newValue
    }

    suspend fun decrementVariable(
        name: String,
        amount: Int = 1,
        scope: String = "GLOBAL",
        macroId: Long? = null
    ): Int = incrementVariable(name, -amount, scope, macroId)

    suspend fun appendToVariable(
        name: String,
        text: String,
        scope: String = "GLOBAL",
        macroId: Long? = null
    ): String {
        val variable = variableDao.getVariable(name, scope, macroId)
            ?: throw IllegalArgumentException("Variable '$name' not found")

        val newValue = variable.value + text
        val updated = variable.copy(
            value = newValue,
            updatedAt = System.currentTimeMillis()
        )

        variableDao.updateVariable(updated)
        return newValue
    }

    suspend fun performArithmeticOperation(
        name: String,
        operation: String,
        operand: Int,
        scope: String = "GLOBAL",
        macroId: Long? = null
    ): Int {
        val variable = variableDao.getVariable(name, scope, macroId)
            ?: throw IllegalArgumentException("Variable '$name' not found")

        val currentValue = variable.value.toIntOrNull() ?: 0
        val newValue = when (operation.uppercase()) {
            "ADD" -> currentValue + operand
            "SUBTRACT" -> currentValue - operand
            "MULTIPLY" -> currentValue * operand
            "DIVIDE" -> if (operand != 0) currentValue / operand else 0
            "MODULO" -> if (operand != 0) currentValue % operand else 0
            else -> throw IllegalArgumentException("Unknown operation: $operation")
        }

        val updated = variable.copy(
            value = newValue.toString(),
            type = "NUMBER",
            updatedAt = System.currentTimeMillis()
        )

        variableDao.updateVariable(updated)
        return newValue
    }
}

private fun VariableEntity.toDTO(): VariableDTO = VariableDTO(
    id = id,
    name = name,
    value = value,
    scope = scope,
    macroId = macroId,
    type = type
)

private fun VariableDTO.toEntity(): VariableEntity = VariableEntity(
    id = id,
    name = name,
    value = value,
    scope = scope,
    macroId = macroId,
    type = type
)