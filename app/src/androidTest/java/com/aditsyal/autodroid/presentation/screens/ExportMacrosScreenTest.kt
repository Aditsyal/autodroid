package com.aditsyal.autodroid.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.presentation.viewmodels.ExportMacrosViewModel
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ExportMacrosScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var importExportMacrosUseCase: com.aditsyal.autodroid.domain.usecase.ImportExportMacrosUseCase
    private lateinit var macroRepository: com.aditsyal.autodroid.domain.repository.MacroRepository
    private lateinit var viewModel: ExportMacrosViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        importExportMacrosUseCase = mockk()
        macroRepository = mockk()
    }

    @Test
    fun whenSingleExportTypeSelectedAndMacrosAvailable_macroSelectionUIShown() {
        val macros = listOf(
            MacroDTO(id = 1, name = "Test Macro 1", description = "Description 1"),
            MacroDTO(id = 2, name = "Test Macro 2", description = "Description 2")
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(macros)

        viewModel = ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)

        composeTestRule.setContent {
            ExportMacrosScreenContent(
                uiState = viewModel.uiState.value,
                macros = viewModel.macros.value,
                context = androidx.compose.ui.platform.LocalContext.current,
                onBackClick = {},
                onExportAllMacros = {},
                onExportSingleMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Single Macro").performClick()
        composeTestRule.onNodeWithText("Select Macro").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Macro 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Macro 2").assertIsDisplayed()
    }

    @Test
    fun whenSingleExportTypeSelectedAndNoMacrosAvailable_emptyStateShown() {
        coEvery { macroRepository.getAllMacros() } returns flowOf(emptyList())

        viewModel = ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)

        composeTestRule.setContent {
            ExportMacrosScreenContent(
                uiState = viewModel.uiState.value,
                macros = viewModel.macros.value,
                context = androidx.compose.ui.platform.LocalContext.current,
                onBackClick = {},
                onExportAllMacros = {},
                onExportSingleMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Single Macro").performClick()
        composeTestRule.onNodeWithText("No macros available").assertIsDisplayed()
    }

    @Test
    fun whenMacroSelected_itShouldBeHighlighted() {
        val macros = listOf(
            MacroDTO(id = 1, name = "Test Macro 1", description = "Description 1"),
            MacroDTO(id = 2, name = "Test Macro 2", description = "Description 2")
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(macros)

        viewModel = ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)

        composeTestRule.setContent {
            ExportMacrosScreenContent(
                uiState = viewModel.uiState.value,
                macros = viewModel.macros.value,
                context = androidx.compose.ui.platform.LocalContext.current,
                selectedMacroId = 1L,
                onBackClick = {},
                onExportAllMacros = {},
                onExportSingleMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Single Macro").performClick()
        composeTestRule.onNodeWithText("Test Macro 1").assertIsDisplayed()
        composeTestRule.onNodeWithText("Test Macro 2").assertIsDisplayed()
    }

    @Test
    fun exportButtonDisabledWhenSingleExportTypeSelectedButNoMacroChosen() {
        val macros = listOf(
            MacroDTO(id = 1, name = "Test Macro 1", description = "Description 1")
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(macros)

        viewModel = ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)

        composeTestRule.setContent {
            ExportMacrosScreenContent(
                uiState = viewModel.uiState.value,
                macros = viewModel.macros.value,
                context = androidx.compose.ui.platform.LocalContext.current,
                selectedMacroId = null,
                selectedExportType = ExportType.Single,
                onBackClick = {},
                onExportAllMacros = {},
                onExportSingleMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Export Macros").assertIsNotEnabled()
    }

    @Test
    fun exportButtonEnabledWhenSingleExportTypeSelectedAndMacroChosen() {
        val macros = listOf(
            MacroDTO(id = 1, name = "Test Macro 1", description = "Description 1")
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(macros)

        viewModel = ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)

        composeTestRule.setContent {
            ExportMacrosScreenContent(
                uiState = viewModel.uiState.value,
                macros = viewModel.macros.value,
                context = androidx.compose.ui.platform.LocalContext.current,
                selectedMacroId = 1L,
                selectedExportType = ExportType.Single,
                onBackClick = {},
                onExportAllMacros = {},
                onExportSingleMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Export Macros").assertIsEnabled()
    }

    @Test
    fun whenAllExportTypeSelected_macroSelectionUIShouldNotBeShown() {
        val macros = listOf(
            MacroDTO(id = 1, name = "Test Macro 1", description = "Description 1")
        )
        coEvery { macroRepository.getAllMacros() } returns flowOf(macros)

        viewModel = ExportMacrosViewModel(importExportMacrosUseCase, macroRepository)

        composeTestRule.setContent {
            ExportMacrosScreenContent(
                uiState = viewModel.uiState.value,
                macros = viewModel.macros.value,
                context = androidx.compose.ui.platform.LocalContext.current,
                selectedExportType = ExportType.All,
                onBackClick = {},
                onExportAllMacros = {},
                onExportSingleMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Select Macro").assertDoesNotExist()
    }
}
