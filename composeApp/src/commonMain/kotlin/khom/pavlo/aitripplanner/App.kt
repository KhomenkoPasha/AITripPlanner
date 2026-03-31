package khom.pavlo.aitripplanner

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import khom.pavlo.aitripplanner.core.platform.PlatformLanguageManager
import khom.pavlo.aitripplanner.presentation.app.AppViewModel
import khom.pavlo.aitripplanner.presentation.dayroute.DayRouteMapViewModel
import khom.pavlo.aitripplanner.presentation.details.TripDetailsNavigationEvent
import khom.pavlo.aitripplanner.presentation.details.TripDetailsViewModel
import khom.pavlo.aitripplanner.presentation.place.PlaceDetailsViewModel
import khom.pavlo.aitripplanner.presentation.planner.PlannerNavigationEvent
import khom.pavlo.aitripplanner.presentation.planner.PlannerViewModel
import khom.pavlo.aitripplanner.presentation.saved.SavedTripsViewModel
import khom.pavlo.aitripplanner.domain.model.resolveDarkTheme
import khom.pavlo.aitripplanner.ui.components.BottomNavigationBar
import khom.pavlo.aitripplanner.ui.components.PlatformBackHandler
import khom.pavlo.aitripplanner.ui.components.rememberPlatformCloseApp
import khom.pavlo.aitripplanner.ui.navigation.AppRoute
import khom.pavlo.aitripplanner.ui.navigation.BottomTab
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreen
import khom.pavlo.aitripplanner.ui.screens.dayroute.DayRouteMapScreen
import khom.pavlo.aitripplanner.ui.screens.place.PlaceDetailsScreen
import khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreen
import khom.pavlo.aitripplanner.ui.screens.saved.SavedTripsScreen
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform

@Composable
fun App() {
    val koin = remember { KoinPlatform.getKoin() }
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
    val systemDarkTheme = isSystemInDarkTheme()
    val closeApp = rememberPlatformCloseApp()

    AppTheme(darkTheme = appState.selectedTheme.resolveDarkTheme(systemDarkTheme)) {
        LaunchedEffect(appState.selectedLanguage) {
            PlatformLanguageManager.apply(appState.selectedLanguage)
        }

        LaunchedEffect(appState.closeAppRequestId) {
            if (appState.closeAppRequestId > 0) {
                closeApp()
            }
        }

        PlatformBackHandler(
            enabled = true,
            onBack = appViewModel::handleBackPress,
        )

        val strings = appStrings()

        if (appState.isExitDialogVisible) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = appViewModel::dismissExitDialog,
                title = {
                    androidx.compose.material3.Text(text = strings.closeAppTitle)
                },
                text = {
                    androidx.compose.material3.Text(text = strings.closeAppMessage)
                },
                confirmButton = {
                    androidx.compose.material3.TextButton(onClick = appViewModel::confirmExitDialog) {
                        androidx.compose.material3.Text(text = strings.exitAppAction)
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(onClick = appViewModel::dismissExitDialog) {
                        androidx.compose.material3.Text(text = strings.cancelAction)
                    }
                },
            )
        }

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
                when {
                    initialState.isTopLevelRoute() && targetState.isTopLevelRoute() -> {
                        ContentTransform(
                            targetContentEnter = fadeIn(animationSpec = tween(120)),
                            initialContentExit = fadeOut(animationSpec = tween(90)),
                            sizeTransform = SizeTransform(clip = false),
                        )
                    }

                    targetState.navigationDepth() > initialState.navigationDepth() -> {
                        (slideInHorizontally(
                            animationSpec = tween(220),
                            initialOffsetX = { width -> width / 10 },
                        ) + fadeIn(animationSpec = tween(180))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(180),
                                targetOffsetX = { width -> -width / 14 },
                            ) + fadeOut(animationSpec = tween(140)))
                    }

                    else -> {
                        (slideInHorizontally(
                            animationSpec = tween(200),
                            initialOffsetX = { width -> -width / 14 },
                        ) + fadeIn(animationSpec = tween(170))) togetherWith
                            (slideOutHorizontally(
                                animationSpec = tween(170),
                                targetOffsetX = { width -> width / 10 },
                            ) + fadeOut(animationSpec = tween(130)))
                    }
                }
            },
            label = "root_destination",
        ) { currentRoute ->
            when (currentRoute) {
                is AppRoute.Planner -> PlannerScreen(
                    state = plannerState,
                    selectedLanguage = appState.selectedLanguage,
                    selectedTheme = appState.selectedTheme,
                    onLanguageSelected = appViewModel::setLanguage,
                    onThemeSelected = appViewModel::setTheme,
                    onCityChange = plannerViewModel::onCityChange,
                    onCitySuggestionClick = plannerViewModel::onCitySuggestionSelected,
                    onTitleChange = plannerViewModel::onTitleChange,
                    onPromptChange = plannerViewModel::onPromptChange,
                    onDaysChange = plannerViewModel::onDaysChange,
                    onPlaceCountChange = plannerViewModel::onPlaceCountChange,
                    onWalkingMinutesPerDayChange = plannerViewModel::onWalkingMinutesPerDayChange,
                    onTravelModeChange = plannerViewModel::onTravelModeChange,
                    onInterestToggle = plannerViewModel::onInterestToggle,
                    onPaceSelected = plannerViewModel::onPaceSelected,
                    onBudgetSelected = plannerViewModel::onBudgetSelected,
                    onCompanionTypeSelected = plannerViewModel::onCompanionTypeSelected,
                    onPreferenceToggle = plannerViewModel::onPreferenceToggle,
                    onWithChildrenToggle = plannerViewModel::onWithChildrenToggle,
                    onHeroNoteChange = plannerViewModel::onHeroNoteChange,
                    onSaveClick = plannerViewModel::onSaveTrip,
                    onBackClick = appViewModel::closeEditTrip,
                    onTripClick = { appViewModel.openTripDetails(it, BottomTab.CREATE_NEW_TRIP) },
                    bottomBar = bottomBar,
                )

                AppRoute.MyTrips -> SavedTripsScreen(
                    state = savedTripsState,
                    selectedLanguage = appState.selectedLanguage,
                    selectedTheme = appState.selectedTheme,
                    onLanguageSelected = appViewModel::setLanguage,
                    onThemeSelected = appViewModel::setTheme,
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
                        selectedTheme = appState.selectedTheme,
                        onLanguageSelected = appViewModel::setLanguage,
                        onThemeSelected = appViewModel::setTheme,
                        onBackClick = appViewModel::navigateBack,
                        onEditClick = { appViewModel.openEditTrip(currentRoute.tripId) },
                        onDeleteClick = detailsViewModel::showDeleteDialog,
                        onDismissDelete = detailsViewModel::hideDeleteDialog,
                        onConfirmDelete = detailsViewModel::confirmDelete,
                        onToggleDay = detailsViewModel::onToggleDay,
                        onPlaceCompletionChange = detailsViewModel::onPlaceCompletionChange,
                        onDeletePlace = detailsViewModel::onDeletePlace,
                        onOpenPlace = { dayId, placeId ->
                            appViewModel.openPlaceDetails(
                                tripId = currentRoute.tripId,
                                dayId = dayId,
                                placeId = placeId,
                                originTab = currentRoute.originTab,
                            )
                        },
                        onOpenDayRouteMap = { dayId ->
                            appViewModel.openDayRouteMap(
                                tripId = currentRoute.tripId,
                                dayId = dayId,
                                originTab = currentRoute.originTab,
                            )
                        },
                        bottomBar = bottomBar,
                    )
                }

                is AppRoute.DayRouteMap -> {
                    val dayRouteMapViewModel = remember(currentRoute.tripId, currentRoute.dayId) {
                        koin.get<DayRouteMapViewModel> {
                            parametersOf(currentRoute.tripId, currentRoute.dayId)
                        }
                    }
                    DisposableEffect(dayRouteMapViewModel) {
                        onDispose { dayRouteMapViewModel.clear() }
                    }
                    val dayRouteMapState by dayRouteMapViewModel.state.collectAsState()

                    DayRouteMapScreen(
                        state = dayRouteMapState,
                        onBackClick = appViewModel::navigateBack,
                        bottomBar = bottomBar,
                    )
                }

                is AppRoute.PlaceDetails -> {
                    val placeDetailsViewModel = remember(
                        currentRoute.tripId,
                        currentRoute.dayId,
                        currentRoute.placeId,
                    ) {
                        koin.get<PlaceDetailsViewModel> {
                            parametersOf(currentRoute.tripId, currentRoute.dayId, currentRoute.placeId)
                        }
                    }
                    DisposableEffect(placeDetailsViewModel) {
                        onDispose { placeDetailsViewModel.clear() }
                    }
                    val placeDetailsState by placeDetailsViewModel.state.collectAsState()

                    PlaceDetailsScreen(
                        state = placeDetailsState,
                        onBackClick = appViewModel::navigateBack,
                        onVisitedChange = placeDetailsViewModel::onVisitedChange,
                        onPhotoPicked = placeDetailsViewModel::onPhotoPicked,
                        onDeletePhoto = placeDetailsViewModel::onDeletePhoto,
                        onPhotoPickerError = placeDetailsViewModel::onPhotoPickerError,
                        bottomBar = bottomBar,
                    )
                }
            }
        }
    }
}

private fun AppRoute.isTopLevelRoute(): Boolean = when (this) {
    is AppRoute.Planner -> true
    AppRoute.MyTrips -> true
    is AppRoute.TripDetails -> false
    is AppRoute.DayRouteMap -> false
    is AppRoute.PlaceDetails -> false
}

private fun AppRoute.navigationDepth(): Int = when (this) {
    is AppRoute.Planner -> 0
    AppRoute.MyTrips -> 0
    is AppRoute.TripDetails -> 1
    is AppRoute.DayRouteMap -> 2
    is AppRoute.PlaceDetails -> 2
}
