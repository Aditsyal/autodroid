package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.VariableDTO
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import timber.log.Timber
import javax.inject.Inject

/**
 * Evaluates logic conditions for if/else and loop control
 */
class EvaluateLogicUseCase @Inject constructor(
    private val getVariableUseCase: GetVariableUseCase
) {
    private val gson = Gson()

    /**
     * Evaluate a condition and return true/false
     * @param conditionConfig Map containing: leftOperand, operator, rightOperand, variableName (optional)
     */
    suspend fun evaluateCondition(
        conditionConfig: Map<String, Any>,
        macroId: Long?
    ): Boolean {
        return try {
            val leftOperand = conditionConfig["leftOperand"]?.toString() ?: return false
            val operator = conditionConfig["operator"]?.toString() ?: "=="
            val rightOperand = conditionConfig["rightOperand"]?.toString() ?: return false
            val useVariables = conditionConfig["useVariables"] as? Boolean ?: false

            // Resolve variable placeholders if needed
            val leftValue = if (useVariables && leftOperand.startsWith("{") && leftOperand.endsWith("}")) {
                val varName = leftOperand.removeSurrounding("{", "}")
                getVariableUseCase(varName, macroId)?.value ?: leftOperand
            } else {
                leftOperand
            }

            val rightValue = if (useVariables && rightOperand.startsWith("{") && rightOperand.endsWith("}")) {
                val varName = rightOperand.removeSurrounding("{", "}")
                getVariableUseCase(varName, macroId)?.value ?: rightOperand
            } else {
                rightOperand
            }

            evaluateComparison(leftValue, operator, rightValue)
        } catch (e: Exception) {
            Timber.e(e, "Failed to evaluate condition")
            false
        }
    }

    /**
     * Evaluate multiple conditions with logical operators (AND, OR, NOT)
     */
    suspend fun evaluateConditions(
        conditions: List<Map<String, Any>>,
        logicalOperator: String, // "AND" or "OR"
        macroId: Long?
    ): Boolean {
        if (conditions.isEmpty()) return true

        return when (logicalOperator.uppercase()) {
            "AND" -> conditions.all { evaluateCondition(it, macroId) }
            "OR" -> conditions.any { evaluateCondition(it, macroId) }
            else -> {
                Timber.w("Unknown logical operator: $logicalOperator, defaulting to AND")
                conditions.all { evaluateCondition(it, macroId) }
            }
        }
    }

    private fun evaluateComparison(left: String, operator: String, right: String): Boolean {
        return try {
            when (operator) {
                "==", "equals" -> left == right
                "!=", "not_equals" -> left != right
                ">", "greater_than" -> {
                    val leftNum = left.toDoubleOrNull()
                    val rightNum = right.toDoubleOrNull()
                    if (leftNum != null && rightNum != null) leftNum > rightNum else false
                }
                "<", "less_than" -> {
                    val leftNum = left.toDoubleOrNull()
                    val rightNum = right.toDoubleOrNull()
                    if (leftNum != null && rightNum != null) leftNum < rightNum else false
                }
                ">=", "greater_than_or_equal" -> {
                    val leftNum = left.toDoubleOrNull()
                    val rightNum = right.toDoubleOrNull()
                    if (leftNum != null && rightNum != null) leftNum >= rightNum else false
                }
                "<=", "less_than_or_equal" -> {
                    val leftNum = left.toDoubleOrNull()
                    val rightNum = right.toDoubleOrNull()
                    if (leftNum != null && rightNum != null) leftNum <= rightNum else false
                }
                "contains" -> left.contains(right, ignoreCase = true)
                "starts_with" -> left.startsWith(right, ignoreCase = true)
                "ends_with" -> left.endsWith(right, ignoreCase = true)
                else -> {
                    Timber.w("Unknown operator: $operator, defaulting to equals")
                    left == right
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error comparing values: $left $operator $right")
            false
        }
    }
}

