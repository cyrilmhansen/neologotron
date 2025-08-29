package com.neologotron.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.neologotron.app.navigation.NavItems
import com.neologotron.app.navigation.Route
import com.neologotron.app.ui.screens.FavoritesScreen
import com.neologotron.app.ui.screens.HistoryScreen
import com.neologotron.app.ui.screens.MainScreen
import com.neologotron.app.ui.screens.SettingsScreen
import com.neologotron.app.ui.screens.ThematicScreen
import com.neologotron.app.ui.screens.WorkshopScreen
import com.neologotron.app.ui.screens.WordDetailScreen
import com.neologotron.app.ui.screens.DebugScreen
import com.neologotron.app.ui.screens.AboutScreen

@Composable
fun WordTheatreHost() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            val backStack by navController.currentBackStackEntryAsState()
            val currentRoute = backStack?.destination?.route
            val bottomRoutes = NavItems.bottomBar.map { it.route.value }.toSet()
            // Determine which bottom tab should appear selected; if on detail, map to parent 'from' arg.
            val selectedTabRoute = when (currentRoute) {
                Route.Detail.value -> {
                    val from = backStack?.arguments?.getString(Route.Detail.fromArg)
                    if (from != null && bottomRoutes.contains(from)) from else Route.Main.value
                }
                else -> currentRoute
            } ?: Route.Main.value
            NavigationBar {
                NavItems.bottomBar.forEach { item ->
                    val route = item.route.value
                    NavigationBarItem(
                        selected = selectedTabRoute == route,
                        onClick = {
                            // Prefer popping back to an existing instance of the destination if present
                            val popped = navController.popBackStack(route, inclusive = false)
                            if (!popped) {
                                navController.navigate(route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = stringResource(id = item.titleRes)) },
                        label = { Text(text = stringResource(id = item.titleRes)) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Route.Main.value,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding)
        ) {
            composable(Route.Main.value) {
                MainScreen(
                    onOpenThematic = { navController.navigate(Route.Thematic.value) },
                    onOpenWorkshop = { navController.navigate(Route.Workshop.value) }
                )
            }
            composable(Route.History.value) {
                HistoryScreen(onOpenDetail = { w -> navController.navigate(Route.Detail.build(w, Route.History)) })
            }
            composable(Route.Favorites.value) {
                FavoritesScreen(onOpenDetail = { w -> navController.navigate(Route.Detail.build(w, Route.Favorites)) })
            }
            composable(Route.Settings.value) {
                SettingsScreen(
                    onOpenDebug = { navController.navigate(Route.Debug.value) },
                    onOpenAbout = { navController.navigate(Route.About.value) }
                )
            }
            composable(Route.Thematic.value) {
                ThematicScreen(onOpenDetail = { w -> navController.navigate(Route.Detail.build(w, Route.Thematic)) })
            }
            composable(Route.Workshop.value) {
                WorkshopScreen(
                    onOpenDetail = { w, def, decomp ->
                        navController.navigate(Route.Detail.build(w, Route.Workshop, def = def, decomp = decomp))
                    }
                )
            }
            composable(Route.Debug.value) { DebugScreen() }
            composable(Route.About.value) { AboutScreen(onBack = { navController.popBackStack() }) }
            composable(
                route = Route.Detail.value,
                arguments = listOf(
                    navArgument(Route.Detail.argName) { type = NavType.StringType },
                    navArgument(Route.Detail.fromArg) { type = NavType.StringType; defaultValue = "" },
                    navArgument(Route.Detail.defArg) { type = NavType.StringType; defaultValue = "" },
                    navArgument(Route.Detail.decompArg) { type = NavType.StringType; defaultValue = "" }
                )
            ) {
                WordDetailScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
