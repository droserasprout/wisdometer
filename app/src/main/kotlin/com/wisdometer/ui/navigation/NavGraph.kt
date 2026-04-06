package com.wisdometer.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.*
import androidx.navigation.compose.*
import com.wisdometer.ui.detail.PredictionDetailScreen
import com.wisdometer.ui.edit.EditPredictionScreen
import com.wisdometer.ui.predictions.PredictionsScreen
import com.wisdometer.ui.profile.ProfileScreen
import com.wisdometer.ui.settings.SettingsScreen

sealed class Route(val path: String) {
    object Predictions : Route("predictions")
    object Profile : Route("profile")
    object Settings : Route("settings")
    object Detail : Route("detail/{predictionId}") {
        fun withId(id: Long) = "detail/$id"
    }
    object Edit : Route("edit?predictionId={predictionId}") {
        fun newPrediction() = "edit"
        fun editExisting(id: Long) = "edit?predictionId=$id"
    }
}

@Composable
fun NavGraph(initialPredictionId: Long? = null) {
    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route

    // Navigate to detail on first launch if opened from notification
    LaunchedEffect(initialPredictionId) {
        initialPredictionId?.let { navController.navigate(Route.Detail.withId(it)) }
    }

    val bottomRoutes = listOf(Route.Predictions.path, Route.Profile.path, Route.Settings.path)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (currentRoute in bottomRoutes) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    NavigationBarItem(
                        selected = currentRoute == Route.Predictions.path,
                        onClick = { navController.navigate(Route.Predictions.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.List, contentDescription = "Predictions") },
                        label = { Text("Predictions") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Profile.path,
                        onClick = { navController.navigate(Route.Profile.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.BarChart, contentDescription = "Profile") },
                        label = { Text("Profile") },
                    )
                    NavigationBarItem(
                        selected = currentRoute == Route.Settings.path,
                        onClick = { navController.navigate(Route.Settings.path) { launchSingleTop = true } },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") },
                    )
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Predictions.path,
            modifier = Modifier.padding(padding),
        ) {
            composable(Route.Predictions.path) {
                PredictionsScreen(
                    onNavigateToDetail = { id -> navController.navigate(Route.Detail.withId(id)) },
                    onNavigateToNew = { navController.navigate(Route.Edit.newPrediction()) },
                )
            }
            composable(Route.Profile.path) {
                ProfileScreen()
            }
            composable(Route.Settings.path) {
                SettingsScreen()
            }
            composable(
                route = Route.Detail.path,
                arguments = listOf(navArgument("predictionId") { type = NavType.LongType }),
            ) { backStack ->
                val id = backStack.arguments!!.getLong("predictionId")
                PredictionDetailScreen(
                    predictionId = id,
                    onBack = { navController.popBackStack() },
                    onEdit = { navController.navigate(Route.Edit.editExisting(id)) },
                )
            }
            composable(
                route = Route.Edit.path,
                arguments = listOf(
                    navArgument("predictionId") {
                        type = NavType.LongType
                        defaultValue = -1L
                    }
                ),
            ) { backStack ->
                val id = backStack.arguments!!.getLong("predictionId").takeIf { it != -1L }
                EditPredictionScreen(
                    predictionId = id,
                    onDone = { navController.popBackStack() },
                )
            }
        }
    }
}
