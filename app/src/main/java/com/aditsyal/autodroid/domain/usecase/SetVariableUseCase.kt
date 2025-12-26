package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import com.aditsyal.autodroid.data.models.VariableDTO
import timber.log.Timber
import javax.inject.Inject

/**
 * Set or update a variable value
 */
class SetVariableUseCase @Inject constructor(
    private val variableDao: VariableDao
) {
    suspend operator fun invoke(variable: VariableDTO) {
        try {
            val existing = variableDao.getVariable(variable.name, variable.scope, variable.macroId)
            
            if (existing != null) {
                // Update existing variable
                variableDao.updateVariable(
                    existing.copy(
                        value = variable.value,
                        type = variable.type,
                        updatedAt = System.currentTimeMillis()
                    )
                )
                Timber.d("Updated variable: ${variable.name} = ${variable.value}")
            } else {
                // Create new variable
                variableDao.insertVariable(
                    VariableEntity(
                        name = variable.name,
                        value = variable.value,
                        scope = variable.scope,
                        macroId = variable.macroId,
                        type = variable.type
                    )
                )
                Timber.d("Created variable: ${variable.name} = ${variable.value}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to set variable: ${variable.name}")
            throw e
        }
    }
}

