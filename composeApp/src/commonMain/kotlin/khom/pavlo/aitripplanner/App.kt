package khom.pavlo.aitripplanner

import aitripplanner.composeapp.generated.resources.Res
import aitripplanner.composeapp.generated.resources.auth_continue_offline_action
import aitripplanner.composeapp.generated.resources.auth_login_action
import aitripplanner.composeapp.generated.resources.auth_restore_subtitle
import aitripplanner.composeapp.generated.resources.auth_restore_title
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.core.platform.PlatformLanguageManager
import khom.pavlo.aitripplanner.presentation.auth.AuthViewModel
import khom.pavlo.aitripplanner.presentation.app.AppViewModel
import khom.pavlo.aitripplanner.presentation.dayroute.DayRouteMapViewModel
import khom.pavlo.aitripplanner.presentation.details.TripDetailsNavigationEvent
import khom.pavlo.aitripplanner.presentation.details.TripDetailsViewModel
import khom.pavlo.aitripplanner.presentation.place.PlaceDetailsViewModel
import khom.pavlo.aitripplanner.presentation.planner.PlannerNavigationEvent
import khom.pavlo.aitripplanner.presentation.planner.PlannerViewModel
import khom.pavlo.aitripplanner.presentation.saved.SavedTripsViewModel
import khom.pavlo.aitripplanner.domain.model.resolveDarkTheme
import khom.pavlo.aitripplanner.presentation.label
import khom.pavlo.aitripplanner.ui.components.BottomNavigationBar
import khom.pavlo.aitripplanner.ui.components.LoadingStateView
import khom.pavlo.aitripplanner.ui.components.PlatformBackHandler
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.rememberPlatformCloseApp
import khom.pavlo.aitripplanner.ui.navigation.AppRoute
import khom.pavlo.aitripplanner.ui.navigation.BottomTab
import khom.pavlo.aitripplanner.ui.navigation.AuthRoute
import khom.pavlo.aitripplanner.ui.screens.auth.LoginScreen
import khom.pavlo.aitripplanner.ui.screens.auth.RegisterScreen
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreen
import khom.pavlo.aitripplanner.ui.screens.dayroute.DayRouteMapScreen
import khom.pavlo.aitripplanner.ui.screens.place.PlaceDetailsScreen
import khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreen
import khom.pavlo.aitripplanner.ui.screens.profile.ProfileScreen
import khom.pavlo.aitripplanner.ui.screens.saved.SavedTripsScreen
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import org.koin.core.parameter.parametersOf
import org.koin.mp.KoinPlatform
import org.jetbrains.compose.resources.stringResource

@Composable
fun App() {
    val koin = remember { KoinPlatform.getKoin() }
    val appViewModel = remember { koin.get<AppViewModel>() }
    val authViewModel = remember { koin.get<AuthViewModel>() }
    val plannerViewModel = remember { koin.get<PlannerViewModel>() }
    val savedTripsViewModel = remember { koin.get<SavedTripsViewModel>() }

    DisposableEffect(Unit) {
        onDispose {
            appViewModel.clear()
            authViewModel.clear()
            plannerViewModel.clear()
            savedTripsViewModel.clear()
        }
    }

    val appState by appViewModel.state.collectAsState()
    val authUiState by authViewModel.state.collectAsState()
    val plannerState by plannerViewModel.state.collectAsState()
    val savedTripsState by savedTripsViewModel.state.collectAsState()
    val systemDarkTheme = isSystemInDarkTheme()
    val closeApp = rememberPlatformCloseApp()
    val canUseApp = authUiState.authState.canUseApp
    val isOfflineMode = authUiState.authState.isOfflineMode

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
            onBack = {
                if (canUseApp) {
                    appViewModel.handleBackPress()
                } else if (!authViewModel.handleBackPress()) {
                    appViewModel.showExitDialog()
                }
            },
        )

        val strings = appStrings()
        val offlineActionLabel = stringResource(Res.string.auth_continue_offline_action)
        val profilePreferenceLabels = buildProfilePreferenceLabels(
            state = plannerState,
            strings = strings,
            language = appState.selectedLanguage,
        )
        val profileFavoriteTrips = savedTripsState.trips.filter { it.isFavorite }
        val profileUserName = authUiState.authState.user?.name ?: "—"
        val profileUserEmail = authUiState.authState.user?.email ?: "—"

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

        LaunchedEffect(authUiState.authState.status) {
            if (authUiState.authState.canUseApp) {
                appViewModel.resetToRootRoute()
            }
        }

        LaunchedEffect(canUseApp, appState.currentRoute) {
            if (!canUseApp) return@LaunchedEffect
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
                        BottomTab.PROFILE -> appViewModel.openProfile()
                    }
                },
            )
        }

        if (!canUseApp) {
            if (!authUiState.isStartupResolved ||
                (authUiState.authState.isLoading &&
                    !authUiState.isSubmitting &&
                    !authUiState.isLogoutInProgress)
            ) {
                TravelAppScaffold {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        LoadingStateView(
                            title = stringResource(Res.string.auth_restore_title),
                            subtitle = stringResource(Res.string.auth_restore_subtitle),
                        )
                    }
                }
            } else {
                AnimatedContent(
                    targetState = authUiState.route,
                    label = "auth_route",
                ) { route ->
                    when (route) {
                        AuthRoute.LOGIN -> LoginScreen(
                            email = authUiState.loginEmail,
                            password = authUiState.loginPassword,
                            selectedLanguage = appState.selectedLanguage,
                            selectedTheme = appState.selectedTheme,
                            emailError = authUiState.loginFieldErrors.email,
                            passwordError = authUiState.loginFieldErrors.password,
                            errorMessage = authUiState.submitError,
                            successMessage = authUiState.registrationSuccess,
                            isLoading = authUiState.isSubmitting || authUiState.isLogoutInProgress,
                            isPasswordVisible = authUiState.loginPasswordVisible,
                            offlineActionLabel = offlineActionLabel,
                            onEmailChange = authViewModel::onLoginEmailChange,
                            onPasswordChange = authViewModel::onLoginPasswordChange,
                            onTogglePasswordVisibility = authViewModel::toggleLoginPasswordVisibility,
                            onSubmit = authViewModel::onLoginSubmit,
                            onOpenRegister = authViewModel::openRegister,
                            onContinueOffline = authViewModel::continueOffline,
                            onLanguageSelected = appViewModel::setLanguage,
                            onThemeSelected = appViewModel::setTheme,
                        )

                        AuthRoute.REGISTER -> RegisterScreen(
                            name = authUiState.registerName,
                            email = authUiState.registerEmail,
                            password = authUiState.registerPassword,
                            confirmPassword = authUiState.registerConfirmPassword,
                            selectedLanguage = appState.selectedLanguage,
                            selectedTheme = appState.selectedTheme,
                            nameError = authUiState.registerFieldErrors.name,
                            emailError = authUiState.registerFieldErrors.email,
                            passwordError = authUiState.registerFieldErrors.password,
                            confirmPasswordError = authUiState.registerFieldErrors.confirmPassword,
                            errorMessage = authUiState.submitError,
                            isLoading = authUiState.isSubmitting,
                            isPasswordVisible = authUiState.registerPasswordVisible,
                            isConfirmPasswordVisible = authUiState.registerConfirmPasswordVisible,
                            isSubmitEnabled = authUiState.isRegisterFormClearlyValid && !authUiState.isSubmitting,
                            offlineActionLabel = offlineActionLabel,
                            onNameChange = authViewModel::onRegisterNameChange,
                            onEmailChange = authViewModel::onRegisterEmailChange,
                            onPasswordChange = authViewModel::onRegisterPasswordChange,
                            onConfirmPasswordChange = authViewModel::onRegisterConfirmPasswordChange,
                            onTogglePasswordVisibility = authViewModel::toggleRegisterPasswordVisibility,
                            onToggleConfirmPasswordVisibility = authViewModel::toggleRegisterConfirmPasswordVisibility,
                            onSubmit = authViewModel::onRegisterSubmit,
                            onOpenLogin = authViewModel::openLogin,
                            onContinueOffline = authViewModel::continueOffline,
                            onLanguageSelected = appViewModel::setLanguage,
                            onThemeSelected = appViewModel::setTheme,
                        )
                    }
                }
            }
        } else {
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

                        AppRoute.Profile -> ProfileScreen(
                            userName = profileUserName,
                            userEmail = profileUserEmail,
                            selectedLanguage = appState.selectedLanguage,
                            selectedTheme = appState.selectedTheme,
                            preferenceLabels = profilePreferenceLabels,
                            favoriteTrips = profileFavoriteTrips,
                            strings = strings,
                            onLanguageSelected = appViewModel::setLanguage,
                            onThemeSelected = appViewModel::setTheme,
                            onTripClick = { appViewModel.openTripDetails(it, BottomTab.PROFILE) },
                            onLogoutClick = authViewModel::logout,
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
}

private fun AppRoute.isTopLevelRoute(): Boolean = when (this) {
    is AppRoute.Planner -> true
    AppRoute.MyTrips -> true
    AppRoute.Profile -> true
    is AppRoute.TripDetails -> false
    is AppRoute.DayRouteMap -> false
    is AppRoute.PlaceDetails -> false
}

private fun AppRoute.navigationDepth(): Int = when (this) {
    is AppRoute.Planner -> 0
    AppRoute.MyTrips -> 0
    AppRoute.Profile -> 0
    is AppRoute.TripDetails -> 1
    is AppRoute.DayRouteMap -> 2
    is AppRoute.PlaceDetails -> 2
}

private fun buildProfilePreferenceLabels(
    state: khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreenState,
    strings: khom.pavlo.aitripplanner.ui.strings.AppStrings,
    language: khom.pavlo.aitripplanner.domain.model.AppLanguage,
): List<String> = buildList {
    addAll(state.selectedInterests.map { it.label(language) })
    state.selectedPace?.let { add(it.label(language)) }
    state.selectedBudget?.let { add(it.label(language)) }
    state.selectedCompanionType?.let { add(it.label(language)) }
    addAll(state.selectedPreferences.map { it.label(language) })
    if (state.withChildren) add(strings.withChildrenLabel)
}.distinct()
