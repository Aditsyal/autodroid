package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.local.entities.VariableEntity
import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.utils.CacheManager
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
            val localVar: VariableDTO? = macroId?.let { id ->
                CacheManager.getSuspend<VariableDTO?>("VAR_LOCAL_${name}_${id}", 30_000L) {
                    variableDao.getVariable(name, "LOCAL", id)?.toDTO()
                }
            }

            if (localVar != null) {
                localVar
            } else {
                // Fall back to global variable
                CacheManager.getSuspend<VariableDTO?>("VAR_GLOBAL_${name}", 60_000L) {
                    variableDao.getVariable(name, "GLOBAL", null)?.toDTO()
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to get variable: $name")
            null
        }
    }
}

fun VariableEntity.toDTO(): VariableDTO {
    return VariableDTO(
        id = id,
        name = name,
        value = value,
        scope = scope,
        macroId = macroId,
        type = type
    )
}