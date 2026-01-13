package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.data.repository.VariableRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VariableManagementViewModel @Inject constructor(
    private val repository: VariableRepository
) : ViewModel() {

    val uiState: StateFlow<VariableManagementUiState> = repository.getGlobalVariables()
        .map<List<VariableDTO>, VariableManagementUiState> { VariableManagementUiState.Success(it) }
        .catch { emit(VariableManagementUiState.Error(it.message ?: "Unknown error")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = VariableManagementUiState.Loading
        )

    private val _dialogState = MutableStateFlow<VariableDialogState>(VariableDialogState.Hidden)
    val dialogState: StateFlow<VariableDialogState> = _dialogState.asStateFlow()

    fun showAddDialog() {
        _dialogState.value = VariableDialogState.Add
    }

    fun showEditDialog(variable: VariableDTO) {
        _dialogState.value = VariableDialogState.Edit(variable)
    }

    fun hideDialog() {
        _dialogState.value = VariableDialogState.Hidden
    }

    fun createVariable(name: String, value: String, type: String) {
        viewModelScope.launch {
            repository.setVariableValue(name = name, value = value, type = type, scope = "GLOBAL")
            hideDialog()
        }
    }

    fun updateVariable(variable: VariableDTO, newValue: String, newType: String) {
        viewModelScope.launch {
            repository.updateVariable(variable.copy(value = newValue, type = newType))
            hideDialog()
        }
    }

    fun deleteVariable(variable: VariableDTO) {
        viewModelScope.launch {
            repository.deleteVariable(variable.id)
        }
    }
}

sealed class VariableManagementUiState {
    data object Loading : VariableManagementUiState()
    data class Success(val variables: List<VariableDTO>) : VariableManagementUiState()
    data class Error(val message: String) : VariableManagementUiState()
}

sealed class VariableDialogState {
    data object Hidden : VariableDialogState()
    data object Add : VariableDialogState()
    data class Edit(val variable: VariableDTO) : VariableDialogState()
}
