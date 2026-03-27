package khom.pavlo.aitripplanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import khom.pavlo.aitripplanner.core.platform.PlatformLanguageManager
import khom.pavlo.aitripplanner.presentation.app.AppViewModel
import khom.pavlo.aitripplanner.presentation.details.TripDetailsNavigationEvent
import khom.pavlo.aitripplanner.presentation.details.TripDetailsViewModel
import khom.pavlo.aitripplanner.presentation.planner.PlannerNavigationEvent
import khom.pavlo.aitripplanner.presentation.planner.PlannerViewModel
import khom.pavlo.aitripplanner.presentation.saved.SavedTripsViewModel
import khom.pavlo.aitripplanner.ui.components.BottomNavigationBar
import khom.pavlo.aitripplanner.ui.navigation.AppRoute
import khom.pavlo.aitripplanner.ui.navigation.BottomTab
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreen
import khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreen
import khom.pavlo.aitripplanner.ui.screens.saved.SavedTripsScreen
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import org.koin.core.context.GlobalContext
import org.koin.core.parameter.parametersOf

@Composable
fun App() {
    AppTheme {
        val koin = remember { GlobalContext.get() }
        val appViewModel = remember { koin.get<AppViewModel>() }
        val plannerViewModel = remember { koin.get<PlannerViewModel>() }
        val savedTripsViewModel = remember { koin.get<SavedTripsViewModel>() }

        DisposableEffect(Unit) {
            onDispose {
                appViewModel.clear()
                plannerViewModel.clear()
                savedTripsViewModel.clear()
            }
        }

        val appState by appViewModel.state.collectAsState()
        val plannerState by plannerViewModel.state.collectAsState()
        val savedTripsState by savedTripsViewModel.state.collectAsState()

        LaunchedEffect(appState.selectedLanguage) {
            PlatformLanguageManager.apply(appState.selectedLanguage)
        }

        val strings = appStrings()

        LaunchedEffect(appState.currentRoute) {
            val route = appState.currentRoute
            if (route is AppRoute.Planner) {
                plannerViewModel.setMode(route.mode)
            }
        }

        LaunchedEffect(plannerViewModel) {
            plannerViewModel.events.collect { event ->
                when (event) {
                    is PlannerNavigationEvent.TripSaved -> appViewModel.onTripSaved(event.mode, event.tripId)
                }
            }
        }

        val bottomBar: @Composable () -> Unit = {
            BottomNavigationBar(
                selectedTab = appState.selectedTab,
                strings = strings,
                onTabSelected = { tab ->
                    when (tab) {
                        BottomTab.CREATE_NEW_TRIP -> appViewModel.openCreateTrip()
                        BottomTab.MY_TRIPS -> appViewModel.openMyTrips()
                    }
                },
            )
        }

        AnimatedContent(
            targetState = appState.currentRoute,
            transitionSpec = {
                (slideInHorizontally(
                    animationSpec = tween(260),
                    initialOffsetX = { width -> width / 8 },
                ) + fadeIn(animationSpec = tween(260))) togetherWith
                    (slideOutHorizontally(
                        animationSpec = tween(220),
                        targetOffsetX = { width -> -width / 10 },
                    ) + fadeOut(animationSpec = tween(220)))
            },
            label = "root_destination",
        ) { currentRoute ->
            when (currentRoute) {
                is AppRoute.Planner -> PlannerScreen(
                    state = plannerState,
                    selectedLanguage = appState.selectedLanguage,
                    onLanguageSelected = appViewModel::setLanguage,
                    onCityChange = plannerViewModel::onCityChange,
                    onTitleChange = plannerViewModel::onTitleChange,
                    onSummaryChange = plannerViewModel::onSummaryChange,
                    onHeroNoteChange = plannerViewModel::onHeroNoteChange,
                    onSaveClick = plannerViewModel::onSaveTrip,
                    onBackClick = appViewModel::closeEditTrip,
                    onTripClick = { appViewModel.openTripDetails(it, BottomTab.CREATE_NEW_TRIP) },
                    bottomBar = bottomBar,
                )

                AppRoute.MyTrips -> SavedTripsScreen(
                    state = savedTripsState,
                    selectedLanguage = appState.selectedLanguage,
                    onLanguageSelected = appViewModel::setLanguage,
                    onTripClick = { appViewModel.openTripDetails(it, BottomTab.MY_TRIPS) },
                    onEditTrip = appViewModel::openEditTrip,
                    onDeleteTrip = savedTripsViewModel::requestDelete,
                    onDismissDelete = savedTripsViewModel::dismissDelete,
                    onConfirmDelete = savedTripsViewModel::confirmDelete,
                    bottomBar = bottomBar,
                )

                is AppRoute.TripDetails -> {
                    val detailsViewModel = remember(currentRoute.tripId) {
                        koin.get<TripDetailsViewModel> { parametersOf(currentRoute.tripId) }
                    }
                    DisposableEffect(detailsViewModel) {
                        onDispose { detailsViewModel.clear() }
                    }
                    val detailsState by detailsViewModel.state.collectAsState()

                    LaunchedEffect(detailsViewModel) {
                        detailsViewModel.events.collect { event ->
                            when (event) {
                                is TripDetailsNavigationEvent.TripDeleted -> appViewModel.onDeleteCompleted()
                            }
                        }
                    }

                    TripDetailsScreen(
                        state = detailsState,
                        selectedLanguage = appState.selectedLanguage,
                        onLanguageSelected = appViewModel::setLanguage,
                        onBackClick = {
                            when (currentRoute.originTab) {
                                BottomTab.CREATE_NEW_TRIP -> appViewModel.openCreateTrip()
                                BottomTab.MY_TRIPS -> appViewModel.openMyTrips()
                            }
                        },
                        onEditClick = { appViewModel.openEditTrip(currentRoute.tripId) },
                        onDeleteClick = detailsViewModel::showDeleteDialog,
                        onDismissDelete = detailsViewModel::hideDeleteDialog,
                        onConfirmDelete = detailsViewModel::confirmDelete,
                        onToggleDay = detailsViewModel::onToggleDay,
                        bottomBar = bottomBar,
                    )
                }
            }
        }
    }
}
