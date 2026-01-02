package com.aditsyal.autodroid.domain.usecase

import android.content.Context
import com.aditsyal.autodroid.data.local.dao.VariableDao
import com.aditsyal.autodroid.data.models.VariableDTO
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

/**
 * Evaluates variable expressions like ${variable_name} in action configurations
 */
class VariableInterpolator @Inject constructor(
    @ApplicationContext private val context: Context,
    private val variableDao: VariableDao
) {

    /**
     * Interpolates variables in a string, replacing ${variable_name} with actual values
     */
    suspend fun interpolate(
        text: String,
        macroId: Long? = null,
        scope: String = "GLOBAL"
    ): String {
        if (!text.contains("$")) {
            return text
        }

        var result = text

        // Find all ${variable_name} patterns
        val pattern = Regex("\\$\\{([^}]+)\\}")
        val matches = pattern.findAll(text)

        for (match in matches) {
            val variableName = match.groupValues[1]
            val replacement = getVariableValue(variableName, scope, macroId)
            result = result.replace(match.value, replacement)
        }

        return result
    }

    /**
     * Interpolates variables in a map of configurations
     */
    suspend fun interpolateConfig(
        config: Map<String, Any>,
        macroId: Long? = null,
        scope: String = "GLOBAL"
    ): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        for ((key, value) in config) {
            result[key] = when (value) {
                is String -> interpolate(value, macroId, scope)
                is Map<*, *> -> @Suppress("UNCHECKED_CAST") interpolateConfig(value as Map<String, Any>, macroId, scope)
                is List<*> -> value.map { item ->
                    when (item) {
                        is String -> interpolate(item, macroId, scope)
                        is Map<*, *> -> @Suppress("UNCHECKED_CAST") interpolateConfig(item as Map<String, Any>, macroId, scope)
                        else -> item
                    }
                }
                else -> value
            }
        }

        return result
    }

    private suspend fun getVariableValue(
        variableName: String,
        scope: String,
        macroId: Long?
    ): String {
        return try {
            val variable = variableDao.getVariable(variableName, scope, macroId)
            variable?.value ?: ""
        } catch (e: Exception) {
            Timber.e(e, "Failed to get variable value for $variableName")
            ""
        }
    }
}