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
        val startTime = System.currentTimeMillis()
        Timber.i("🔄 Starting macro execution: macroId=$macroId, dryRun=$isDryRun")

        val macro = repository.getMacroById(macroId)
        if (macro == null) {
            Timber.w("❌ Macro $macroId not found")
            return ExecutionResult.NotFound
        }

        Timber.i("📋 Macro details: name='${macro.name}', enabled=${macro.enabled}, triggers=${macro.triggers.size}, actions=${macro.actions.size}, constraints=${macro.constraints.size}")

        return runCatching {
            // 1. Evaluate constraints
            Timber.d("🔍 Evaluating ${macro.constraints.size} constraints...")
            val constraintsSatisfied = evaluateConstraintsUseCase(macro.constraints)
            if (!constraintsSatisfied) {
                Timber.i("⏭️ Macro $macroId skipped: Constraints not satisfied")
                repository.logExecution(
                    ExecutionLogDTO(
                        macroId = macro.id,
                        executedAt = startTime,
                        executionStatus = "SKIPPED_CONSTRAINTS",
                        errorMessage = "Constraints not satisfied",
                        executionDurationMs = System.currentTimeMillis() - startTime
                    )
                )
                return ExecutionResult.Skipped("Constraints not satisfied")
            }
            Timber.i("✅ All constraints satisfied")

            // 2. Execute actions sequentially
            if (!isDryRun) {
                Timber.d("⚡ Executing ${macro.actions.size} actions...")
                repository.updateExecutionInfo(macro.id, startTime)

                macro.actions.sortedBy { it.executionOrder }.forEachIndexed { index, action ->
                    Timber.d("🎯 Executing action ${index + 1}/${macro.actions.size}: type=${action.actionType}, config=${action.actionConfig}")
                    try {
                        executeActionUseCase(action)
                        Timber.i("✅ Action ${index + 1} completed: ${action.actionType}")
                    } catch (e: Exception) {
                        Timber.e(e, "❌ Action ${index + 1} failed: ${action.actionType}")
                        throw e // Re-throw to be caught by outer catch block
                    }

                    if (action.delayAfter > 0) {
                        Timber.d("⏳ Delaying for ${action.delayAfter}ms after action ${index + 1}")
                        kotlinx.coroutines.delay(action.delayAfter)
                    }
                }
                Timber.i("🎉 All actions completed successfully")
            } else {
                Timber.i("🔍 Dry run completed - no actions executed")
            }

            val finalStatus = if (isDryRun) "SIMULATION_SUCCESS" else "SUCCESS"
            repository.logExecution(
                ExecutionLogDTO(
                    macroId = macro.id,
                    executedAt = startTime,
                    executionStatus = finalStatus,
                    executionDurationMs = System.currentTimeMillis() - startTime
                )
            )
            Timber.i("🏁 Macro execution completed: $finalStatus in ${System.currentTimeMillis() - startTime}ms")
            ExecutionResult.Success
        }.getOrElse { throwable ->
            val errorMsg = throwable.message ?: "Unknown error"
            Timber.e(throwable, "💥 Macro execution failed: $errorMsg")

            repository.logExecution(
                ExecutionLogDTO(
                    macroId = macro.id,
                    executedAt = startTime,
                    executionStatus = if (isDryRun) "SIMULATION_FAILURE" else "FAILURE",
                    errorMessage = errorMsg,
                    executionDurationMs = System.currentTimeMillis() - startTime
                )
            )
            ExecutionResult.Failure(errorMsg)
        }
    }

    sealed class ExecutionResult {
        object Success : ExecutionResult()
        data class Failure(val reason: String?) : ExecutionResult()
        object NotFound : ExecutionResult()
        data class Skipped(val reason: String) : ExecutionResult()
    }
}


