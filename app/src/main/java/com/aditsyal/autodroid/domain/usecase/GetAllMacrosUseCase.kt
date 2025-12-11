package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllMacrosUseCase @Inject constructor(
    private val repository: MacroRepository
) {
    operator fun invoke(): Flow<List<MacroDTO>> = repository.getAllMacros()
}


