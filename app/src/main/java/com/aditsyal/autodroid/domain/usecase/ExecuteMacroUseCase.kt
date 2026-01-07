package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.utils.PerformanceMonitor
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import timber.log.Timber
import javax.inject.Inject

class ExecuteMacroUseCase @Inject constructor(
    private val repository: MacroRepository,
    private val evaluateConstraintsUseCase: EvaluateConstraintsUseCase,
    private val executeActionUseCase: ExecuteActionUseCase,
    private val evaluateLogicUseCase: EvaluateLogicUseCase,
    private val performanceMonitor: PerformanceMonitor
) {
    suspend operator fun invoke(macroId: Long, isDryRun: Boolean = false): ExecutionResult {
        val executionId = performanceMonitor.startExecution("Macro_$macroId")
        return try {
            withTimeout(60_000L) { // 60 second timeout for the entire macro execution
                val macro = repository.getMacroById(macroId) ?: return@withTimeout ExecutionResult.NotFound
                val startTime = System.currentTimeMillis()
                
                performanceMonitor.checkpoint(executionId, "Macro_Fetched")

                runCatching {
                    // 1. Evaluate constraints
                    if (!evaluateConstraintsUseCase(macro.constraints)) {
                        Timber.d("Macro $macroId skipped: Constraints not satisfied")
                        performanceMonitor.checkpoint(executionId, "Constraints_Skipped")
                        return@runCatching ExecutionResult.Skipped("Constraints not satisfied")
                    }
                    
                    performanceMonitor.checkpoint(executionId, "Constraints_Passed")

                    // 2. Execute actions sequentially with logic control
                    if (!isDryRun) {
                        repository.updateExecutionInfo(macro.id, startTime)
                        
                        executeActionsWithLogic(macro.actions.sortedBy { it.executionOrder }, macro.id, executionId)
                    }
                    
                    performanceMonitor.checkpoint(executionId, "Actions_Completed")
                    
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
                    performanceMonitor.checkpoint(executionId, "Execution_Error")
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
        } catch (e: TimeoutCancellationException) {
            performanceMonitor.checkpoint(executionId, "Execution_Timeout")
            Timber.e("Macro $macroId execution timed out")
            ExecutionResult.Failure("Execution timed out after 60 seconds")
        } finally {
            performanceMonitor.endExecution(executionId, "Macro_$macroId")
        }
    }

    /**
     * Execute actions with logic control (if/else, loops)
     */
    private suspend fun executeActionsWithLogic(
        actions: List<com.aditsyal.autodroid.data.models.ActionDTO>, 
        macroId: Long,
        executionId: String
    ) {
        var actionIndex = 0
        val actionStack = mutableListOf<LoopContext>()
        
        while (actionIndex < actions.size) {
            val action = actions[actionIndex]
            performanceMonitor.checkpoint(executionId, "Action_${actionIndex}_Start_${action.actionType}")
            
            try {
                when (action.actionType) {
                    "IF_CONDITION" -> {
                        val condition = action.actionConfig["condition"] as? Map<*, *>
                        val conditionMet = if (condition != null) {
                            @Suppress("UNCHECKED_CAST")
                            evaluateLogicUseCase.evaluateCondition(condition as Map<String, Any>, macroId)
                        } else {
                            false
                        }
                        
                        val elseIndex = action.actionConfig["elseIndex"]?.toString()?.toIntOrNull()
                        val endIfIndex = action.actionConfig["endIfIndex"]?.toString()?.toIntOrNull()
                        
                        if (conditionMet) {
                            // Execute actions in if block, skip else
                            actionIndex++
                            if (elseIndex != null && endIfIndex != null) {
                                // Skip to endIf, skipping else block
                                while (actionIndex < actions.size && actionIndex < endIfIndex) {
                                    if (actionIndex == elseIndex) {
                                        // Skip else block
                                        actionIndex = endIfIndex
                                        break
                                    }
                                    executeActionIfNotLogic(actions[actionIndex], macroId)
                                    actionIndex++
                                }
                            }
                        } else {
                            // Skip if block, execute else if present
                            if (elseIndex != null && endIfIndex != null) {
                                actionIndex = elseIndex + 1
                                while (actionIndex < actions.size && actionIndex < endIfIndex) {
                                    executeActionIfNotLogic(actions[actionIndex], macroId)
                                    actionIndex++
                                }
                            } else {
                                // No else block, skip to endIf
                                actionIndex = (endIfIndex ?: (actionIndex + 1))
                            }
                        }
                    }
                    "WHILE_LOOP" -> {
                        val condition = action.actionConfig["condition"] as? Map<*, *>
                        val loopStartIndex = actionIndex
                        val endWhileIndex = action.actionConfig["endWhileIndex"]?.toString()?.toIntOrNull()
                        
                        if (condition != null) {
                            @Suppress("UNCHECKED_CAST")
                            val conditionMet = evaluateLogicUseCase.evaluateCondition(condition as Map<String, Any>, macroId)
                            
                            if (conditionMet) {
                                // Push loop context
                                actionStack.add(LoopContext(loopStartIndex, endWhileIndex ?: actions.size, "WHILE"))
                                actionIndex++
                                // Execute loop body
                                while (actionIndex < actions.size && actionIndex < (endWhileIndex ?: actions.size)) {
                                    executeActionIfNotLogic(actions[actionIndex], macroId)
                                    actionIndex++
                                }
                                // Check condition again
                                @Suppress("UNCHECKED_CAST")
                                if (evaluateLogicUseCase.evaluateCondition(condition as Map<String, Any>, macroId)) {
                                    // Loop again
                                    actionIndex = loopStartIndex + 1
                                } else {
                                    // Exit loop
                                    actionStack.removeLastOrNull()
                                    actionIndex = (endWhileIndex ?: actions.size)
                                }
                            } else {
                                // Skip loop body
                                actionIndex = (endWhileIndex ?: actions.size)
                            }
                        } else {
                            actionIndex++
                        }
                    }
                    "FOR_LOOP" -> {
                        val iterations = action.actionConfig["iterations"]?.toString()?.toIntOrNull() ?: 0
                        val loopStartIndex = actionIndex
                        val endForIndex = action.actionConfig["endForIndex"]?.toString()?.toIntOrNull()
                        val loopVariable = action.actionConfig["loopVariable"]?.toString()
                        
                        actionStack.add(LoopContext(loopStartIndex, endForIndex ?: actions.size, "FOR", iterations))
                        
                        for (i in 0 until iterations) {
                            // Set loop variable if specified
                             if (loopVariable != null) {
                                 val result = executeActionUseCase(
                                     com.aditsyal.autodroid.data.models.ActionDTO(
                                         actionType = "SET_VARIABLE",
                                         actionConfig = mapOf(
                                             "variableName" to loopVariable,
                                             "value" to i.toString(),
                                             "scope" to "LOCAL"
                                         ),
                                         executionOrder = 0
                                     ),
                                     macroId
                                 )
                                 if (result.isFailure) {
                                     Timber.w("Failed to set loop variable: ${result.exceptionOrNull()?.message}")
                                 }
                             }
                            
                            // Execute loop body
                            var bodyIndex = loopStartIndex + 1
                            while (bodyIndex < actions.size && bodyIndex < (endForIndex ?: actions.size)) {
                                executeActionIfNotLogic(actions[bodyIndex], macroId)
                                bodyIndex++
                            }
                        }
                        
                        actionStack.removeLastOrNull()
                        actionIndex = (endForIndex ?: actions.size)
                    }
                    "BREAK" -> {
                        // Break out of current loop
                        val loopContext = actionStack.removeLastOrNull()
                        if (loopContext != null) {
                            actionIndex = loopContext.endIndex
                        } else {
                            actionIndex++
                        }
                    }
                    "CONTINUE" -> {
                        // Continue to next iteration of current loop
                        val loopContext = actionStack.lastOrNull()
                        if (loopContext != null) {
                            actionIndex = loopContext.startIndex + 1
                        } else {
                            actionIndex++
                        }
                    }
                     else -> {
                         val result = executeActionUseCase(action, macroId)
                         if (result.isSuccess) {
                             if (action.delayAfter > 0) {
                                 delay(action.delayAfter)
                             }
                             actionIndex++
                         } else {
                             Timber.w("Action ${action.actionType} failed: ${result.exceptionOrNull()?.message}")
                             // Skip failed action and continue with next
                             actionIndex++
                         }
                     }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error executing action ${action.actionType} at index $actionIndex")
                actionIndex++
            }
        }
    }
    
    private suspend fun executeActionIfNotLogic(action: com.aditsyal.autodroid.data.models.ActionDTO, macroId: Long) {
        if (action.actionType !in listOf("IF_CONDITION", "WHILE_LOOP", "FOR_LOOP", "BREAK", "CONTINUE", "END_IF", "END_WHILE", "END_FOR")) {
            val result = executeActionUseCase(action, macroId)
            if (result.isSuccess) {
                if (action.delayAfter > 0) {
                    delay(action.delayAfter)
                }
            } else {
                Timber.w("Action ${action.actionType} failed: ${result.exceptionOrNull()?.message}")
            }
        }
    }
    
    private data class LoopContext(
        val startIndex: Int,
        val endIndex: Int,
        val type: String,
        val iterations: Int = 0
    )

    sealed class ExecutionResult {
        object Success : ExecutionResult()
        data class Failure(val reason: String?) : ExecutionResult()
        object NotFound : ExecutionResult()
        data class Skipped(val reason: String) : ExecutionResult()
    }
}


