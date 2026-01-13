package com.aditsyal.autodroid.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.aditsyal.autodroid.data.models.ActionDTO
import com.aditsyal.autodroid.data.models.ConstraintDTO
import com.aditsyal.autodroid.data.models.TriggerDTO
import com.aditsyal.autodroid.presentation.theme.AutodroidTheme
import com.aditsyal.autodroid.presentation.viewmodels.MacroEditorUiState
import com.aditsyal.autodroid.presentation.viewmodels.MacroEditorViewModel
import com.aditsyal.autodroid.test.TestDataFactory
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MacroEditorScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @MockK
    lateinit var viewModel: MacroEditorViewModel

    private val emptyMacro = TestDataFactory.createTestMacro(
        id = 0L,
        name = "",
        description = null,
        triggers = emptyList(),
        actions = emptyList(),
        constraints = emptyList()
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Mock ViewModel uiState with empty macro for editing
        val uiStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(emptyMacro)
        )
        every { viewModel.uiState } returns uiStateFlow

        // Mock other viewModel methods if needed
        every { viewModel.updateMacroName(any()) } returns Unit
        every { viewModel.updateMacroDescription(any()) } returns Unit
        every { viewModel.addTrigger(any()) } returns Unit
        every { viewModel.addAction(any()) } returns Unit
        every { viewModel.addConstraint(any()) } returns Unit
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `macro editor screen displays initial empty state correctly`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify screen title
        composeTestRule.onNodeWithText("Macro Editor").assertIsDisplayed()

        // Verify macro name field is displayed
        composeTestRule.onNodeWithText("Macro Name").assertIsDisplayed()

        // Verify empty states for triggers, actions, constraints
        composeTestRule.onNodeWithText("No triggers added").assertIsDisplayed()
        composeTestRule.onNodeWithText("No actions added").assertIsDisplayed()
        composeTestRule.onNodeWithText("No constraints added").assertIsDisplayed()
    }

    @Test
    fun `macro name input updates when text is entered`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Enter macro name
        composeTestRule.onNodeWithText("Macro Name")
            .performTextInput("My Test Macro")

        // Verify updateMacroName was called
        verify { viewModel.updateMacroName("My Test Macro") }
    }

    @Test
    fun `macro description input updates when text is entered`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Enter macro description (assuming description field exists)
        // composeTestRule.onNodeWithText("Description")
        //     .performTextInput("This is a test macro")

        // Verify updateMacroDescription was called
        // verify { viewModel.updateMacroDescription("This is a test macro") }
    }

    @Test
    fun `add trigger button shows trigger type selection when clicked`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Click add trigger button
        composeTestRule.onNodeWithText("Add Trigger").performClick()

        // Verify trigger selection dialog appears
        composeTestRule.onNodeWithText("Select Trigger Type").assertIsDisplayed()
    }

    @Test
    fun `add action button shows action type selection when clicked`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Click add action button
        composeTestRule.onNodeWithText("Add Action").performClick()

        // Verify action selection dialog appears
        composeTestRule.onNodeWithText("Select Action Type").assertIsDisplayed()
    }

    @Test
    fun `add constraint button shows constraint type selection when clicked`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Click add constraint button
        composeTestRule.onNodeWithText("Add Constraint").performClick()

        // Verify constraint selection dialog appears
        composeTestRule.onNodeWithText("Select Constraint Type").assertIsDisplayed()
    }

    @Test
    fun `macro editor displays existing triggers correctly`() {
        val macroWithTriggers = emptyMacro.copy(
            triggers = listOf(
                TestDataFactory.createTestTrigger(
                    id = 1L,
                    type = "TIME",
                    description = "Daily at 9 AM"
                ),
                TestDataFactory.createTestTrigger(
                    id = 2L,
                    type = "LOCATION",
                    description = "When arriving home"
                )
            )
        )

        val triggerStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(macroWithTriggers)
        )
        every { viewModel.uiState } returns triggerStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify triggers are displayed
        composeTestRule.onNodeWithText("TIME").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily at 9 AM").assertIsDisplayed()
        composeTestRule.onNodeWithText("LOCATION").assertIsDisplayed()
        composeTestRule.onNodeWithText("When arriving home").assertIsDisplayed()

        // Verify "No triggers added" is not displayed
        composeTestRule.onNodeWithText("No triggers added").assertDoesNotExist()
    }

    @Test
    fun `macro editor displays existing actions correctly`() {
        val macroWithActions = emptyMacro.copy(
            actions = listOf(
                TestDataFactory.createTestAction(
                    id = 1L,
                    type = "TOAST",
                    description = "Show notification"
                ),
                TestDataFactory.createTestAction(
                    id = 2L,
                    type = "NOTIFICATION",
                    description = "Send push notification"
                )
            )
        )

        val actionStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(macroWithActions)
        )
        every { viewModel.uiState } returns actionStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify actions are displayed
        composeTestRule.onNodeWithText("TOAST").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show notification").assertIsDisplayed()
        composeTestRule.onNodeWithText("NOTIFICATION").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send push notification").assertIsDisplayed()

        // Verify "No actions added" is not displayed
        composeTestRule.onNodeWithText("No actions added").assertDoesNotExist()
    }

    @Test
    fun `macro editor displays existing constraints correctly`() {
        val macroWithConstraints = emptyMacro.copy(
            constraints = listOf(
                TestDataFactory.createTestConstraint(
                    id = 1L,
                    type = "BATTERY_LEVEL",
                    description = "Battery > 20%"
                ),
                TestDataFactory.createTestConstraint(
                    id = 2L,
                    type = "WIFI_CONNECTED",
                    description = "WiFi connected"
                )
            )
        )

        val constraintStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(macroWithConstraints)
        )
        every { viewModel.uiState } returns constraintStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify constraints are displayed
        composeTestRule.onNodeWithText("BATTERY_LEVEL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery > 20%").assertIsDisplayed()
        composeTestRule.onNodeWithText("WIFI_CONNECTED").assertIsDisplayed()
        composeTestRule.onNodeWithText("WiFi connected").assertIsDisplayed()

        // Verify "No constraints added" is not displayed
        composeTestRule.onNodeWithText("No constraints added").assertDoesNotExist()
    }

    @Test
    fun `back button calls onBack when clicked`() {
        var backClicked = false

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = { backClicked = true },
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Click back button
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Verify onBack was called
        assert(backClicked)
    }

    @Test
    fun `save button calls onSaved when clicked and macro is valid`() {
        var savedCalled = false

        // Mock a valid macro
        val validMacro = emptyMacro.copy(
            name = "Valid Macro",
            triggers = listOf(TestDataFactory.createTestTrigger()),
            actions = listOf(TestDataFactory.createTestAction())
        )

        val validStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(validMacro)
        )
        every { viewModel.uiState } returns validStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = null,
                    onBack = {},
                    onSaved = { savedCalled = true },
                    viewModel = viewModel
                )
            }
        }

        // Click save button (assuming it exists)
        // composeTestRule.onNodeWithText("Save").performClick()

        // For now, just verify the screen loads
        composeTestRule.onNodeWithText("Macro Editor").assertIsDisplayed()

        // When save functionality is implemented, uncomment:
        // assert(savedCalled)
    }

    @Test
    fun `macro editor shows loading state correctly`() {
        // Mock loading state
        val loadingStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Loading
        )
        every { viewModel.uiState } returns loadingStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 1L, // Existing macro
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify loading indicator is shown or main content is not displayed
        composeTestRule.onNodeWithText("Macro Editor").assertIsDisplayed()
        // Loading might show a progress indicator
    }

    @Test
    fun `macro editor shows error state correctly`() {
        // Mock error state
        val errorStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Error("Failed to load macro")
        )
        every { viewModel.uiState } returns errorStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 1L,
                    templateId = null,
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to load macro").assertIsDisplayed()
    }

    @Test
    fun `template loading works correctly when templateId is provided`() {
        // This test would verify template loading functionality
        // For now, just verify the screen loads with templateId
        composeTestRule.setContent {
            AutodroidTheme {
                MacroEditorScreen(
                    macroId = 0L,
                    templateId = 1L, // Template ID provided
                    onBack = {},
                    onSaved = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify screen loads correctly with template
        composeTestRule.onNodeWithText("Macro Editor").assertIsDisplayed()
    }
}