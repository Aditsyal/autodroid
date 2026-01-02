package com.aditsyal.autodroid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aditsyal.autodroid.presentation.screens.MacroDetailScreen
import com.aditsyal.autodroid.presentation.screens.MacroEditorScreen
import com.aditsyal.autodroid.presentation.screens.MacroListScreen
import com.aditsyal.autodroid.presentation.screens.TemplateLibraryScreen
import com.aditsyal.autodroid.presentation.ui.ExecutionHistoryScreen
import com.aditsyal.autodroid.presentation.ui.ConflictDetectionScreen
import com.aditsyal.autodroid.presentation.screens.SettingsScreen

private const val MacroListRoute = "macro_list"
private const val MacroDetailRoute = "macro_detail"
private const val MacroEditorRoute = "macro_editor"

private const val TemplateLibraryRoute = "template_library"
private const val ExecutionHistoryRoute = "execution_history"
private const val ConflictDetectionRoute = "conflict_detection"
private const val SettingsRoute = "settings"

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MacroListRoute,
        modifier = modifier
    ) {
        composable(route = MacroListRoute) {
            MacroListScreen(
                onAddMacro = { navController.navigate("$MacroEditorRoute/0") },
                onViewMacro = { id -> navController.navigate("$MacroDetailRoute/$id") },
                onEditMacro = { id -> navController.navigate("$MacroEditorRoute/$id") },
                onShowHistory = { navController.navigate(ExecutionHistoryRoute) },
                onShowConflicts = { navController.navigate(ConflictDetectionRoute) },
                onShowSettings = { navController.navigate(SettingsRoute) },
                onShowTemplates = { navController.navigate(TemplateLibraryRoute) }
            )
        }

        composable(
            route = "$MacroDetailRoute/{macroId}",
            arguments = listOf(
                navArgument("macroId") {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val macroId = backStackEntry.arguments?.getLong("macroId") ?: 0L
            MacroDetailScreen(
                macroId = macroId,
                onBack = { navController.navigateUp() },
                onEdit = { id -> navController.navigate("$MacroEditorRoute/$id") }
            )
        }

        composable(
            route = "$MacroEditorRoute/{macroId}?templateId={templateId}",
            arguments = listOf(
                navArgument("macroId") {
                    type = NavType.LongType
                    defaultValue = 0L
                },
                navArgument("templateId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val macroId = backStackEntry.arguments?.getLong("macroId") ?: 0L
            val templateIdString = backStackEntry.arguments?.getString("templateId")
            val templateId = templateIdString?.toLongOrNull()
            MacroEditorScreen(
                macroId = macroId,
                templateId = templateId,
                onBack = { navController.navigateUp() },
                onSaved = { navController.navigateUp() }
            )
        }

        composable(route = TemplateLibraryRoute) {
            TemplateLibraryScreen(
                onBackClick = { navController.navigateUp() },
                onTemplateSelected = { templateId ->
                    // Navigate to macro editor with the selected template
                    navController.navigate("$MacroEditorRoute/0?templateId=$templateId")
                }
            )
        }

        composable(route = ExecutionHistoryRoute) {
            ExecutionHistoryScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = ConflictDetectionRoute) {
            ConflictDetectionScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable(route = SettingsRoute) {
            SettingsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }
    }
}


