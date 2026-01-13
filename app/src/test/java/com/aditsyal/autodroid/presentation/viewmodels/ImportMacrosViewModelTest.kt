package com.aditsyal.autodroid.presentation.viewmodels

import android.net.Uri
import com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
import com.aditsyal.autodroid.domain.usecase.ImportResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ImportMacrosViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var importExportMacrosUseCase: ImportExportMacrosUseCase
    private lateinit var viewModel: ImportMacrosViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        importExportMacrosUseCase = mockk()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Idle`() = runTest {
        viewModel = ImportMacrosViewModel(importExportMacrosUseCase)

        assertTrue(viewModel.uiState.value is ImportMacrosViewModel.ImportState.Idle)
    }

    @Test
    fun `importMacros should complete with Success on successful import`() = runTest {
        val mockUri = mockk<Uri>()
        val importResult = ImportResult(
            success = true,
            macroCount = 5,
            variableCount = 3,
            templateCount = 2,
            error = null
        )
        coEvery { importExportMacrosUseCase.importMacros(mockUri) } returns importResult

        viewModel = ImportMacrosViewModel(importExportMacrosUseCase)
        viewModel.importMacros(mockUri)

        val state = viewModel.uiState.value
        assertTrue(state is ImportMacrosViewModel.ImportState.Success)
        assertEquals(importResult, (state as ImportMacrosViewModel.ImportState.Success).result)
    }

    @Test
    fun `importMacros should complete with Error on failed import`() = runTest {
        val mockUri = mockk<Uri>()
        val errorMessage = "Failed to parse file"
        coEvery { importExportMacrosUseCase.importMacros(mockUri) } returns ImportResult(
            success = false,
            macroCount = 0,
            variableCount = 0,
            templateCount = 0,
            error = errorMessage
        )

        viewModel = ImportMacrosViewModel(importExportMacrosUseCase)
        viewModel.importMacros(mockUri)

        val state = viewModel.uiState.value
        assertTrue(state is ImportMacrosViewModel.ImportState.Error)
        assertEquals(errorMessage, (state as ImportMacrosViewModel.ImportState.Error).error)
    }

    @Test
    fun `importMacros should complete with Error on exception`() = runTest {
        val mockUri = mockk<Uri>()
        val exceptionMessage = "Unexpected error"
        coEvery { importExportMacrosUseCase.importMacros(mockUri) } throws RuntimeException(exceptionMessage)

        viewModel = ImportMacrosViewModel(importExportMacrosUseCase)
        viewModel.importMacros(mockUri)

        val state = viewModel.uiState.value
        assertTrue(state is ImportMacrosViewModel.ImportState.Error)
        assertTrue((state as ImportMacrosViewModel.ImportState.Error).error.contains(exceptionMessage))
    }

    @Test
    fun `resetState should return to Idle`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery { importExportMacrosUseCase.importMacros(mockUri) } returns ImportResult(
            success = true,
            macroCount = 1,
            variableCount = 0,
            templateCount = 0
        )

        viewModel = ImportMacrosViewModel(importExportMacrosUseCase)
        viewModel.importMacros(mockUri)
        viewModel.resetState()

        assertTrue(viewModel.uiState.value is ImportMacrosViewModel.ImportState.Idle)
    }
}
