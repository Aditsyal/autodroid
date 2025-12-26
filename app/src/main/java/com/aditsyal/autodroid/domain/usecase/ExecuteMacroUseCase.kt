package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import kotlinx.coroutines.delay
import timber.log.Timber
import javax.inject.Inject

class ExecuteMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val evaluateConstraintsUseCase: EvaluateConstraintsUseCase,
    private val executeActionUseCase: ExecuteActionUseCase
) {
    suspend operator fun invoke(macroId: Long, isDryRun: Boolean = false): ExecutionResult {
        val macro = repository.getMacroById(macroId) ?: return ExecutionResult.NotFound
        val startTime = System.currentTimeMillis()

        return runCatching {
            // 1. Evaluate constraints
            if (!evaluateConstraintsUseCase(macro.constraints)) {
                Timber.d("Macro $macroId skipped: Constraints not satisfied")
                return ExecutionResult.Skipped("Constraints not satisfied")
            }

            // 2. Execute actions sequentially
            if (!isDryRun) {
                repository.updateExecutionInfo(macro.id, startTime)
                
                macro.actions.sortedBy { it.executionOrder }.forEach { action ->
                    executeActionUseCase(action, macro.id)
                    if (action.delayAfter > 0) {
                        kotlinx.coroutines.delay(action.delayAfter)
                    }
                }
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
        data class Skipped(val reason: String) : ExecutionResult()
    }
}


