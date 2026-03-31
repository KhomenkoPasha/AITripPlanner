package khom.pavlo.aitripplanner.presentation.app

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.domain.usecase.GetCurrentAppThemeUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppThemeUseCase
import khom.pavlo.aitripplanner.domain.usecase.RemoveMockDataUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetAppThemeUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.ui.navigation.AppRoute
import khom.pavlo.aitripplanner.ui.navigation.BottomTab
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode
import khom.pavlo.aitripplanner.ui.navigation.bottomTab
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppShellState(
    val backStack: List<AppRoute> = listOf(AppRoute.Planner(PlannerMode.Create)),
    val selectedLanguage: AppLanguage = AppLanguage.EN,
    val selectedTheme: AppThemeMode = AppThemeMode.SYSTEM,
    val isExitDialogVisible: Boolean = false,
    val closeAppRequestId: Long = 0L,
) {
    val currentRoute: AppRoute
        get() = backStack.last()

    val selectedTab: BottomTab
        get() = currentRoute.bottomTab()

    val canNavigateBack: Boolean
        get() = backStack.size > 1
}

class AppViewModel(
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase,
    private val observeAppTheme: ObserveAppThemeUseCase,
    private val getCurrentAppTheme: GetCurrentAppThemeUseCase,
    private val setAppTheme: SetAppThemeUseCase,
    private val removeMockData: RemoveMockDataUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(
        AppShellState(selectedTheme = getCurrentAppTheme()),
    )
    val state: StateFlow<AppShellState> = mutableState.asStateFlow()

    init {
        scope.launch {
            removeMockData()
        }
        scope.launch {
            observeAppLanguage().collect { language ->
                mutableState.update { it.copy(selectedLanguage = language) }
            }
        }
        scope.launch {
            observeAppTheme().collect { theme ->
                mutableState.update { it.copy(selectedTheme = theme) }
            }
        }
    }

    fun openCreateTrip() {
        navigateTo(AppRoute.Planner(PlannerMode.Create))
    }

    fun openEditTrip(tripId: String) {
        navigateTo(AppRoute.Planner(PlannerMode.Edit(tripId)))
    }

    fun openMyTrips() {
        navigateTo(AppRoute.MyTrips)
    }

    fun openTripDetails(tripId: String, originTab: BottomTab) {
        navigateTo(AppRoute.TripDetails(tripId, originTab))
    }

    fun openDayRouteMap(
        tripId: String,
        dayId: String,
        originTab: BottomTab,
    ) {
        navigateTo(
            AppRoute.DayRouteMap(
                tripId = tripId,
                dayId = dayId,
                originTab = originTab,
            ),
        )
    }

    fun openPlaceDetails(
        tripId: String,
        dayId: String,
        placeId: String,
        originTab: BottomTab,
    ) {
        navigateTo(
            AppRoute.PlaceDetails(
                tripId = tripId,
                dayId = dayId,
                placeId = placeId,
                originTab = originTab,
            ),
        )
    }

    fun onDeleteCompleted() {
        navigateBack()
    }

    fun onTripSaved(mode: PlannerMode, tripId: String) {
        if (mode is PlannerMode.Edit) {
            mutableState.update { state ->
                val previousRoute = state.backStack
                    .dropLast(1)
                    .lastOrNull()
                val originTab = when (previousRoute) {
                    is AppRoute.TripDetails -> previousRoute.originTab
                    is AppRoute.DayRouteMap -> previousRoute.originTab
                    is AppRoute.PlaceDetails -> previousRoute.originTab
                    AppRoute.MyTrips -> BottomTab.MY_TRIPS
                    else -> BottomTab.CREATE_NEW_TRIP
                }
                state.copy(
                    backStack = state.backStack.replaceTop(
                        AppRoute.TripDetails(tripId, originTab),
                    ),
                )
            }
        }
    }

    fun closeEditTrip() {
        navigateBack()
    }

    fun handleBackPress() {
        mutableState.update { state ->
            when {
                state.isExitDialogVisible -> state.copy(isExitDialogVisible = false)
                state.canNavigateBack -> state.copy(backStack = state.backStack.dropLast(1))
                else -> state.copy(isExitDialogVisible = true)
            }
        }
    }

    fun navigateBack() {
        mutableState.update { state ->
            if (!state.canNavigateBack) return@update state
            state.copy(backStack = state.backStack.dropLast(1), isExitDialogVisible = false)
        }
    }

    fun dismissExitDialog() {
        mutableState.update { it.copy(isExitDialogVisible = false) }
    }

    fun confirmExitDialog() {
        mutableState.update { state ->
            state.copy(
                isExitDialogVisible = false,
                closeAppRequestId = state.closeAppRequestId + 1,
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        scope.launch { setAppLanguage(language) }
    }

    fun setTheme(theme: AppThemeMode) {
        scope.launch { setAppTheme(theme) }
    }

    private fun navigateTo(route: AppRoute) {
        mutableState.update { state ->
            if (state.currentRoute == route) {
                state
            } else {
                state.copy(
                    backStack = state.backStack + route,
                    isExitDialogVisible = false,
                )
            }
        }
    }
}

private fun List<AppRoute>.replaceTop(route: AppRoute): List<AppRoute> = when {
    isEmpty() -> listOf(route)
    last() == route -> this
    else -> dropLast(1) + route
}
