package com.aditsyal.autodroid.presentation.viewmodels

import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.usecase.DeleteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.ExecuteMacroUseCase
import com.aditsyal.autodroid.domain.usecase.GetAllMacrosUseCase
import com.aditsyal.autodroid.domain.usecase.ToggleMacroUseCase
import com.aditsyal.autodroid.util.MainDispatcherRule
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MacroListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @MockK
    lateinit var getAllMacrosUseCase: GetAllMacrosUseCase

    @MockK
    lateinit var toggleMacroUseCase: ToggleMacroUseCase

    @MockK
    lateinit var deleteMacroUseCase: DeleteMacroUseCase

    @MockK
    lateinit var executeMacroUseCase: ExecuteMacroUseCase

    private lateinit var viewModel: MacroListViewModel

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
        
        // Default behavior for init
        coEvery { getAllMacrosUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `init loads macros successfully`() = runTest {
        val macros = listOf(
            MacroDTO(id = 1, name = "Macro 1"),
            MacroDTO(id = 2, name = "Macro 2")
        )
        coEvery { getAllMacrosUseCase() } returns flowOf(macros)

        viewModel = MacroListViewModel(
            getAllMacrosUseCase,
            toggleMacroUseCase,
            deleteMacroUseCase,
            executeMacroUseCase
        )

        val currentState = viewModel.uiState.value
        assertEquals(macros, currentState.macros)
        assertFalse(currentState.isLoading)
        assertNull(currentState.error)
    }

    @Test
    fun `toggleMacro calls useCase`() = runTest {
        viewModel = MacroListViewModel(
            getAllMacrosUseCase,
            toggleMacroUseCase,
            deleteMacroUseCase,
            executeMacroUseCase
        )
        
        coEvery { toggleMacroUseCase(1, true) } returns Unit

        viewModel.toggleMacro(1, true)

        coVerify { toggleMacroUseCase(1, true) }
    }

    @Test
    fun `deleteMacro calls useCase`() = runTest {
        viewModel = MacroListViewModel(
            getAllMacrosUseCase,
            toggleMacroUseCase,
            deleteMacroUseCase,
            executeMacroUseCase
        )

        coEvery { deleteMacroUseCase(1) } returns Unit

        viewModel.deleteMacro(1)

        coVerify { deleteMacroUseCase(1) }
    }

    @Test
    fun `executeMacro success updates state`() = runTest {
        viewModel = MacroListViewModel(
            getAllMacrosUseCase,
            toggleMacroUseCase,
            deleteMacroUseCase,
            executeMacroUseCase
        )

        coEvery { executeMacroUseCase(1) } returns ExecuteMacroUseCase.ExecutionResult.Success

        viewModel.executeMacro(1)

        val currentState = viewModel.uiState.value
        assertFalse(currentState.isActionInFlight)
        assertEquals("Macro executed", currentState.lastActionMessage)
        assertNull(currentState.error)
    }

    @Test
    fun `executeMacro failure updates state`() = runTest {
        viewModel = MacroListViewModel(
            getAllMacrosUseCase,
            toggleMacroUseCase,
            deleteMacroUseCase,
            executeMacroUseCase
        )

        val errorMessage = "Something went wrong"
        coEvery { executeMacroUseCase(1) } returns ExecuteMacroUseCase.ExecutionResult.Failure(errorMessage)

        viewModel.executeMacro(1)

        val currentState = viewModel.uiState.value
        assertFalse(currentState.isActionInFlight)
        assertEquals(errorMessage, currentState.error)
        assertNull(currentState.lastActionMessage)
    }
}
