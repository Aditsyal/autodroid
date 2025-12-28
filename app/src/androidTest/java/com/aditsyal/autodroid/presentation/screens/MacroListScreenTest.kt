package com.aditsyal.autodroid.presentation.screens

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aditsyal.autodroid.data.models.MacroDTO
import com.aditsyal.autodroid.presentation.viewmodels.MacroListUiState
import org.junit.Rule
import org.junit.Test

class MacroListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_isDisplayed() {
        composeTestRule.setContent {
            MacroListScreenContent(
                uiState = MacroListUiState(isLoading = true),
                snackbarHostState = SnackbarHostState(),
                onAddMacro = {},
                onEditMacro = {},
                onShowHistory = {},
                onShowConflicts = {},
                onShowSettings = {},
                onShowTemplates = {},
                onToggleMacro = { _, _ -> },
                onExecuteMacro = {},
                onDeleteMacro = {}
            )
        }

        // CircularProgressIndicator doesn't have a default text/tag unless set manually.
        // We can check if the list is NOT displayed or empty state is NOT displayed.
        composeTestRule.onNodeWithText("No macros yet").assertDoesNotExist()
    }

    @Test
    fun emptyState_isDisplayed() {
        composeTestRule.setContent {
            MacroListScreenContent(
                uiState = MacroListUiState(macros = emptyList(), isLoading = false),
                snackbarHostState = SnackbarHostState(),
                onAddMacro = {},
                onEditMacro = {},
                onShowHistory = {},
                onShowConflicts = {},
                onShowSettings = {},
                onShowTemplates = {},
                onToggleMacro = { _, _ -> },
                onExecuteMacro = {},
                onDeleteMacro = {}
            )
        }

        composeTestRule.onNodeWithText("No macros yet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create macro").assertIsDisplayed()
    }

    @Test
    fun macroList_isDisplayed() {
        val macros = listOf(
            MacroDTO(id = 1, name = "Morning Routine", description = "Wake up tasks"),
            MacroDTO(id = 2, name = "Night Routine", description = "Sleep tasks")
        )

        composeTestRule.setContent {
            MacroListScreenContent(
                uiState = MacroListUiState(macros = macros, isLoading = false),
                snackbarHostState = SnackbarHostState(),
                onAddMacro = {},
                onEditMacro = {},
                onShowHistory = {},
                onShowConflicts = {},
                onShowSettings = {},
                onShowTemplates = {},
                onToggleMacro = { _, _ -> },
                onExecuteMacro = {},
                onDeleteMacro = {}
            )
        }

        composeTestRule.onNodeWithText("Morning Routine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wake up tasks").assertIsDisplayed()
        composeTestRule.onNodeWithText("Night Routine").assertIsDisplayed()
    }

    @Test
    fun onAddMacro_isCalled_whenFabClicked() {
        var addClicked = false
        composeTestRule.setContent {
            MacroListScreenContent(
                uiState = MacroListUiState(macros = emptyList()),
                snackbarHostState = SnackbarHostState(),
                onAddMacro = { addClicked = true },
                onEditMacro = {},
                onShowHistory = {},
                onShowConflicts = {},
                onShowSettings = {},
                onShowTemplates = {},
                onToggleMacro = { _, _ -> },
                onExecuteMacro = {},
                onDeleteMacro = {}
            )
        }

        // FAB has content description "Add macro"
        composeTestRule.onNodeWithContentDescription("Add macro").performClick()
        
        assert(addClicked)
    }
}
