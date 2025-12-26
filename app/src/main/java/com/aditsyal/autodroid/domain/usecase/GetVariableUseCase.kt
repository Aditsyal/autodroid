package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import com.aditsyal.autodroid.data.models.VariableDTO
import timber.log.Timber
import javax.inject.Inject

/**
 * Get a variable by name, checking local scope first, then global scope
 */
class GetVariableUseCase @Inject constructor(
    private val variableDao: VariableDao
) {
    suspend operator fun invoke(name: String, macroId: Long?): VariableDTO? {
        return try {
            // Try local variable first if macroId is provided
            val localVar = macroId?.let {
                variableDao.getVariable(name, "LOCAL", it)
            }
            
            if (localVar != null) {
                localVar.toDTO()
            } else {
                // Fall back to global variable
                val globalVar = variableDao.getVariable(name, "GLOBAL", null)
                globalVar?.toDTO()
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get variable: $name")
            null
        }
    }
}

private fun VariableEntity.toDTO(): VariableDTO {
    return VariableDTO(
        id = id,
        name = name,
        value = value,
        scope = scope,
        macroId = macroId,
        type = type
    )
}

