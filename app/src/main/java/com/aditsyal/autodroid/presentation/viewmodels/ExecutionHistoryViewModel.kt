package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class ExecutionHistoryViewModel @Inject constructor(
    private val repository: MacroRepository
) : ViewModel() {

    val uiState: StateFlow<ExecutionHistoryUiState> = repository.getAllExecutionLogs()
        .map { logs ->
            ExecutionHistoryUiState.Success(logs)
        }
        .catch { throwable ->
            emit(ExecutionHistoryUiState.Error(throwable.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExecutionHistoryUiState.Loading
        )
}

sealed class ExecutionHistoryUiState {
    object Loading : ExecutionHistoryUiState()
    data class Success(val logs: List<ExecutionLogDTO>) : ExecutionHistoryUiState()
    data class Error(val message: String) : ExecutionHistoryUiState()
}
