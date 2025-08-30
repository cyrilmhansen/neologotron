package com.neologotron.app.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.neologotron.app.navigation.NavItems
import com.neologotron.app.navigation.Route
import com.neologotron.app.ui.screens.AboutScreen
import com.neologotron.app.ui.screens.DebugScreen
import com.neologotron.app.ui.screens.FavoritesScreen
import com.neologotron.app.ui.screens.HistoryScreen
import com.neologotron.app.ui.screens.MainScreen
import com.neologotron.app.ui.screens.OnboardingFlow
import com.neologotron.app.ui.screens.SettingsScreen
import com.neologotron.app.ui.screens.ThematicScreen
import com.neologotron.app.ui.screens.WorkshopScreen
import com.neologotron.app.ui.screens.WordDetailScreen
import com.neologotron.app.ui.viewmodel.OnboardingViewModel

@Composable
fun WordTheatreHost() {
    val navController = rememberNavController()
    val onboardingVm: OnboardingViewModel = hiltViewModel()
    val startDest = remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val done = onboardingVm.checkComplete()
        startDest.value = if (done) Route.Main.value else Route.Onboarding.value
    }
    val startDestination = startDest.value
    if (startDestination != null) {
        Scaffold(
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            bottomBar = {
                val backStack by navController.currentBackStackEntryAsState()
                val currentRoute = backStack?.destination?.route
                val bottomRoutes = NavItems.bottomBar.map { it.route.value }.toSet()
                val selectedTabRoute = when (currentRoute) {
                    Route.Detail.value -> {
                        val from = backStack?.arguments?.getString(Route.Detail.fromArg)
                        if (from != null && bottomRoutes.contains(from)) from else Route.Main.value
                    }
                    else -> currentRoute
                } ?: Route.Main.value
                if (currentRoute != Route.Onboarding.value && currentRoute != null) {
                    NavigationBar {
                        NavItems.bottomBar.forEach { item ->
                            val route = item.route.value
                            NavigationBarItem(
                                selected = selectedTabRoute == route,
                                onClick = {
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
                                label = { Text(text = stringResource(id = item.titleRes)) },
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = androidx.compose.ui.Modifier.padding(innerPadding),
            ) {
                composable(Route.Onboarding.value) {
                    OnboardingFlow(
                        onFinished = {
                            navController.navigate(Route.Main.value) {
                                popUpTo(Route.Onboarding.value) { inclusive = true }
                            }
                        },
                    )
                }
                composable(Route.Main.value) {
                    MainScreen(
                        onOpenThematic = { navController.navigate(Route.Thematic.value) },
                        onOpenWorkshop = { navController.navigate(Route.Workshop.value) },
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
                        onOpenAbout = { navController.navigate(Route.About.value) },
                    )
                }
                composable(Route.Thematic.value) {
                    ThematicScreen(
                        onOpenDetail = { w, def, decomp, pform, rform, sform, rgloss, rconn, spos, sdef, stags ->
                            navController.navigate(
                                Route.Detail.build(
                                    w,
                                    Route.Thematic,
                                    def = def,
                                    decomp = decomp,
                                    pform = pform,
                                    rform = rform,
                                    sform = sform,
                                    rgloss = rgloss,
                                    rconn = rconn,
                                    spos = spos,
                                    sdef = sdef,
                                    stags = stags,
                                )
                            )
                        }
                    )
                }
                composable(Route.Workshop.value) {
                    WorkshopScreen(
                        onOpenDetail = { w, def, decomp, pform, rform, sform, rgloss, rconn, spos, sdef, stags ->
                            navController.navigate(
                                Route.Detail.build(
                                    w,
                                    Route.Workshop,
                                    def = def,
                                    decomp = decomp,
                                    pform = pform,
                                    rform = rform,
                                    sform = sform,
                                    rgloss = rgloss,
                                    rconn = rconn,
                                    spos = spos,
                                    sdef = sdef,
                                    stags = stags,
                                )
                            )
                        },
                    )
                }
                composable(Route.Debug.value) {
                    DebugScreen(
                        onShowMessage = { msg ->
                            scope.launch { snackbarHostState.showSnackbar(message = msg) }
                        }
                    )
                }
                composable(Route.About.value) { AboutScreen(onBack = { navController.popBackStack() }) }
                composable(
                    route = Route.Detail.value,
                    arguments = listOf(
                        navArgument(Route.Detail.argName) { type = NavType.StringType },
                        navArgument(Route.Detail.fromArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.defArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.decompArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.pformArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.rformArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.sformArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.rglossArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.rconnArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.sposArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.sdefArg) { type = NavType.StringType; defaultValue = "" },
                        navArgument(Route.Detail.stagsArg) { type = NavType.StringType; defaultValue = "" },
                    ),
                ) {
                    WordDetailScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
