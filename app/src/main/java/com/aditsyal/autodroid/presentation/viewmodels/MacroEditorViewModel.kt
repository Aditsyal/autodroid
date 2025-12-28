package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.SavedStateHandle
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.domain.usecase.CreateMacroUseCase
import com.aditsyal.autodroid.domain.usecase.CreateMacroFromTemplateUseCase
import com.aditsyal.autodroid.domain.usecase.GetMacroByIdUseCase
import com.aditsyal.autodroid.domain.usecase.UpdateMacroUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MacroEditorViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val getMacroByIdUseCase: GetMacroByIdUseCase,
    private val createMacroUseCase: CreateMacroUseCase,
    private val updateMacroUseCase: UpdateMacroUseCase,
    private val createMacroFromTemplateUseCase: CreateMacroFromTemplateUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MacroEditorUiState(
        currentMacro = savedStateHandle.get<MacroDTO>("current_macro")
    ))
    val uiState: StateFlow<MacroEditorUiState> = _uiState.asStateFlow()

    private fun updateMacroState(macro: MacroDTO?) {
        savedStateHandle["current_macro"] = macro
        _uiState.update { it.copy(currentMacro = macro) }
    }

    fun addTrigger(trigger: TriggerDTO) {
        val updatedMacro = (uiState.value.currentMacro ?: createEmptyMacro()).let { 
            it.copy(triggers = it.triggers + trigger)
        }
        updateMacroState(updatedMacro)
    }

    fun removeTrigger(trigger: TriggerDTO) {
        val updatedMacro = uiState.value.currentMacro?.let { 
            it.copy(triggers = it.triggers - trigger)
        }
        updateMacroState(updatedMacro)
    }

    fun addAction(action: ActionDTO) {
        val updatedMacro = (uiState.value.currentMacro ?: createEmptyMacro()).let {
            val newAction = action.copy(executionOrder = it.actions.size)
            it.copy(actions = it.actions + newAction)
        }
        updateMacroState(updatedMacro)
    }

    fun removeAction(action: ActionDTO) {
        val updatedMacro = uiState.value.currentMacro?.let {
            it.copy(actions = it.actions - action)
        }
        updateMacroState(updatedMacro)
    }

    fun addConstraint(constraint: ConstraintDTO) {
        val updatedMacro = (uiState.value.currentMacro ?: createEmptyMacro()).let {
            it.copy(constraints = it.constraints + constraint)
        }
        updateMacroState(updatedMacro)
    }

    fun removeConstraint(constraint: ConstraintDTO) {
        val updatedMacro = uiState.value.currentMacro?.let {
            it.copy(constraints = it.constraints - constraint)
        }
        updateMacroState(updatedMacro)
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
                    if (macro != null) {
                        updateMacroState(macro)
                        _uiState.update { it.copy(isLoading = false, error = null) }
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Macro not found") }
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

    fun loadFromTemplate(templateId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            runCatching { createMacroFromTemplateUseCase(templateId) }
                .onSuccess { macroId ->
                    if (macroId != null) {
                        // Load the newly created macro
                        loadMacro(macroId)
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "Failed to create macro from template") }
                    }
                }
                .onFailure { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: "Unable to load template"
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
                    updateMacroState(savedMacro)
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            saved = true
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


