package com.aditsyal.autodroid.presentation.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.aditsyal.autodroid.data.local.entities.TemplateEntity
import org.junit.Rule
import org.junit.Test

class TemplateLibraryScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun templateLibrary_displaysTitle() {
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Loading,
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Templates").assertIsDisplayed()
    }

    @Test
    fun loadingState_isDisplayed() {
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Loading,
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }
    }

    @Test
    fun errorState_isDisplayed() {
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Error("Failed to load templates"),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Failed to load templates").assertIsDisplayed()
    }

    @Test
    fun templatesList_isDisplayed() {
        val templates = listOf(
            createTestTemplate(1, "Morning Routine", "Start your day right"),
            createTestTemplate(2, "Night Routine", "Wind down for sleep")
        )

        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Success(templates),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Morning Routine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Start your day right").assertIsDisplayed()
        composeTestRule.onNodeWithText("Night Routine").assertIsDisplayed()
        composeTestRule.onNodeWithText("Wind down for sleep").assertIsDisplayed()
    }

    @Test
    fun searchField_isDisplayed() {
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Success(emptyList()),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Search templates...").assertIsDisplayed()
    }

    @Test
    fun sortOptions_areDisplayed() {
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Success(emptyList()),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Popular").assertIsDisplayed()
        composeTestRule.onNodeWithText("Name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Recent").assertIsDisplayed()
    }

    @Test
    fun onBackClick_isCalled_whenBackButtonClicked() {
        var backClicked = false
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Loading,
                onBackClick = { backClicked = true },
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithContentDescription("Back").performClick()
        assert(backClicked)
    }

    @Test
    fun templateCard_displaysCategory() {
        val template = createTestTemplate(1, "Test Template", "Description", "Productivity")

        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Success(listOf(template)),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Productivity").assertIsDisplayed()
    }

    @Test
    fun templateCard_displaysUsageCount() {
        val template = createTestTemplate(1, "Test Template", "Description").copy(usageCount = 100)

        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Success(listOf(template)),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("100 uses").assertIsDisplayed()
    }

    @Test
    fun filterChip_canBeSelected() {
        composeTestRule.setContent {
            TemplateLibraryScreenContent(
                uiState = TemplateLibraryUiState.Success(emptyList()),
                onBackClick = {},
                onTemplateSelected = {},
                onSearchQueryChange = {},
                onSortByChange = {},
                onShowPreview = {}
            )
        }

        composeTestRule.onNodeWithText("Popular").performClick()
        composeTestRule.onNodeWithText("Name").performClick()
        composeTestRule.onNodeWithText("Recent").performClick()
    }

    private fun createTestTemplate(
        id: Long,
        name: String,
        description: String,
        category: String = "General"
    ): TemplateEntity {
        return TemplateEntity(
            id = id,
            name = name,
            description = description,
            category = category,
            macroJson = "{}",
            usageCount = 10
        )
    }
}
