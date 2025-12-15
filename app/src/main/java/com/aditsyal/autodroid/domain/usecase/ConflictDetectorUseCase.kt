package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.ConflictDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ConflictDetectorUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    operator fun invoke(): Flow<List<ConflictDTO>> = repository.getMacroConflicts()
}
