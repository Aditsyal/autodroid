package com.aditsyal.autodroid.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.aditsyal.autodroid.presentation.viewmodels.SettingsUiState
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun settingsScreen_displaysTitle() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysDataAndVariablesSection() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Data & Variables").assertIsDisplayed()
        composeTestRule.onNodeWithText("Global Variables").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sidebar Launcher").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAppearanceSection() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Appearance").assertIsDisplayed()
        composeTestRule.onNodeWithText("AMOLED Dark Mode").assertIsDisplayed()
        composeTestRule.onNodeWithText("Haptic Feedback").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysServiceStatusSection() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(isWorkManagerRunning = true),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Service Status").assertIsDisplayed()
        composeTestRule.onNodeWithText("Background Monitoring").assertIsDisplayed()
        composeTestRule.onNodeWithText("Active").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysInactiveServiceStatus() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(isWorkManagerRunning = false),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Inactive").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysPermissionsSection() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(
                    isAccessibilityEnabled = true,
                    isBatteryOptimizationDisabled = true,
                    isNotificationPermissionGranted = true
                ),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Permissions").assertIsDisplayed()
        composeTestRule.onNodeWithText("Accessibility Service").assertIsDisplayed()
        composeTestRule.onNodeWithText("Battery Optimization").assertIsDisplayed()
        composeTestRule.onNodeWithText("Notifications").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysAmoledModeToggle() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(isAmoledMode = true),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("AMOLED Dark Mode").assertIsDisplayed()
    }

    @Test
    fun settingsScreen_displaysHapticFeedbackToggle() {
        composeTestRule.setContent {
            SettingsScreenContent(
                uiState = SettingsUiState(isHapticFeedbackEnabled = true),
                onBackClick = {},
                onNavigateToVariables = {},
                onRefreshStatus = {},
                onToggleSidebar = { false },
                onSetAmoledMode = {},
                onSetHapticFeedback = {}
            )
        }

        composeTestRule.onNodeWithText("Haptic Feedback").assertIsDisplayed()
    }
}
