package com.aditsyal.autodroid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aditsyal.autodroid.presentation.screens.MacroEditorScreen
import com.aditsyal.autodroid.presentation.screens.MacroListScreen
import com.aditsyal.autodroid.presentation.ui.ExecutionHistoryScreen
import com.aditsyal.autodroid.presentation.ui.ConflictDetectionScreen

private const val MacroListRoute = "macro_list"
private const val MacroEditorRoute = "macro_editor"
private const val ExecutionHistoryRoute = "execution_history"
private const val ConflictDetectionRoute = "conflict_detection"

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
                onEditMacro = { id -> navController.navigate("$MacroEditorRoute/$id") },
                onShowHistory = { navController.navigate(ExecutionHistoryRoute) },
                onShowConflicts = { navController.navigate(ConflictDetectionRoute) }
            )
        }

        composable(
            route = "$MacroEditorRoute/{macroId}",
            arguments = listOf(
                navArgument("macroId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val macroId = backStackEntry.arguments?.getLong("macroId") ?: 0L
            MacroEditorScreen(
                macroId = macroId,
                onBack = { navController.navigateUp() },
                onSaved = { navController.navigateUp() }
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
    }
}


