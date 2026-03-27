package khom.pavlo.aitripplanner.presentation.app

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetAppLanguageUseCase
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
    val currentRoute: AppRoute = AppRoute.Planner(PlannerMode.Create),
    val selectedLanguage: AppLanguage = AppLanguage.EN,
    val editOriginRoute: AppRoute? = null,
) {
    val selectedTab: BottomTab
        get() = currentRoute.bottomTab()
}

class AppViewModel(
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val setAppLanguage: SetAppLanguageUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(AppShellState())
    val state: StateFlow<AppShellState> = mutableState.asStateFlow()

    init {
        scope.launch {
            observeAppLanguage().collect { language ->
                mutableState.update { it.copy(selectedLanguage = language) }
            }
        }
    }

    fun openCreateTrip() {
        mutableState.update {
            it.copy(
                currentRoute = AppRoute.Planner(PlannerMode.Create),
                editOriginRoute = null,
            )
        }
    }

    fun openEditTrip(tripId: String) {
        mutableState.update { state ->
            val originRoute = state.currentRoute.takeIf { it !is AppRoute.Planner } ?: AppRoute.MyTrips
            state.copy(
                currentRoute = AppRoute.Planner(PlannerMode.Edit(tripId)),
                editOriginRoute = originRoute,
            )
        }
    }

    fun openMyTrips() {
        mutableState.update {
            it.copy(
                currentRoute = AppRoute.MyTrips,
                editOriginRoute = null,
            )
        }
    }

    fun openTripDetails(tripId: String, originTab: BottomTab) {
        mutableState.update {
            it.copy(
                currentRoute = AppRoute.TripDetails(tripId, originTab),
                editOriginRoute = null,
            )
        }
    }

    fun onDeleteCompleted() {
        openMyTrips()
    }

    fun onTripSaved(mode: PlannerMode, tripId: String) {
        if (mode is PlannerMode.Edit) {
            mutableState.update { state ->
                val originTab = when (val originRoute = state.editOriginRoute) {
                    is AppRoute.TripDetails -> originRoute.originTab
                    AppRoute.MyTrips -> BottomTab.MY_TRIPS
                    else -> BottomTab.MY_TRIPS
                }
                state.copy(
                    currentRoute = AppRoute.TripDetails(tripId, originTab),
                    editOriginRoute = null,
                )
            }
        }
    }

    fun closeEditTrip() {
        mutableState.update { state ->
            state.copy(
                currentRoute = state.editOriginRoute ?: AppRoute.MyTrips,
                editOriginRoute = null,
            )
        }
    }

    fun setLanguage(language: AppLanguage) {
        scope.launch { setAppLanguage(language) }
    }
}
