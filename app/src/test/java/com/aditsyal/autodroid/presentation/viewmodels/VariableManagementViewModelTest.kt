package com.aditsyal.autodroid.presentation.viewmodels

import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.data.repository.VariableRepository
import io.mockk.coEvery
import io.mockk.coVerify
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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class VariableManagementViewModelTest {

    private lateinit var viewModel: VariableManagementViewModel
    private val repository = mockk<VariableRepository>()

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @Test
    fun `uiState should emit Success with variables from repository`() = runTest {
        val variables = listOf(
            VariableDTO(id = 1, name = "var1", value = "val1", scope = "GLOBAL", type = "STRING", macroId = null)
        )
        coEvery { repository.getGlobalVariables() } returns flowOf(variables)

        viewModel = VariableManagementViewModel(repository)

        // Ensure collection starts
        backgroundScope.launch { viewModel.uiState.collect {} }

        // Wait for Success state (first one that matches)
        val state = viewModel.uiState.first { it is VariableManagementUiState.Success }
        assertEquals(variables, (state as VariableManagementUiState.Success).variables)
    }

    @Test
    fun `createVariable should call repository`() = runTest {
        coEvery { repository.getGlobalVariables() } returns flowOf(emptyList())
        coEvery { repository.setVariableValue(any(), any(), any(), any(), any()) } returns 1L

        viewModel = VariableManagementViewModel(repository)
        viewModel.createVariable("newVar", "newValue", "STRING")

        coVerify { 
            repository.setVariableValue(
                name = "newVar", 
                value = "newValue", 
                type = "STRING", 
                scope = "GLOBAL"
            ) 
        }
        assertEquals(VariableDialogState.Hidden, viewModel.dialogState.value)
    }

    @Test
    fun `deleteVariable should call repository`() = runTest {
        coEvery { repository.getGlobalVariables() } returns flowOf(emptyList())
        coEvery { repository.deleteVariable(any()) } returns Unit

        viewModel = VariableManagementViewModel(repository)
        val variable = VariableDTO(id = 1, name = "var1", value = "val1", scope = "GLOBAL", type = "STRING", macroId = null)
        viewModel.deleteVariable(variable)

        coVerify { repository.deleteVariable(1L) }
    }
}
