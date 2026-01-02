package com.aditsyal.autodroid.domain.usecase.executors

import android.content.Context
import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.data.repository.VariableRepository
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class IfElseActionExecutor @Inject constructor(
    private val variableRepository: VariableRepository,
    private val executeActionUseCase: com.aditsyal.autodroid.domain.usecase.ExecuteActionUseCase,
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val condition = config["condition"] as? Map<*, *>
                ?: throw IllegalArgumentException("condition is required")
            val thenActions = config["thenActions"] as? List<Map<*, *>>
                ?: throw IllegalArgumentException("thenActions is required")
            val elseActions = config["elseActions"] as? List<Map<*, *>>

            val conditionMet = evaluateCondition(condition)

            val actionsToExecute = if (conditionMet) {
                Timber.d("Condition met, executing THEN branch")
                thenActions
            } else {
                if (elseActions != null && elseActions.isNotEmpty()) {
                    Timber.d("Condition not met, executing ELSE branch")
                    elseActions
                } else {
                    Timber.d("Condition not met, no ELSE branch, skipping")
                    return@runCatching
                }
            }

            for (actionConfig in actionsToExecute) {
                val action = com.aditsyal.autodroid.data.models.ActionDTO(
                    actionType = actionConfig["actionType"]?.toString() ?: "",
                    actionConfig = actionConfig["config"] as? Map<String, Any> ?: emptyMap(),
                    executionOrder = actionConfig["executionOrder"] as? Int ?: 0,
                    delayAfter = actionConfig["delayAfter"] as? Long ?: 0
                )

                executeActionUseCase(action, macroId = null)
                    .getOrThrow()

                if (action.delayAfter > 0) {
                    kotlinx.coroutines.delay(action.delayAfter)
                }
            }
        }
    }

    private suspend fun evaluateCondition(condition: Map<*, *>): Boolean {
        return try {
            val variableName = condition["variableName"]?.toString()
                ?: throw IllegalArgumentException("variableName is required")
            val operator = condition["operator"]?.toString()
                ?: throw IllegalArgumentException("operator is required")
            val value = condition["value"]?.toString()
                ?: throw IllegalArgumentException("value is required")
            val scope = condition["scope"]?.toString() ?: "GLOBAL"
            val macroId = condition["macroId"]?.toString()?.toLongOrNull()

            val variable: VariableDTO? = variableRepository.getVariable(
                name = variableName,
                scope = scope,
                macroId = macroId
            )

            val actualValue = variable?.value ?: ""

            return when (operator.uppercase()) {
                "EQUALS", "==" -> actualValue == value
                "NOT_EQUALS", "!=" -> actualValue != value
                "LESS_THAN", "<" -> {
                    val actualNum = actualValue.toIntOrNull()
                    val expectedNum = value.toIntOrNull()
                    if (actualNum != null && expectedNum != null) actualNum < expectedNum else false
                }
                "LESS_EQUAL", "<=" -> {
                    val actualNum = actualValue.toIntOrNull()
                    val expectedNum = value.toIntOrNull()
                    if (actualNum != null && expectedNum != null) actualNum <= expectedNum else false
                }
                "GREATER_THAN", ">" -> {
                    val actualNum = actualValue.toIntOrNull()
                    val expectedNum = value.toIntOrNull()
                    if (actualNum != null && expectedNum != null) actualNum > expectedNum else false
                }
                "GREATER_EQUAL", ">=" -> {
                    val actualNum = actualValue.toIntOrNull()
                    val expectedNum = value.toIntOrNull()
                    if (actualNum != null && expectedNum != null) actualNum >= expectedNum else false
                }
                "CONTAINS" -> actualValue.contains(value)
                "NOT_CONTAINS", "!contains" -> !actualValue.contains(value)
                "STARTS_WITH" -> actualValue.startsWith(value)
                "ENDS_WITH" -> actualValue.endsWith(value)
                "IS_EMPTY" -> actualValue.isEmpty()
                "IS_NOT_EMPTY", "!empty" -> actualValue.isNotEmpty()
                "IS_TRUE", "true" -> actualValue.toBooleanStrictOrNull() == true
                "IS_FALSE", "false" -> actualValue.toBooleanStrictOrNull() == false
                else -> {
                    Timber.w("Unknown operator: $operator")
                    false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error evaluating condition")
            false
        }
    }
}

class LoopActionExecutor @Inject constructor(
    private val variableRepository: VariableRepository,
    private val executeActionUseCase: com.aditsyal.autodroid.domain.usecase.ExecuteActionUseCase,
    @ApplicationContext private val context: Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val loopType = config["loopType"]?.toString() ?: throw IllegalArgumentException("loopType is required")
            val variableName = config["variableName"]?.toString() ?: throw IllegalArgumentException("variableName is required")
            val actions = config["actions"] as? List<Map<*, *>>
                ?: throw IllegalArgumentException("actions is required")
            val scope = config["scope"]?.toString() ?: "GLOBAL"
            val macroId = config["macroId"]?.toString()?.toLongOrNull()

            when (loopType.uppercase()) {
                "FOR" -> executeForLoop(variableName, actions, scope, macroId)
                "WHILE" -> executeWhileLoop(variableName, actions, scope, macroId)
                else -> throw IllegalArgumentException("Unknown loop type: $loopType")
            }
        }
    }

    private suspend fun executeForLoop(
        variableName: String,
        actions: List<Map<*, *>>,
        scope: String,
        macroId: Long?
    ) {
        val variable: com.aditsyal.autodroid.data.models.VariableDTO? = variableRepository.getVariable(
            name = variableName,
            scope = scope,
            macroId = macroId
        ) ?: throw IllegalArgumentException("Loop variable '$variableName' not found")

        val iterations = variable?.value?.toIntOrNull() ?: throw IllegalArgumentException("Loop variable must be a number")

        Timber.d("Executing FOR loop: $iterations iterations")

        for (i in 0 until iterations) {
            for (actionConfig in actions) {
                val action = com.aditsyal.autodroid.data.models.ActionDTO(
                    actionType = actionConfig["actionType"]?.toString() ?: "",
                    actionConfig = actionConfig["config"] as? Map<String, Any> ?: emptyMap(),
                    executionOrder = actionConfig["executionOrder"] as? Int ?: 0,
                    delayAfter = actionConfig["delayAfter"] as? Long ?: 0
                )

                executeActionUseCase(action, macroId = macroId).getOrThrow()

                if (action.delayAfter > 0) {
                    kotlinx.coroutines.delay(action.delayAfter)
                }
            }
        }
    }

    private suspend fun executeWhileLoop(
        variableName: String,
        actions: List<Map<*, *>>,
        scope: String,
        macroId: Long?
    ) {
        val iterations = 0
        val maxIterations = 1000

        Timber.d("Executing WHILE loop (max $maxIterations iterations)")

        while (iterations < maxIterations) {
            val variable: com.aditsyal.autodroid.data.models.VariableDTO? = variableRepository.getVariable(
                name = variableName,
                scope = scope,
                macroId = macroId
            ) ?: break

            val shouldContinue = variable?.value?.toBooleanStrictOrNull() ?: false

            if (!shouldContinue) {
                Timber.d("WHILE loop condition false, exiting after $iterations iterations")
                break
            }

            for (actionConfig in actions) {
                val action = com.aditsyal.autodroid.data.models.ActionDTO(
                    actionType = actionConfig["actionType"]?.toString() ?: "",
                    actionConfig = actionConfig["config"] as? Map<String, Any> ?: emptyMap(),
                    executionOrder = actionConfig["executionOrder"] as? Int ?: 0,
                    delayAfter = actionConfig["delayAfter"] as? Long ?: 0
                )

                executeActionUseCase(action, macroId = macroId).getOrThrow()

                if (action.delayAfter > 0) {
                    kotlinx.coroutines.delay(action.delayAfter)
                }
            }
        }
    }
}