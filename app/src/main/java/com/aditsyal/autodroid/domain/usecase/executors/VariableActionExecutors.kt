package com.aditsyal.autodroid.domain.usecase.executors

import com.aditsyal.autodroid.data.repository.VariableRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class SetVariableExecutor @Inject constructor(
    private val variableRepository: VariableRepository,
    @ApplicationContext private val context: android.content.Context
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val variableName = config["variableName"]?.toString()
                ?: throw IllegalArgumentException("variableName is required")
            val value = config["value"]?.toString()
                ?: throw IllegalArgumentException("value is required")
            val scope = config["scope"]?.toString() ?: "GLOBAL"
            val macroId = config["macroId"]?.toString()?.toLongOrNull()
            val type = config["type"]?.toString() ?: "STRING"

            variableRepository.setVariableValue(
                name = variableName,
                value = value,
                scope = scope,
                macroId = macroId,
                type = type
            )

            Timber.i("Variable set: $variableName = $value (scope: $scope)")
        }
    }
}

class IncrementVariableExecutor @Inject constructor(
    private val variableRepository: VariableRepository
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val variableName = config["variableName"]?.toString()
                ?: throw IllegalArgumentException("variableName is required")
            val amount = config["amount"]?.toString()?.toIntOrNull() ?: 1
            val scope = config["scope"]?.toString() ?: "GLOBAL"
            val macroId = config["macroId"]?.toString()?.toLongOrNull()

            val newValue = variableRepository.incrementVariable(
                name = variableName,
                amount = amount,
                scope = scope,
                macroId = macroId
            )

            Timber.i("Variable incremented: $variableName += $amount (new value: $newValue)")
        }
    }
}

class DecrementVariableExecutor @Inject constructor(
    private val variableRepository: VariableRepository
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val variableName = config["variableName"]?.toString()
                ?: throw IllegalArgumentException("variableName is required")
            val amount = config["amount"]?.toString()?.toIntOrNull() ?: 1
            val scope = config["scope"]?.toString() ?: "GLOBAL"
            val macroId = config["macroId"]?.toString()?.toLongOrNull()

            val newValue = variableRepository.decrementVariable(
                name = variableName,
                amount = amount,
                scope = scope,
                macroId = macroId
            )

            Timber.i("Variable decremented: $variableName -= $amount (new value: $newValue)")
        }
    }
}

class AppendVariableExecutor @Inject constructor(
    private val variableRepository: VariableRepository
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val variableName = config["variableName"]?.toString()
                ?: throw IllegalArgumentException("variableName is required")
            val text = config["text"]?.toString()
                ?: throw IllegalArgumentException("text is required")
            val scope = config["scope"]?.toString() ?: "GLOBAL"
            val macroId = config["macroId"]?.toString()?.toLongOrNull()

            val newValue = variableRepository.appendToVariable(
                name = variableName,
                text = text,
                scope = scope,
                macroId = macroId
            )

            Timber.i("Variable appended: $variableName += \"$text\" (new value: $newValue)")
        }
    }
}

class ArithmeticVariableExecutor @Inject constructor(
    private val variableRepository: VariableRepository
) : ActionExecutor {

    override suspend fun execute(config: Map<String, Any>): Result<Unit> {
        return runCatching {
            val variableName = config["variableName"]?.toString()
                ?: throw IllegalArgumentException("variableName is required")
            val operation = config["operation"]?.toString()
                ?: throw IllegalArgumentException("operation is required")
            val operand = config["operand"]?.toString()?.toIntOrNull()
                ?: throw IllegalArgumentException("operand is required")
            val scope = config["scope"]?.toString() ?: "GLOBAL"
            val macroId = config["macroId"]?.toString()?.toLongOrNull()

            val newValue = variableRepository.performArithmeticOperation(
                name = variableName,
                operation = operation,
                operand = operand,
                scope = scope,
                macroId = macroId
            )

            Timber.i("Variable arithmetic: $variableName $operation $operand = $newValue")
        }
    }
}