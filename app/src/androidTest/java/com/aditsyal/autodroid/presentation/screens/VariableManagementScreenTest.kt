package com.aditsyal.autodroid.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aditsyal.autodroid.data.models.VariableDTO
import com.aditsyal.autodroid.presentation.viewmodels.VariableManagementUiState
import org.junit.Rule
import org.junit.Test

class VariableManagementScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loadingState_isDisplayed() {
        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Loading,
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Loading").assertIsDisplayed()
    }

    @Test
    fun emptyState_isDisplayed() {
        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Success(emptyList()),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithText("No variables found").assertIsDisplayed()
    }

    @Test
    fun variablesList_isDisplayed() {
        val variables = listOf(
            VariableDTO(id = 1, name = "USER_NAME", value = "John", type = "STRING"),
            VariableDTO(id = 2, name = "COUNT", value = "42", type = "NUMBER")
        )

        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Success(variables),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithText("USER_NAME").assertIsDisplayed()
        composeTestRule.onNodeWithText("John").assertIsDisplayed()
        composeTestRule.onNodeWithText("COUNT").assertIsDisplayed()
        composeTestRule.onNodeWithText("42").assertIsDisplayed()
    }

    @Test
    fun errorState_isDisplayed() {
        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Error("Failed to load variables"),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithText("Failed to load variables").assertIsDisplayed()
    }

    @Test
    fun onBackClick_isCalled_whenBackButtonClicked() {
        var backClicked = false
        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Loading,
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = { backClicked = true },
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backClicked)
    }

    @Test
    fun onShowAddDialog_isCalled_whenFabClicked() {
        var addDialogShown = false
        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Success(emptyList()),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = { addDialogShown = true },
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Add Variable").performClick()
        assert(addDialogShown)
    }

    @Test
    fun variableItem_displaysCorrectInfo() {
        val variable = VariableDTO(id = 1, name = "TEST_VAR", value = "test_value", type = "STRING")

        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Success(listOf(variable)),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithText("TEST_VAR").assertIsDisplayed()
        composeTestRule.onNodeWithText("Value: test_value").assertIsDisplayed()
        composeTestRule.onNodeWithText("Type: STRING").assertIsDisplayed()
    }

    @Test
    fun variableItem_hasEditButton() {
        val variable = VariableDTO(id = 1, name = "TEST_VAR", value = "test_value", type = "STRING")

        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Success(listOf(variable)),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
    }

    @Test
    fun variableItem_hasDeleteButton() {
        val variable = VariableDTO(id = 1, name = "TEST_VAR", value = "test_value", type = "STRING")

        composeTestRule.setContent {
            VariableManagementScreenContent(
                uiState = VariableManagementUiState.Success(listOf(variable)),
                dialogState = com.aditsyal.autodroid.presentation.viewmodels.VariableDialogState.Hidden,
                onBackClick = {},
                onShowAddDialog = {},
                onShowEditDialog = {},
                onHideDialog = {},
                onCreateVariable = { _, _, _ -> },
                onUpdateVariable = { _, _, _ -> },
                onDeleteVariable = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Delete").assertIsDisplayed()
    }
}
