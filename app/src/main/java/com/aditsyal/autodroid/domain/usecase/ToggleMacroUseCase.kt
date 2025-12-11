package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.domain.repository.MacroRepository
import javax.inject.Inject

class ToggleMacroUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    suspend operator fun invoke(macroId: Long, enabled: Boolean) =
        repository.toggleMacro(macroId, enabled)
}


