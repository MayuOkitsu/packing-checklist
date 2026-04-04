package com.mochimono.packingchecklist.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mochimono.packingchecklist.ui.screen.*

@Composable
fun PackingNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ChecklistList.route,
        modifier = modifier
    ) {
        composable(Screen.ChecklistList.route) {
            ChecklistListScreen(navController)
        }
        composable(
            route = Screen.ChecklistDetail.route,
            arguments = listOf(navArgument("checklistId") { type = NavType.StringType })
        ) { backStackEntry ->
            val checklistId = backStackEntry.arguments?.getString("checklistId") ?: return@composable
            ChecklistDetailScreen(checklistId = checklistId, navController = navController)
        }
        composable(Screen.CreateChecklist.route) {
            CreateChecklistScreen(navController)
        }
        composable(Screen.TemplateSelection.route) {
            TemplateSelectionScreen(navController)
        }
        composable(Screen.TemplateManagement.route) {
            TemplateManagementScreen(navController)
        }
        composable(
            route = Screen.CustomTemplateEdit.route,
            arguments = listOf(navArgument("templateId") { type = NavType.StringType })
        ) { backStackEntry ->
            val templateId = backStackEntry.arguments?.getString("templateId") ?: return@composable
            CustomTemplateEditScreen(templateId = templateId, navController = navController)
        }
        composable(Screen.BagManagement.route) {
            BagManagementScreen(navController)
        }
        composable(Screen.FamilyGroup.route) {
            FamilyGroupScreen(navController)
        }
    }
}
