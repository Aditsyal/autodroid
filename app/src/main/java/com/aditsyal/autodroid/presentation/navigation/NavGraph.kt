package com.aditsyal.autodroid.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
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
import com.aditsyal.autodroid.presentation.screens.ImportMacrosScreen
import com.aditsyal.autodroid.presentation.screens.ExportMacrosScreen
import com.aditsyal.autodroid.presentation.ui.ExecutionHistoryScreen
import com.aditsyal.autodroid.presentation.ui.ConflictDetectionScreen
import com.aditsyal.autodroid.presentation.screens.SettingsScreen
import com.aditsyal.autodroid.utils.PerformanceMonitor

private const val MacroListRoute = "macro_list"
private const val MacroDetailRoute = "macro_detail"
private const val MacroEditorRoute = "macro_editor"

private const val TemplateLibraryRoute = "template_library"
private const val ImportMacrosRoute = "import_macros"
private const val ExportMacrosRoute = "export_macros"
private const val ExecutionHistoryRoute = "execution_history"
private const val ConflictDetectionRoute = "conflict_detection"
private const val SettingsRoute = "settings"

@Composable
fun TrackRender(
    performanceMonitor: PerformanceMonitor,
    screenName: String,
    content: @Composable () -> Unit
) {
    val executionId = remember(screenName) { 
        performanceMonitor.startExecution("Render_$screenName") 
    }
    
    SideEffect {
        performanceMonitor.endExecution(executionId, "Render_$screenName")
    }
    
    content()
}

@Composable
fun AppNavGraph(
    modifier: Modifier = Modifier,
    performanceMonitor: PerformanceMonitor
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = MacroListRoute,
        modifier = modifier
    ) {
        composable(route = MacroListRoute) {
            TrackRender(performanceMonitor, "MacroList") {
                MacroListScreen(
                    onAddMacro = { navController.navigate("$MacroEditorRoute/0") },
                    onImportMacros = { navController.navigate(ImportMacrosRoute) },
                    onExportMacros = { navController.navigate(ExportMacrosRoute) },
                    onViewMacro = { id -> navController.navigate("$MacroDetailRoute/$id") },
                    onEditMacro = { id -> navController.navigate("$MacroEditorRoute/$id") },
                    onShowHistory = { navController.navigate(ExecutionHistoryRoute) },
                    onShowConflicts = { navController.navigate(ConflictDetectionRoute) },
                    onShowSettings = { navController.navigate(SettingsRoute) },
                    onShowTemplates = { navController.navigate(TemplateLibraryRoute) }
                )
            }
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
            TrackRender(performanceMonitor, "MacroDetail_$macroId") {
                MacroDetailScreen(
                    macroId = macroId,
                    onBack = { navController.navigateUp() },
                    onEdit = { id -> navController.navigate("$MacroEditorRoute/$id") }
                )
            }
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
            TrackRender(performanceMonitor, "MacroEditor_$macroId") {
                MacroEditorScreen(
                    macroId = macroId,
                    templateId = templateId,
                    onBack = { navController.navigateUp() },
                    onSaved = { navController.navigateUp() }
                )
            }
        }

        composable(route = TemplateLibraryRoute) {
            TrackRender(performanceMonitor, "TemplateLibrary") {
                TemplateLibraryScreen(
                    onBackClick = { navController.navigateUp() },
                    onTemplateSelected = { templateId ->
                        // Navigate to macro editor with the selected template
                        navController.navigate("$MacroEditorRoute/0?templateId=$templateId")
                    }
                )
            }
        }

        composable(route = ImportMacrosRoute) {
            TrackRender(performanceMonitor, "ImportMacros") {
                ImportMacrosScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }

        composable(route = ExportMacrosRoute) {
            TrackRender(performanceMonitor, "ExportMacros") {
                ExportMacrosScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }

        composable(route = ExecutionHistoryRoute) {
            TrackRender(performanceMonitor, "ExecutionHistory") {
                ExecutionHistoryScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }

        composable(route = ConflictDetectionRoute) {
            TrackRender(performanceMonitor, "ConflictDetection") {
                ConflictDetectionScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }

        composable(route = SettingsRoute) {
            TrackRender(performanceMonitor, "Settings") {
                SettingsScreen(
                    onBackClick = { navController.navigateUp() }
                )
            }
        }
    }
}


