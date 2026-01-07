package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.usecase.DeleteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.GetAllMacrosUseCase
import com.aditsyal.autodroid.domain.usecase.ToggleMacroUseCase
import com.aditsyal.autodroid.utils.PerformanceMonitor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MacroListViewModel @Inject constructor(
    private val getAllMacrosUseCase: GetAllMacrosUseCase,
    private val toggleMacroUseCase: ToggleMacroUseCase,
    private val deleteMacroUseCase: DeleteMacroUseCase,
    private val executeMacroUseCase: ExecuteMacroUseCase,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacroListUiState())
    val uiState: StateFlow<MacroListUiState> = _uiState.asStateFlow()

    init {
        observeMacros()
    }

    private fun observeMacros() {
        viewModelScope.launch {
            getAllMacrosUseCase()
                .onStart { _uiState.update { it.copy(isLoading = true, error = null) } }
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load macros"
                        )
                    }
                }
                .collect { macros ->
                    performanceMonitor.findActiveExecutionId("Render_MacroList")?.let { id ->
                        performanceMonitor.checkpoint(id, "Data_Loaded")
                    }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            macros = macros,
                            error = null
                        )
                    }
                }
        }
    }

    fun toggleMacro(macroId: Long, enabled: Boolean) {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    macros = currentState.macros.map { macro ->
                        if (macro.id == macroId) macro.copy(enabled = enabled)
                        else macro
                    },
                    isActionInFlight = true,
                    error = null,
                    lastActionMessage = null
                )
            }
            runCatching { toggleMacroUseCase(macroId, enabled) }
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isActionInFlight = false,
                            lastActionMessage = "Macro ${if (enabled) "enabled" else "disabled"}"
                        ) 
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { 
                        it.copy(
                            isActionInFlight = false,
                            error = throwable.message ?: "Unable to toggle macro"
                        ) 
                    }
                }
        }
    }

    fun deleteMacro(macroId: Long) {
        runAction(
            successMessage = "Macro deleted",
            errorMessage = "Unable to delete macro"
        ) {
            deleteMacroUseCase(macroId)
        }
    }

    fun executeMacro(macroId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInFlight = true, error = null, lastActionMessage = null) }
            val result = executeMacroUseCase(macroId)
            _uiState.update {
                when (result) {
                    ExecuteMacroUseCase.ExecutionResult.Success -> it.copy(
                        isActionInFlight = false,
                        lastActionMessage = "Macro executed"
                    )
                    is ExecuteMacroUseCase.ExecutionResult.Failure -> it.copy(
                        isActionInFlight = false,
                        error = result.reason ?: "Macro execution failed"
                    )
                    ExecuteMacroUseCase.ExecutionResult.NotFound -> it.copy(
                        isActionInFlight = false,
                        error = "Macro not found"
                    )
                    is ExecuteMacroUseCase.ExecutionResult.Skipped -> it.copy(
                        isActionInFlight = false,
                        lastActionMessage = "Macro skipped: ${result.reason}"
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, lastActionMessage = null) }
    }

    private fun runAction(
        successMessage: String,
        errorMessage: String,
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionInFlight = true, error = null, lastActionMessage = null) }
            runCatching { block() }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isActionInFlight = false,
                            lastActionMessage = successMessage
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isActionInFlight = false,
                            error = throwable.message ?: errorMessage
                        )
                    }
                }
        }
    }
}

data class MacroListUiState(
    val isLoading: Boolean = false,
    val isActionInFlight: Boolean = false,
    val macros: List<MacroDTO> = emptyList(),
    val error: String? = null,
    val lastActionMessage: String? = null
)


