package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.VariableDTO
import timber.log.Timber
import javax.inject.Inject

/**
 * Evaluate variable operations and return result
 */
class EvaluateVariableUseCase @Inject constructor(
    private val getVariableUseCase: GetVariableUseCase
) {
    suspend operator fun invoke(
        variableName: String,
        operation: String?,
        operand: String?,
        macroId: Long?
    ): String? {
        return try {
            val variable = getVariableUseCase(variableName, macroId)
            if (variable == null) {
                Timber.w("Variable not found: $variableName")
                return null
            }
            
            when (operation?.uppercase()) {
                "SET" -> operand ?: variable.value
                "ADD", "+" -> performAdd(variable, operand)
                "SUBTRACT", "-" -> performSubtract(variable, operand)
                "MULTIPLY", "*" -> performMultiply(variable, operand)
                "DIVIDE", "/" -> performDivide(variable, operand)
                "APPEND" -> performAppend(variable, operand)
                "SUBSTRING" -> performSubstring(variable, operand)
                else -> variable.value
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to evaluate variable operation")
            null
        }
    }
    
    private suspend fun performAdd(variable: VariableDTO, operand: String?): String? {
        if (operand == null) return variable.value
        
        return when (variable.type) {
            "NUMBER" -> {
                val current = variable.value.toDoubleOrNull() ?: return variable.value
                val op = operand.toDoubleOrNull() ?: return variable.value
                (current + op).toString()
            }
            else -> variable.value + operand
        }
    }
    
    private suspend fun performSubtract(variable: VariableDTO, operand: String?): String? {
        if (operand == null) return variable.value
        
        val current = variable.value.toDoubleOrNull() ?: return variable.value
        val op = operand.toDoubleOrNull() ?: return variable.value
        return (current - op).toString()
    }
    
    private suspend fun performMultiply(variable: VariableDTO, operand: String?): String? {
        if (operand == null) return variable.value
        
        val current = variable.value.toDoubleOrNull() ?: return variable.value
        val op = operand.toDoubleOrNull() ?: return variable.value
        return (current * op).toString()
    }
    
    private suspend fun performDivide(variable: VariableDTO, operand: String?): String? {
        if (operand == null) return variable.value
        
        val current = variable.value.toDoubleOrNull() ?: return variable.value
        val op = operand.toDoubleOrNull() ?: return variable.value
        if (op == 0.0) {
            Timber.w("Division by zero")
            return variable.value
        }
        return (current / op).toString()
    }
    
    private suspend fun performAppend(variable: VariableDTO, operand: String?): String {
        return variable.value + (operand ?: "")
    }
    
    private suspend fun performSubstring(variable: VariableDTO, operand: String?): String {
        if (operand == null) return variable.value
        
        val parts = operand.split(",")
        if (parts.size == 2) {
            val start = parts[0].toIntOrNull() ?: 0
            val end = parts[1].toIntOrNull() ?: variable.value.length
            return variable.value.substring(start.coerceAtLeast(0), end.coerceAtMost(variable.value.length))
        }
        return variable.value
    }
}

