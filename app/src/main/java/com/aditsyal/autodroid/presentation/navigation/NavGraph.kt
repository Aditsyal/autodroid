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

private const val MacroListRoute = "macro_list"
private const val MacroEditorRoute = "macro_editor"

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
                onEditMacro = { id -> navController.navigate("$MacroEditorRoute/$id") }
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
    }
}


