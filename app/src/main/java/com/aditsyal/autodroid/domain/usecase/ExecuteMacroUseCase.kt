package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import javax.inject.Inject

class ExecuteMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(macroId: Long, isDryRun: Boolean = false): ExecutionResult {
        val macro = repository.getMacroById(macroId) ?: return ExecutionResult.NotFound
        val startTime = System.currentTimeMillis()

        return runCatching {
            // TODO: Replace with real trigger/action/constraint execution once implemented.
            if (!isDryRun) {
                repository.updateExecutionInfo(macro.id, startTime)
            }
            
            repository.logExecution(
                ExecutionLogDTO(
                    macroId = macro.id,
                    executedAt = startTime,
                    executionStatus = if (isDryRun) "SIMULATION_SUCCESS" else "SUCCESS",
                    executionDurationMs = System.currentTimeMillis() - startTime
                )
            )
            ExecutionResult.Success
        }.getOrElse { throwable ->
            repository.logExecution(
                ExecutionLogDTO(
                    macroId = macro.id,
                    executedAt = startTime,
                    executionStatus = if (isDryRun) "SIMULATION_FAILURE" else "FAILURE",
                    errorMessage = throwable.message,
                    executionDurationMs = System.currentTimeMillis() - startTime
                )
            )
            ExecutionResult.Failure(throwable.message)
        }
    }

    sealed class ExecutionResult {
        object Success : ExecutionResult()
        data class Failure(val reason: String?) : ExecutionResult()
        object NotFound : ExecutionResult()
    }
}


