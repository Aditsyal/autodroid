package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.ConflictDetectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConflictDetectionViewModel @Inject constructor(
    private val conflictDetectionUseCase: ConflictDetectionUseCase,
    private val macroRepository: MacroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConflictDetectionUiState())
    val uiState: StateFlow<ConflictDetectionUiState> = _uiState.asStateFlow()

    init {
        loadAllConflicts()
    }

    /**
     * Check conflicts for a specific macro during editing
     */
    fun checkMacroConflicts(macro: MacroDTO) {
        _uiState.update { it.copy(isCheckingMacro = true, macroConflicts = emptyList()) }

        viewModelScope.launch {
            try {
                val allMacros = macroRepository.getAllMacros().collectLatest { macros ->
                    conflictDetectionUseCase.checkMacroConflicts(macro, macros).collect { result ->
                        _uiState.update {
                            it.copy(
                                isCheckingMacro = false,
                                macroConflicts = result.conflicts,
                                canSaveMacro = result.canProceed
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isCheckingMacro = false,
                        macroConflicts = emptyList(),
                        error = "Failed to check conflicts: ${e.message}"
                    )
                }
            }
        }
    }

    /**
     * Load all conflicts between existing macros
     */
    private fun loadAllConflicts() {
        viewModelScope.launch {
            try {
                macroRepository.getAllMacros().collectLatest { macros ->
                    conflictDetectionUseCase.checkAllMacroConflicts(macros).collect { conflicts ->
                        _uiState.update {
                            it.copy(
                                allConflicts = conflicts,
                                isLoadingConflicts = false
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        error = "Failed to load conflicts: ${e.message}",
                        isLoadingConflicts = false
                    )
                }
            }
        }
    }

    /**
     * Validate a macro before saving
     */
    fun validateMacroForSaving(macro: MacroDTO): Boolean {
        val result = conflictDetectionUseCase.validateMacroForSaving(macro)
        return result.canProceed
    }

    /**
     * Refresh all conflicts
     */
    fun refreshConflicts() {
        _uiState.update { it.copy(isLoadingConflicts = true, error = null) }
        loadAllConflicts()
    }

    /**
     * Clear any displayed error
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Clear macro-specific conflict checking state
     */
    fun clearMacroConflicts() {
        _uiState.update {
            it.copy(
                macroConflicts = emptyList(),
                canSaveMacro = true,
                isCheckingMacro = false
            )
        }
    }
}

data class ConflictDetectionUiState(
    val isLoadingConflicts: Boolean = true,
    val isCheckingMacro: Boolean = false,
    val allConflicts: List<ConflictDetectionUseCase.Conflict> = emptyList(),
    val macroConflicts: List<ConflictDetectionUseCase.Conflict> = emptyList(),
    val canSaveMacro: Boolean = true,
    val error: String? = null
)