package com.aditsyal.autodroid.domain.usecase

import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.automation.trigger.TriggerManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateMacroUseCaseTest {
    private lateinit var useCase: CreateMacroUseCase
    private val repository = mockk<MacroRepository>(relaxed = true)
    private val triggerManager = mockk<TriggerManager>(relaxed = true)

    @Before
    fun setup() {
        useCase = CreateMacroUseCase(repository, triggerManager)
    }

    @Test
    fun `should call repository createMacro when creating macro`() = runTest {
        val macro = MacroDTO(name = "Test", triggers = emptyList(), actions = emptyList())
        useCase(macro)
        coVerify { repository.createMacro(macro) }
    }
}

class DeleteMacroUseCaseTest {
    private lateinit var useCase: DeleteMacroUseCase
    private val repository = mockk<MacroRepository>(relaxed = true)

    @Before
    fun setup() {
        useCase = DeleteMacroUseCase(repository)
    }

    @Test
    fun `should call repository delete when deleting macro`() = runTest {
        useCase(1L)
        coVerify { repository.deleteMacro(1L) }
    }
}

class GetAllMacrosUseCaseTest {
    private lateinit var useCase: GetAllMacrosUseCase
    private val repository = mockk<MacroRepository>(relaxed = true)

    @Before
    fun setup() {
        useCase = GetAllMacrosUseCase(repository)
    }

    @Test
    fun `should call repository getAllMacros`() = runTest {
        useCase()
        coVerify { repository.getAllMacros() }
    }
}

class GetMacroByIdUseCaseTest {
    private lateinit var useCase: GetMacroByIdUseCase
    private val repository = mockk<MacroRepository>(relaxed = true)

    @Before
    fun setup() {
        useCase = GetMacroByIdUseCase(repository)
    }

    @Test
    fun `should call repository getMacroById`() = runTest {
        useCase(1L)
        coVerify { repository.getMacroById(1L) }
    }
}

class UpdateMacroUseCaseTest {
    private lateinit var useCase: UpdateMacroUseCase
    private val repository = mockk<MacroRepository>(relaxed = true)
    private val triggerManager = mockk<TriggerManager>(relaxed = true)

    @Before
    fun setup() {
        useCase = UpdateMacroUseCase(repository, triggerManager)
    }

    @Test
    fun `should call repository update when updating macro`() = runTest {
        val macro = MacroDTO(id = 1L, name = "Updated", triggers = emptyList(), actions = emptyList())
        useCase(macro)
        coVerify { repository.updateMacro(macro) }
    }
}

class ToggleMacroUseCaseTest {
    private lateinit var useCase: ToggleMacroUseCase
    private val repository = mockk<MacroRepository>(relaxed = true)
    private val triggerManager = mockk<TriggerManager>(relaxed = true)

    @Before
    fun setup() {
        useCase = ToggleMacroUseCase(repository, triggerManager)
    }

    @Test
    fun `should call repository toggleMacro`() = runTest {
        useCase(1L, true)
        coVerify { repository.toggleMacro(1L, true) }
    }
}
