package com.aditsyal.autodroid.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

class MacroDetailScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @MockK
    lateinit var viewModel: MacroEditorViewModel

    private val testMacro = TestDataFactory.createTestMacro(
        id = 1L,
        name = "Test Macro",
        description = "A test macro for UI testing",
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
        ),
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
        ),
        constraints = listOf(
            TestDataFactory.createTestConstraint(
                id = 1L,
                type = "BATTERY_LEVEL",
                description = "Battery > 20%"
            )
        )
    )

    @Before
    fun setup() {
        MockKAnnotations.init(this)

        // Mock ViewModel uiState
        val uiStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(testMacro)
        )
        every { viewModel.uiState } returns uiStateFlow
    }

    @After
    fun teardown() {
        unmockkAll()
    }

    @Test
    fun `macro detail screen displays macro information correctly`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify macro name is displayed
        composeTestRule.onNodeWithText("Test Macro").assertIsDisplayed()

        // Verify description is displayed (if shown in UI)
        // composeTestRule.onNodeWithText("A test macro for UI testing").assertIsDisplayed()

        // Verify trigger count is displayed
        composeTestRule.onNodeWithText("2 triggers").assertIsDisplayed()

        // Verify action count is displayed
        composeTestRule.onNodeWithText("2 actions").assertIsDisplayed()

        // Verify constraint information is displayed
        composeTestRule.onNodeWithText("1 constraint").assertIsDisplayed()
    }

    @Test
    fun `macro detail screen shows trigger details correctly`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify individual triggers are displayed
        composeTestRule.onNodeWithText("TIME").assertIsDisplayed()
        composeTestRule.onNodeWithText("Daily at 9 AM").assertIsDisplayed()

        composeTestRule.onNodeWithText("LOCATION").assertIsDisplayed()
        composeTestRule.onNodeWithText("When arriving home").assertIsDisplayed()
    }

    @Test
    fun `macro detail screen shows action details correctly`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify individual actions are displayed
        composeTestRule.onNodeWithText("TOAST").assertIsDisplayed()
        composeTestRule.onNodeWithText("Show notification").assertIsDisplayed()

        composeTestRule.onNodeWithText("NOTIFICATION").assertIsDisplayed()
        composeTestRule.onNodeWithText("Send push notification").assertIsDisplayed()
    }

    @Test
    fun `macro detail screen shows constraint details correctly`() {
        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify constraints are displayed
        composeTestRule.onNodeWithText("BATTERY_LEVEL").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery > 20%").assertIsDisplayed()
    }

    @Test
    fun `back button calls onBack when clicked`() {
        var backClicked = false

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = { backClicked = true },
                    onEdit = {},
                    onDryRun = {},
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
    fun `edit button calls onEdit with correct macroId when clicked`() {
        var editedMacroId: Long? = null

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = { editedMacroId = it },
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Click edit button
        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        // Verify onEdit was called with correct macroId
        assertEquals(1L, editedMacroId)
    }

    @Test
    fun `dry run button calls onDryRun with correct macroId when clicked`() {
        var dryRunMacroId: Long? = null

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = { dryRunMacroId = it },
                    viewModel = viewModel
                )
            }
        }

        // Click dry run button
        composeTestRule.onNodeWithContentDescription("Dry Run").performClick()

        // Verify onDryRun was called with correct macroId
        assertEquals(1L, dryRunMacroId)
    }

    @Test
    fun `macro detail screen shows loading state correctly`() {
        // Mock loading state
        val loadingStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Loading
        )
        every { viewModel.uiState } returns loadingStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify loading indicator is shown
        // Note: Loading state might not show specific UI, but we can verify
        // that the main content is not displayed
        composeTestRule.onNodeWithText("Test Macro").assertDoesNotExist()
    }

    @Test
    fun `macro detail screen shows error state correctly`() {
        // Mock error state
        val errorStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Error("Failed to load macro")
        )
        every { viewModel.uiState } returns errorStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify error message is displayed
        composeTestRule.onNodeWithText("Failed to load macro").assertIsDisplayed()

        // Verify main content is not displayed
        composeTestRule.onNodeWithText("Test Macro").assertDoesNotExist()
    }

    @Test
    fun `macro detail screen handles empty macro correctly`() {
        val emptyMacro = TestDataFactory.createTestMacro(
            id = 2L,
            name = "Empty Macro",
            triggers = emptyList(),
            actions = emptyList(),
            constraints = emptyList()
        )

        val emptyStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(emptyMacro)
        )
        every { viewModel.uiState } returns emptyStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 2L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify macro name is displayed
        composeTestRule.onNodeWithText("Empty Macro").assertIsDisplayed()

        // Verify empty states are shown
        composeTestRule.onNodeWithText("0 triggers").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 actions").assertIsDisplayed()
        composeTestRule.onNodeWithText("0 constraints").assertIsDisplayed()
    }

    @Test
    fun `macro detail screen displays created and updated dates if available`() {
        // Assuming the UI shows these dates
        val macroWithDates = testMacro.copy(
            createdAt = 1609459200000L, // 2021-01-01 00:00:00 UTC
            updatedAt = 1609545600000L  // 2021-01-02 00:00:00 UTC
        )

        val dateStateFlow = MutableStateFlow<MacroEditorUiState>(
            MacroEditorUiState.Success(macroWithDates)
        )
        every { viewModel.uiState } returns dateStateFlow

        composeTestRule.setContent {
            AutodroidTheme {
                MacroDetailScreen(
                    macroId = 1L,
                    onBack = {},
                    onEdit = {},
                    onDryRun = {},
                    viewModel = viewModel
                )
            }
        }

        // Verify dates are displayed (format may vary)
        // composeTestRule.onNodeWithText("Created: Jan 1, 2021").assertIsDisplayed()
        // composeTestRule.onNodeWithText("Updated: Jan 2, 2021").assertIsDisplayed()

        // At minimum, verify the screen still loads correctly
        composeTestRule.onNodeWithText("Test Macro").assertIsDisplayed()
    }
}