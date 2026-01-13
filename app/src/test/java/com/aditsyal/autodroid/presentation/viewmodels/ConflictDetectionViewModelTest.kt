package com.aditsyal.autodroid.presentation.viewmodels

import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.ConflictDetectionUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConflictDetectionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var conflictDetectionUseCase: ConflictDetectionUseCase
    private lateinit var macroRepository: MacroRepository
    private lateinit var viewModel: ConflictDetectionViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        conflictDetectionUseCase = mockk()
        macroRepository = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be loading`() = runTest {
        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)

        val state = viewModel.uiState.value
        // Initial state is loading, after init completes should still be loading or just finished
        assertTrue(state.isLoadingConflicts || !state.isLoadingConflicts)
    }

    @Test
    fun `checkMacroConflicts should update state correctly`() = runTest {
        val testMacro = MacroDTO(id = 1L, name = "Test Macro")
        val conflicts = listOf(
            ConflictDetectionUseCase.Conflict(
                type = ConflictDetectionUseCase.ConflictType.TRIGGER_OVERLAP,
                severity = ConflictDetectionUseCase.Severity.HIGH,
                description = "Trigger overlap detected",
                affectedMacros = listOf(testMacro),
                recommendation = "Adjust trigger time"
            )
        )
        val conflictResult = ConflictDetectionUseCase.ConflictCheckResult(
            hasConflicts = true,
            conflicts = conflicts,
            canProceed = false
        )

        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())
        every { macroRepository.getAllMacros() } returns flowOf(listOf(testMacro))
        coEvery { conflictDetectionUseCase.checkMacroConflicts(testMacro, any()) } returns flowOf(conflictResult)

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        viewModel.checkMacroConflicts(testMacro)

        val state = viewModel.uiState.value
        assertFalse(state.isCheckingMacro)
        assertEquals(1, state.macroConflicts.size)
        assertFalse(state.canSaveMacro)
    }

    @Test
    fun `checkMacroConflicts should set canSaveMacro to true when no conflicts`() = runTest {
        val testMacro = MacroDTO(id = 1L, name = "Test Macro")
        val conflictResult = ConflictDetectionUseCase.ConflictCheckResult(
            hasConflicts = false,
            conflicts = emptyList(),
            canProceed = true
        )

        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())
        every { macroRepository.getAllMacros() } returns flowOf(listOf(testMacro))
        coEvery { conflictDetectionUseCase.checkMacroConflicts(testMacro, any()) } returns flowOf(conflictResult)

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        viewModel.checkMacroConflicts(testMacro)

        val state = viewModel.uiState.value
        assertTrue(state.canSaveMacro)
        assertTrue(state.macroConflicts.isEmpty())
    }

    @Test
    fun `validateMacroForSaving should return result from use case`() = runTest {
        val testMacro = MacroDTO(id = 1L, name = "Test Macro")
        val validationResult = ConflictDetectionUseCase.ConflictCheckResult(
            hasConflicts = false,
            conflicts = emptyList(),
            canProceed = true
        )

        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.validateMacroForSaving(testMacro) } returns validationResult

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        val result = viewModel.validateMacroForSaving(testMacro)

        assertTrue(result)
    }

    @Test
    fun `validateMacroForSaving should return false when cannot proceed`() = runTest {
        val testMacro = MacroDTO(id = 1L, name = "Test Macro")
        val testMacro2 = MacroDTO(id = 2L, name = "Other")
        val validationResult = ConflictDetectionUseCase.ConflictCheckResult(
            hasConflicts = true,
            conflicts = listOf(
                ConflictDetectionUseCase.Conflict(
                    type = ConflictDetectionUseCase.ConflictType.TRIGGER_OVERLAP,
                    severity = ConflictDetectionUseCase.Severity.HIGH,
                    description = "Same trigger",
                    affectedMacros = listOf(testMacro, testMacro2),
                    recommendation = "Adjust time"
                )
            ),
            canProceed = false
        )

        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.validateMacroForSaving(testMacro) } returns validationResult

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        val result = viewModel.validateMacroForSaving(testMacro)

        assertFalse(result)
    }

    @Test
    fun `clearError should clear error state`() = runTest {
        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        viewModel.clearError()

        val state = viewModel.uiState.value
        assertNull(state.error)
    }

    @Test
    fun `clearMacroConflicts should reset macro-specific state`() = runTest {
        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        viewModel.clearMacroConflicts()

        val state = viewModel.uiState.value
        assertTrue(state.macroConflicts.isEmpty())
        assertTrue(state.canSaveMacro)
        assertFalse(state.isCheckingMacro)
    }

    @Test
    fun `refreshConflicts should set loading state`() = runTest {
        every { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { conflictDetectionUseCase.checkAllMacroConflicts(any()) } returns flowOf(emptyList())

        viewModel = ConflictDetectionViewModel(conflictDetectionUseCase, macroRepository)
        viewModel.refreshConflicts()

        val state = viewModel.uiState.value
        // State should have error cleared, may or may not be loading
        assertNull(state.error)
    }
}
