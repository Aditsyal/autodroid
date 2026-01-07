package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class ExecutionHistoryViewModel @Inject constructor(
    private val repository: MacroRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    val uiState: StateFlow<ExecutionHistoryUiState> = combine(
        repository.getAllExecutionLogs(),
        _searchQuery.debounce(300).distinctUntilChanged(),
        _statusFilter
    ) { logs, query, status ->
        val filtered = logs.filter { log ->
            (status == null || log.executionStatus == status) &&
            (query.isEmpty() || 
             (log.macroName?.contains(query, ignoreCase = true) == true) ||
             log.macroId.toString().contains(query, ignoreCase = true))
        }
        ExecutionHistoryUiState.Success(filtered) as ExecutionHistoryUiState
    }
        .catch { throwable ->
            emit(ExecutionHistoryUiState.Error(throwable.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExecutionHistoryUiState.Loading
        )

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = status
    }

    // Paged/filtered execution logs for performance
    fun getPagedExecutionLogs(
        limit: Int = 50,
        offset: Int = 0,
        statusFilter: String? = null,
        macroIdFilter: Long? = null
    ): StateFlow<ExecutionHistoryUiState> = repository.getAllExecutionLogs()
        .map<List<ExecutionLogDTO>, ExecutionHistoryUiState> { logs ->
            var filtered = logs
            if (statusFilter != null) {
                filtered = filtered.filter { it.executionStatus == statusFilter }
            }
            if (macroIdFilter != null) {
                filtered = filtered.filter { it.macroId == macroIdFilter }
            }
            ExecutionHistoryUiState.Success(filtered.drop(offset).take(limit))
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
