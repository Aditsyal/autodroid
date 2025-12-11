package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import javax.inject.Inject

class CreateMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(macro: MacroDTO): Long =
        repository.createMacro(macro)
}


