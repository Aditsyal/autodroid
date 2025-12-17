package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.domain.usecase.CreateMacroUseCase
import com.aditsyal.autodroid.domain.usecase.GetMacroByIdUseCase
import com.aditsyal.autodroid.domain.usecase.UpdateMacroUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MacroEditorViewModel @Inject constructor(
    private val getMacroByIdUseCase: GetMacroByIdUseCase,
    private val createMacroUseCase: CreateMacroUseCase,
    private val updateMacroUseCase: UpdateMacroUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacroEditorUiState())
    val uiState: StateFlow<MacroEditorUiState> = _uiState.asStateFlow()

    fun addTrigger(trigger: TriggerDTO) {
        _uiState.update { state ->
            val updatedMacro = (state.currentMacro ?: createEmptyMacro()).let { 
                it.copy(triggers = it.triggers + trigger)
            }
            state.copy(currentMacro = updatedMacro)
        }
    }

    fun removeTrigger(trigger: TriggerDTO) {
        _uiState.update { state ->
            val updatedMacro = state.currentMacro?.let { 
                it.copy(triggers = it.triggers - trigger)
            }
            state.copy(currentMacro = updatedMacro)
        }
    }

    fun addAction(action: ActionDTO) {
        _uiState.update { state ->
            val updatedMacro = (state.currentMacro ?: createEmptyMacro()).let {
                val newAction = action.copy(executionOrder = it.actions.size)
                it.copy(actions = it.actions + newAction)
            }
            state.copy(currentMacro = updatedMacro)
        }
    }

    fun removeAction(action: ActionDTO) {
        _uiState.update { state ->
            val updatedMacro = state.currentMacro?.let {
                it.copy(actions = it.actions - action)
            }
            state.copy(currentMacro = updatedMacro)
        }
    }

    private fun createEmptyMacro() = MacroDTO(
        name = "",
        description = "",
        enabled = true,
        triggers = emptyList(),
        actions = emptyList(),
        constraints = emptyList()
    )

    fun loadMacro(macroId: Long) {
        if (macroId == 0L) {
            _uiState.update { it.copy(currentMacro = null, error = null) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { getMacroByIdUseCase(macroId) }
                .onSuccess { macro ->
                    _uiState.update {
                        if (macro != null) {
                            it.copy(isLoading = false, currentMacro = macro, error = null)
                        } else {
                            it.copy(isLoading = false, error = "Macro not found")
                        }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load macro"
                        )
                    }
                }
        }
    }

    fun saveMacro(macro: MacroDTO) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null, saved = false) }
            runCatching {
                if (macro.id == 0L) {
                    val newId = createMacroUseCase(macro)
                    macro.copy(id = newId)
                } else {
                    updateMacroUseCase(macro)
                    macro
                }
            }
                .onSuccess { savedMacro ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saved = true,
                            currentMacro = savedMacro
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saved = false,
                            error = throwable.message ?: "Unable to save macro"
                        )
                    }
                }
        }
    }

    fun clearTransientState() {
        _uiState.update { it.copy(saved = false, error = null) }
    }
}

data class MacroEditorUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val currentMacro: MacroDTO? = null,
    val saved: Boolean = false,
    val error: String? = null
)


