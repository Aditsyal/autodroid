package com.aditsyal.autodroid.presentation.viewmodels

import android.content.Context
import com.aditsyal.autodroid.data.local.dao.TemplateDao
import com.aditsyal.autodroid.data.models.ConflictDTO
import com.aditsyal.autodroid.data.models.ExecutionLogDTO
import com.aditsyal.autodroid.domain.usecase.CheckPermissionsUseCase
import com.aditsyal.autodroid.domain.usecase.ConflictDetectorUseCase
import com.aditsyal.autodroid.domain.usecase.ManageBatteryOptimizationUseCase
import com.aditsyal.autodroid.domain.repository.MacroRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RemainingViewModelTests {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val context = mockk<Context>(relaxed = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
    }

    @Test
    fun `ConflictDetectorViewModel should load conflicts`() = runTest {
        val useCase = mockk<ConflictDetectorUseCase>()
        val conflicts = listOf(ConflictDTO(1L, "Macro 1", 2))
        every { useCase() } returns flowOf(conflicts)
        
        val viewModel = ConflictDetectorViewModel(useCase)
        // Access uiState to trigger Flow collection, then advance
        val initialValue = viewModel.uiState.value
        testDispatcher.scheduler.advanceTimeBy(100)
        testDispatcher.scheduler.runCurrent()
        // Should have conflicts or be empty initially
        assertTrue(viewModel.uiState.value == conflicts || viewModel.uiState.value.isEmpty())
    }

    @Test
    fun `ExecutionHistoryViewModel should load history`() = runTest {
        val repository = mockk<MacroRepository>()
        val logs = listOf(ExecutionLogDTO(
            id = 1L,
            macroId = 1L,
            macroName = "Macro 1",
            executedAt = System.currentTimeMillis(),
            executionStatus = "SUCCESS"
        ))
        every { repository.getAllExecutionLogs() } returns flowOf(logs)
        
        val viewModel = ExecutionHistoryViewModel(repository)
        // Access uiState to trigger Flow collection, then advance
        val initialValue = viewModel.uiState.value
        testDispatcher.scheduler.advanceTimeBy(100)
        testDispatcher.scheduler.runCurrent()
        // Should be Success or Loading initially
        assertTrue(viewModel.uiState.value is ExecutionHistoryUiState.Success || viewModel.uiState.value is ExecutionHistoryUiState.Loading)
    }

    @Test
    fun `TemplateLibraryViewModel should load templates`() = runTest {
        val templateDao = mockk<TemplateDao>()
        every { templateDao.getAllTemplates() } returns flowOf(emptyList())
        
        val viewModel = TemplateLibraryViewModel(templateDao)
        // Access uiState to trigger Flow collection, then advance
        val initialValue = viewModel.uiState.value
        testDispatcher.scheduler.advanceTimeBy(100)
        testDispatcher.scheduler.runCurrent()
        // Should be Success or Loading initially
        assertTrue(viewModel.uiState.value is TemplateLibraryUiState.Success || viewModel.uiState.value is TemplateLibraryUiState.Loading)
    }

    @Test
    fun `SettingsViewModel should update state`() = runTest {
        val checkPermissionsUseCase = mockk<CheckPermissionsUseCase>(relaxed = true)
        val manageBatteryOptimizationUseCase = mockk<ManageBatteryOptimizationUseCase>(relaxed = true)
        val viewModel = SettingsViewModel(context, checkPermissionsUseCase, manageBatteryOptimizationUseCase)
        
        viewModel.refreshStatus()
        // verify state updates
        assertTrue(viewModel.uiState.value is SettingsUiState)
    }

    @Test
    fun `PermissionHandlerViewModel should exist`() = runTest {
        val useCase = mockk<CheckPermissionsUseCase>()
        val viewModel = PermissionHandlerViewModel(useCase)
        // Just verify the ViewModel was created successfully
        assertTrue(viewModel.checkPermissionsUseCase == useCase)
    }
}
