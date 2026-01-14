package com.aditsyal.autodroid.presentation.viewmodels

import android.net.Uri
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.domain.repository.MacroRepository
import com.aditsyal.autodroid.domain.usecase.ExportResult
import com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportMacrosViewModelTest {

    private lateinit var viewModel: ExportMacrosViewModel
    private val importExportMacrosUseCase = mockk<ImportExportMacrosUseCase>()
    private val macroRepository = mockk<MacroRepository>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ExportMacrosViewModel {
        return ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)
    }

    @Test
    fun `exportAllMacros should update state to Success on success`() = runTest {
        val result = ExportResult(
            success = true,
            uri = mockk<Uri>(),
            macroCount = 5
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { importExportMacrosUseCase.exportAllMacros() } returns result

        viewModel = createViewModel()
        viewModel.exportAllMacros()

        val state = viewModel.uiState.value
        assertTrue(state is ExportMacrosViewModel.ExportState.Success)
        assertEquals(result, (state as ExportMacrosViewModel.ExportState.Success).result)
    }

    @Test
    fun `exportAllMacros should update state to Error on failure`() = runTest {
        val result = ExportResult(
            success = false,
            error = "Export failed"
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { importExportMacrosUseCase.exportAllMacros() } returns result

        viewModel = createViewModel()
        viewModel.exportAllMacros()

        val state = viewModel.uiState.value
        assertTrue(state is ExportMacrosViewModel.ExportState.Error)
        assertEquals("Export failed", (state as ExportMacrosViewModel.ExportState.Error).error)
    }

    @Test
    fun `exportSingleMacro should update state to Success on success`() = runTest {
        val result = ExportResult(
            success = true,
            uri = mockk<Uri>(),
            macroCount = 1
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { importExportMacrosUseCase.exportSingleMacro(123L) } returns result

        viewModel = createViewModel()
        viewModel.exportSingleMacro(123L)

        val state = viewModel.uiState.value
        assertTrue(state is ExportMacrosViewModel.ExportState.Success)
        assertEquals(result, (state as ExportMacrosViewModel.ExportState.Success).result)
    }

    @Test
    fun `exportSingleMacro should update state to Error on failure`() = runTest {
        val result = ExportResult(
            success = false,
            error = "Single export failed"
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(emptyList())
        coEvery { importExportMacrosUseCase.exportSingleMacro(123L) } returns result

        viewModel = createViewModel()
        viewModel.exportSingleMacro(123L)

        val state = viewModel.uiState.value
        assertTrue(state is ExportMacrosViewModel.ExportState.Error)
        assertEquals("Single export failed", (state as ExportMacrosViewModel.ExportState.Error).error)
    }

    @Test
    fun `macros flow should emit macros from repository`() = runTest {
        val macros = listOf(
            MacroDTO(id = 1, name = "Macro 1", description = "Desc 1"),
            MacroDTO(id = 2, name = "Macro 2", description = "Desc 2")
        )
        every { macroRepository.getAllMacros() } returns flowOf(macros)

        viewModel = createViewModel()

        assertEquals(2, viewModel.macros.value.size)
        assertEquals("Macro 1", viewModel.macros.value[0].name)
        assertEquals("Macro 2", viewModel.macros.value[1].name)
    }

    @Test
    fun `macros flow should be empty when repository returns empty`() = runTest {
        every { macroRepository.getAllMacros() } returns flowOf(emptyList())

        viewModel = createViewModel()

        assertTrue(viewModel.macros.value.isEmpty())
    }
}
