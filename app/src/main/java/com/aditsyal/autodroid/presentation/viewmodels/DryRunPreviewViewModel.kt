package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.DryRunUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DryRunPreviewViewModel @Inject constructor(
    private val dryRunUseCase: DryRunUseCase,
    private val macroRepository: MacroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DryRunPreviewUiState())
    val uiState: StateFlow<DryRunPreviewUiState> = _uiState.asStateFlow()

    fun loadMacroAndSimulate(macroId: Long) {
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val macro = macroRepository.getMacroById(macroId)
                if (macro != null) {
                    simulateMacro(macro)
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Macro not found"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load macro: ${e.message}"
                    )
                }
            }
        }
    }

    private fun simulateMacro(macro: MacroDTO) {
        viewModelScope.launch {
            try {
                dryRunUseCase.simulateMacro(macro).collect { result ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            result = result,
                            selectedStepIndex = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
            }
        }
    }

    fun selectStep(stepIndex: Int) {
        _uiState.update { it.copy(selectedStepIndex = stepIndex) }
    }

    fun clearSelection() {
        _uiState.update { it.copy(selectedStepIndex = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    fun retrySimulation() {
        val currentResult = uiState.value.result
        if (currentResult != null) {
            simulateMacro(currentResult.macro)
        }
    }
}

data class DryRunPreviewUiState(
    val isLoading: Boolean = false,
    val result: DryRunUseCase.DryRunResult? = null,
    val selectedStepIndex: Int? = null,
    val error: String? = null
)