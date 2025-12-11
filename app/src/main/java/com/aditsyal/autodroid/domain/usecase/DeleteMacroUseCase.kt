package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.domain.repository.MacroRepository
import javax.inject.Inject

class DeleteMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(macroId: Long) =
        repository.deleteMacro(macroId)
}


