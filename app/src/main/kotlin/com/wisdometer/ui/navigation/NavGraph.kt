package com.wisdometer.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.*
import androidx.navigation.compose.*
import kotlinx.coroutines.launch
import com.wisdometer.ui.detail.PredictionDetailScreen
import com.wisdometer.ui.edit.EditPredictionScreen
import com.wisdometer.ui.predictions.PredictionsScreen
import com.wisdometer.ui.profile.ProfileScreen
import com.wisdometer.ui.settings.SettingsScreen
import com.wisdometer.ui.welcome.WelcomeScreen

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

private const val PREFS_NAME = "wisdometer_settings"
private const val KEY_WELCOME_SEEN = "welcome_seen"

@Composable
fun NavGraph(initialPredictionId: Long? = null) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    var showWelcome by remember { mutableStateOf(!prefs.getBoolean(KEY_WELCOME_SEEN, false)) }

    if (showWelcome) {
        WelcomeScreen(onGetStarted = {
            prefs.edit().putBoolean(KEY_WELCOME_SEEN, true).apply()
            showWelcome = false
        })
        return
    }

    val navController = rememberNavController()
    val navBackStack by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStack?.destination?.route
    val pagerState = rememberPagerState { 3 }
    val coroutineScope = rememberCoroutineScope()

    // Navigate to detail on first launch if opened from notification
    LaunchedEffect(initialPredictionId) {
        initialPredictionId?.let { navController.navigate(Route.Detail.withId(it)) }
    }

    val overlayRoutes = listOf(Route.Detail.path, Route.Edit.path)
    val showBottomBar = currentRoute !in overlayRoutes

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.background) {
                    val tabs = listOf(
                        Triple(0, Icons.Default.List, "Predictions"),
                        Triple(1, Icons.Default.BarChart, "Profile"),
                        Triple(2, Icons.Default.Settings, "Settings"),
                    )
                    tabs.forEach { (index, icon, label) ->
                        NavigationBarItem(
                            selected = pagerState.currentPage == index,
                            onClick = {
                                coroutineScope.launch { pagerState.animateScrollToPage(index) }
                            },
                            icon = { Icon(icon, contentDescription = label) },
                            label = { Text(label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                beyondViewportPageCount = 2,
            ) { page ->
                when (page) {
                    0 -> PredictionsScreen(
                        onNavigateToDetail = { id -> navController.navigate(Route.Detail.withId(id)) },
                        onNavigateToNew = { navController.navigate(Route.Edit.newPrediction()) },
                    )
                    1 -> ProfileScreen()
                    2 -> SettingsScreen()
                }
            }

            NavHost(
                navController = navController,
                startDestination = "empty",
            ) {
                composable("empty") {}
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
}
