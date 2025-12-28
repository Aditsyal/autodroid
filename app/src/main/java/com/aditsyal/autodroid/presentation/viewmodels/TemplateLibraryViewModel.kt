package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TemplateLibraryViewModel @Inject constructor(
    private val templateDao: TemplateDao
) : ViewModel() {

    val uiState: StateFlow<TemplateLibraryUiState> = templateDao.getAllTemplates()
        .map<List<TemplateEntity>, TemplateLibraryUiState> { templates ->
            TemplateLibraryUiState.Success(templates)
        }
        .catch { throwable ->
            emit(TemplateLibraryUiState.Error(throwable.message ?: "Unknown error"))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = TemplateLibraryUiState.Loading
        )
}

sealed class TemplateLibraryUiState {
    object Loading : TemplateLibraryUiState()
    data class Success(val templates: List<TemplateEntity>) : TemplateLibraryUiState()
    data class Error(val message: String) : TemplateLibraryUiState()
}

