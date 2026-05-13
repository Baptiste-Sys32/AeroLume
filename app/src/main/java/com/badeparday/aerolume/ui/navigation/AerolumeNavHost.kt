package com.badeparday.aerolume.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navOptions
import com.badeparday.aerolume.data.AppContainer
import com.badeparday.aerolume.ui.adaptive.rememberAerolumeWindowState
import com.badeparday.aerolume.ui.components.BottomBarDestination
import com.badeparday.aerolume.ui.components.PremiumBottomBar
import com.badeparday.aerolume.ui.screen.detail.FlightDetailScreen
import com.badeparday.aerolume.ui.screen.detail.FlightDetailViewModel
import com.badeparday.aerolume.ui.screen.radar.RadarScreen
import com.badeparday.aerolume.ui.screen.radar.RadarViewModel
import com.badeparday.aerolume.ui.screen.saved.SavedFlightsScreen
import com.badeparday.aerolume.ui.screen.saved.SavedFlightsViewModel
import com.badeparday.aerolume.ui.screen.search.SearchScreen
import com.badeparday.aerolume.ui.screen.search.SearchViewModel
import com.badeparday.aerolume.ui.screen.settings.SettingsScreen
import com.badeparday.aerolume.ui.screen.settings.SettingsViewModel

private const val RadarRoute = "radar"
private const val SavedRoute = "saved"
private const val SearchRoute = "search"
private const val SettingsRoute = "settings"
private const val FlightDetailRoute = "detail"

private val TopLevelDestinations =
    listOf(
        BottomBarDestination(route = RadarRoute, label = "Radar", icon = Icons.Rounded.TravelExplore),
        BottomBarDestination(route = SavedRoute, label = "Saved", icon = Icons.Rounded.Bookmark),
        BottomBarDestination(route = SearchRoute, label = "Search", icon = Icons.Rounded.Search),
        BottomBarDestination(route = SettingsRoute, label = "Settings", icon = Icons.Rounded.Tune),
    )

@Composable
fun AerolumeNavHost(
    container: AppContainer,
    windowSizeClass: WindowSizeClass,
) {
    val windowState = rememberAerolumeWindowState(windowSizeClass)
    if (windowState.isTablet) {
        TabletWorkspaceRoot(
            container = container,
            windowState = windowState,
        )
        return
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentTopLevelRoute =
        TopLevelDestinations.firstOrNull { destination ->
            currentDestination?.hierarchy?.any { hierarchyDestination ->
                hierarchyDestination.route?.startsWith(destination.route) == true
            } == true
        }?.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            if (currentTopLevelRoute != null) {
                PremiumBottomBar(
                    currentRoute = currentTopLevelRoute,
                    destinations = TopLevelDestinations,
                    modifier =
                        Modifier
                            .navigationBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    onNavigate = { destination ->
                        navController.navigate(destination.route, navOptions {
                            launchSingleTop = true
                            restoreState = true
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                        })
                    },
                )
            }
        },
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = RadarRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(
                route = "$RadarRoute?focusFlightId={focusFlightId}",
                arguments =
                    listOf(
                        navArgument("focusFlightId") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        },
                    ),
            ) { entry ->
                val viewModel: RadarViewModel = viewModel(factory = RadarViewModel.factory(container))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                RadarScreen(
                    uiState = uiState,
                    focusFlightId = entry.arguments?.getString("focusFlightId"),
                    contentPadding = paddingValues,
                    onOpenSearch = { navController.navigate(SearchRoute) },
                    onOpenSettings = { navController.navigate(SettingsRoute) },
                    onOpenDetails = { flightId -> navController.navigate("$FlightDetailRoute/$flightId") },
                    onToggleSaved = viewModel::toggleSaved,
                )
            }

            composable(SavedRoute) {
                val viewModel: SavedFlightsViewModel = viewModel(factory = SavedFlightsViewModel.factory(container))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SavedFlightsScreen(
                    uiState = uiState,
                    contentPadding = paddingValues,
                    onOpenFlight = { flightId -> navController.navigate("$FlightDetailRoute/$flightId") },
                    onToggleSaved = viewModel::toggleSaved,
                )
            }

            composable(SearchRoute) {
                val viewModel: SearchViewModel = viewModel(factory = SearchViewModel.factory(container))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SearchScreen(
                    uiState = uiState,
                    contentPadding = paddingValues,
                    onQueryChange = viewModel::onQueryChanged,
                    onUseRecent = viewModel::useRecentQuery,
                    onTrackFlight = { flightId ->
                        viewModel.rememberSelection(flightId)
                        navController.navigate("$RadarRoute?focusFlightId=$flightId") {
                            launchSingleTop = true
                        }
                    },
                )
            }

            composable(SettingsRoute) {
                val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(container))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                SettingsScreen(
                    uiState = uiState,
                    contentPadding = paddingValues,
                    onDemoModeChanged = viewModel::setDemoMode,
                    onMotionIntensityChanged = viewModel::setMotionIntensity,
                    onUnitSystemChanged = viewModel::setUnitSystem,
                    onMapAppearanceChanged = viewModel::setMapAppearance,
                )
            }

            composable("$FlightDetailRoute/{flightId}") { entry ->
                val flightId = entry.arguments?.getString("flightId").orEmpty()
                val viewModel: FlightDetailViewModel =
                    viewModel(factory = FlightDetailViewModel.factory(container, flightId))
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                FlightDetailScreen(
                    uiState = uiState,
                    contentPadding = paddingValues,
                    onBack = navController::popBackStack,
                    onToggleSaved = viewModel::toggleSaved,
                )
            }
        }
    }
}
