package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.ExportResult
import com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ExportMacrosViewModel @Inject constructor(
    private val importExportMacrosUseCase: ImportExportMacrosUseCase,
    private val macroRepository: MacroRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ExportState>(ExportState.Idle)
    val uiState: StateFlow<ExportState> = _uiState.asStateFlow()

    private val _macros = MutableStateFlow<List<MacroDTO>>(emptyList())
    val macros: StateFlow<List<MacroDTO>> = _macros.asStateFlow()

    init {
        loadMacros()
    }

    private fun loadMacros() {
        viewModelScope.launch {
            macroRepository.getAllMacros().collect { macroList ->
                _macros.value = macroList
            }
        }
    }

    fun exportAllMacros() {
        _uiState.update { ExportState.Exporting }

        viewModelScope.launch {
            try {
                val result = importExportMacrosUseCase.exportAllMacros()

                if (result.success) {
                    _uiState.update { ExportState.Success(result) }
                    Timber.i("Export completed successfully: ${result.macroCount} macros, ${result.variableCount} variables, ${result.templateCount} templates")
                } else {
                    _uiState.update { ExportState.Error(result.error ?: "Unknown error") }
                    Timber.e("Export failed: ${result.error}")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error occurred during export"
                _uiState.update { ExportState.Error(errorMessage) }
                Timber.e(e, "Export failed with exception")
            }
        }
    }

    fun exportSingleMacro(macroId: Long) {
        _uiState.update { ExportState.Exporting }

        viewModelScope.launch {
            try {
                val result = importExportMacrosUseCase.exportSingleMacro(macroId)

                if (result.success) {
                    _uiState.update { ExportState.Success(result) }
                    Timber.i("Single macro export completed successfully")
                } else {
                    _uiState.update { ExportState.Error(result.error ?: "Unknown error") }
                    Timber.e("Single macro export failed: ${result.error}")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error occurred during export"
                _uiState.update { ExportState.Error(errorMessage) }
                Timber.e(e, "Export failed with exception")
            }
        }
    }

    fun resetState() {
        _uiState.update { ExportState.Idle }
    }

    sealed class ExportState {
        data object Idle : ExportState()
        data object Exporting : ExportState()
        data class Success(val result: ExportResult) : ExportState()
        data class Error(val error: String) : ExportState()
    }
}