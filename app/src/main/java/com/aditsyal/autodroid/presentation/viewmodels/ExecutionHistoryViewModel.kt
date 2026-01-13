package com.aditsyal.autodroid.presentation.viewmodels

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@HiltViewModel
class ExecutionHistoryViewModel @Inject constructor(
    private val repository: MacroRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    val uiState: StateFlow<ExecutionHistoryUiState> = combine(
        repository.getAllExecutionLogs(),
        _searchQuery.debounce(300).distinctUntilChanged(),
        _statusFilter,
        _startDate,
        _endDate
    ) { logs, query, status, start, end ->
        val filtered = logs.filter { log ->
            (status == null || log.executionStatus == status) &&
            (query.isEmpty() ||
             (log.macroName?.contains(query, ignoreCase = true) == true) ||
             log.macroId.toString().contains(query, ignoreCase = true)) &&
            (start == null || log.executedAt >= start) &&
            (end == null || log.executedAt <= end)
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

    fun setDateRange(startDate: Long?, endDate: Long?) {
        _startDate.value = startDate
        _endDate.value = endDate
    }

    // Paged/filtered execution logs for performance
    fun getPagedExecutionLogs(
        limit: Int = 50,
        offset: Int = 0,
        statusFilter: String? = null,
        macroIdFilter: Long? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): StateFlow<ExecutionHistoryUiState> = repository.getAllExecutionLogs()
        .map<List<ExecutionLogDTO>, ExecutionHistoryUiState> { logs ->
            var filtered = logs
            if (statusFilter != null) {
                filtered = filtered.filter { it.executionStatus == statusFilter }
            }
            if (macroIdFilter != null) {
                filtered = filtered.filter { it.macroId == macroIdFilter }
            }
            if (startDate != null) {
                filtered = filtered.filter { it.executedAt >= startDate }
            }
            if (endDate != null) {
                filtered = filtered.filter { it.executedAt <= endDate }
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

    fun exportHistoryAsCsv(context: Context) {
        viewModelScope.launch {
            try {
                val logs = when (val state = uiState.value) {
                    is ExecutionHistoryUiState.Success -> state.logs
                    else -> emptyList()
                }

                if (logs.isEmpty()) return@launch

                val csvContent = buildCsvContent(logs)
                val file = createExportFile(context, "execution_history.csv")
                FileWriter(file).use { it.write(csvContent) }

                shareFile(context, file, "text/csv")
            } catch (e: Exception) {
                // Handle error - could emit to a state flow for UI feedback
            }
        }
    }

    fun exportHistoryAsJson(context: Context) {
        viewModelScope.launch {
            try {
                val logs = when (val state = uiState.value) {
                    is ExecutionHistoryUiState.Success -> state.logs
                    else -> emptyList()
                }

                if (logs.isEmpty()) return@launch

                val jsonContent = buildJsonContent(logs)
                val file = createExportFile(context, "execution_history.json")
                FileWriter(file).use { it.write(jsonContent) }

                shareFile(context, file, "application/json")
            } catch (e: Exception) {
                // Handle error - could emit to a state flow for UI feedback
            }
        }
    }

    private fun buildCsvContent(logs: List<ExecutionLogDTO>): String {
        val csv = StringBuilder()
        csv.append("ID,Macro Name,Executed At,Status,Duration (ms),Error Message,Actions Count\n")

        logs.forEach { log ->
            val dateStr = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(log.executedAt))
            val errorMsg = log.errorMessage?.replace("\"", "\"\"") ?: "" // Escape quotes for CSV
            csv.append("${log.id},\"${log.macroName ?: ""}\",\"$dateStr\",\"${log.executionStatus}\",${log.executionDurationMs},\"$errorMsg\",${log.actions.size}\n")
        }

        return csv.toString()
    }

    private fun buildJsonContent(logs: List<ExecutionLogDTO>): String {
        val jsonArray = JSONArray()

        logs.forEach { log ->
            val jsonObject = JSONObject().apply {
                put("id", log.id)
                put("macroId", log.macroId)
                put("macroName", log.macroName)
                put("executedAt", log.executedAt)
                put("executionStatus", log.executionStatus)
                put("executionDurationMs", log.executionDurationMs)
                put("errorMessage", log.errorMessage)

                val actionsArray = JSONArray()
                log.actions.forEach { action ->
                    val actionObj = JSONObject().apply {
                        put("id", action.id)
                        put("actionType", action.actionType)
                        put("executionOrder", action.executionOrder)
                        put("delayAfter", action.delayAfter)
                        put("actionConfig", JSONObject(action.actionConfig))
                    }
                    actionsArray.put(actionObj)
                }
                put("actions", actionsArray)
            }
            jsonArray.put(jsonObject)
        }

        return jsonArray.toString(2)
    }

    private fun createExportFile(context: Context, fileName: String): File {
        val dir = File(context.getExternalFilesDir(null), "exports")
        if (!dir.exists()) dir.mkdirs()
        return File(dir, "${System.currentTimeMillis()}_$fileName")
    }

    private fun shareFile(context: Context, file: File, mimeType: String) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share execution history"))
    }
}

sealed class ExecutionHistoryUiState {
    object Loading : ExecutionHistoryUiState()
    data class Success(val logs: List<ExecutionLogDTO>) : ExecutionHistoryUiState()
    data class Error(val message: String) : ExecutionHistoryUiState()
}
