package com.aditsyal.autodroid.presentation.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
import com.aditsyal.autodroid.domain.usecase.ImportResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ImportMacrosViewModel @Inject constructor(
    private val importExportMacrosUseCase: ImportExportMacrosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<ImportState>(ImportState.Idle)
    val uiState: StateFlow<ImportState> = _uiState.asStateFlow()

    fun importMacros(uri: Uri) {
        _uiState.update { ImportState.Importing }

        viewModelScope.launch {
            try {
                val result = importExportMacrosUseCase.importMacros(uri)

                if (result.success) {
                    _uiState.update { ImportState.Success(result) }
                    Timber.i("Import completed successfully: ${result.macroCount} macros, ${result.variableCount} variables, ${result.templateCount} templates")
                } else {
                    _uiState.update { ImportState.Error(result.error ?: "Unknown error") }
                    Timber.e("Import failed: ${result.error}")
                }
            } catch (e: Exception) {
                val errorMessage = e.message ?: "Unknown error occurred during import"
                _uiState.update { ImportState.Error(errorMessage) }
                Timber.e(e, "Import failed with exception")
            }
        }
    }

    fun resetState() {
        _uiState.update { ImportState.Idle }
    }

    sealed class ImportState {
        data object Idle : ImportState()
        data object Importing : ImportState()
        data class Success(val result: ImportResult) : ImportState()
        data class Error(val error: String) : ImportState()
    }
}