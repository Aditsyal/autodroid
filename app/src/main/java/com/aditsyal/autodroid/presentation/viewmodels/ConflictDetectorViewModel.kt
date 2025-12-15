package com.aditsyal.autodroid.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aditsyal.autodroid.data.models.ConflictDTO
import com.aditsyal.autodroid.domain.usecase.ConflictDetectorUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConflictDetectorViewModel @Inject constructor(
    private val conflictDetectorUseCase: ConflictDetectorUseCase
) : ViewModel() {
    val uiState: StateFlow<List<ConflictDTO>> = conflictDetectorUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
