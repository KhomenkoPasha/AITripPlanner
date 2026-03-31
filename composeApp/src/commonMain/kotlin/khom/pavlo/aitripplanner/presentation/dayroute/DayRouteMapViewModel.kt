package khom.pavlo.aitripplanner.presentation.dayroute

import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.presentation.toDayRouteMapUi
import khom.pavlo.aitripplanner.ui.screens.dayroute.DayRouteMapScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DayRouteMapViewModel(
    private val tripId: String,
    private val dayId: String,
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val observeTripDetails: ObserveTripDetailsUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(DayRouteMapScreenState(isLoading = true))
    val state: StateFlow<DayRouteMapScreenState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(observeTripDetails(tripId), observeAppLanguage()) { trip, language ->
                trip?.toDayRouteMapUi(language, dayId)
            }.collect { route ->
                mutableState.update {
                    it.copy(
                        route = route,
                        isLoading = false,
                    )
                }
            }
        }
    }
}
