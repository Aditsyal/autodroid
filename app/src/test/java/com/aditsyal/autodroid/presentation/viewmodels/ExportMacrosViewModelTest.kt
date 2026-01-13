package com.aditsyal.autodroid.presentation.viewmodels

import android.net.Uri
import com.aditsyal.autodroid.domain.usecase.ExportResult
import com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportMacrosViewModelTest {

    private lateinit var viewModel: ExportMacrosViewModel
    private val importExportMacrosUseCase = mockk<ImportExportMacrosUseCase>()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ExportMacrosViewModel(importExportMacrosUseCase)
    }

    @Test
    fun `exportAllMacros should update state to Success on success`() = runTest {
        val result = ExportResult(
            success = true,
            uri = mockk<Uri>(),
            macroCount = 5
        )
        coEvery { importExportMacrosUseCase.exportAllMacros() } returns result

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
        coEvery { importExportMacrosUseCase.exportAllMacros() } returns result

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
        coEvery { importExportMacrosUseCase.exportSingleMacro(123L) } returns result

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
        coEvery { importExportMacrosUseCase.exportSingleMacro(123L) } returns result

        viewModel.exportSingleMacro(123L)

        val state = viewModel.uiState.value
        assertTrue(state is ExportMacrosViewModel.ExportState.Error)
        assertEquals("Single export failed", (state as ExportMacrosViewModel.ExportState.Error).error)
    }
}
