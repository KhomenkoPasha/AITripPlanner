package khom.pavlo.aitripplanner.presentation.place

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetPlaceCompletedUseCase
import khom.pavlo.aitripplanner.presentation.placeNotFoundError
import khom.pavlo.aitripplanner.presentation.toPlaceDetailsUi
import khom.pavlo.aitripplanner.presentation.updatePlaceStatusError
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.ui.screens.place.PlaceDetailsScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlaceDetailsViewModel(
    private val tripId: String,
    private val dayId: String,
    private val placeId: String,
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val observeTripDetails: ObserveTripDetailsUseCase,
    private val setPlaceCompleted: SetPlaceCompletedUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(PlaceDetailsScreenState(isLoading = true))
    private var currentLanguage: AppLanguage = AppLanguage.EN

    val state: StateFlow<PlaceDetailsScreenState> = mutableState.asStateFlow()

    init {
        scope.launch {
            combine(observeTripDetails(tripId), observeAppLanguage()) { trip, language ->
                language to trip
            }.collect { (language, trip) ->
                currentLanguage = language
                val place = trip?.toPlaceDetailsUi(language, dayId, placeId)
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (place == null) language.placeNotFoundError() else null,
                        place = place,
                    )
                }
            }
        }
    }

    fun onVisitedChange(completed: Boolean) {
        scope.launch {
            runCatching {
                setPlaceCompleted(placeId, completed)
            }.onFailure { error ->
                mutableState.update {
                    it.copy(actionErrorMessage = error.message ?: currentLanguage.updatePlaceStatusError())
                }
            }.onSuccess {
                mutableState.update { it.copy(actionErrorMessage = null) }
            }
        }
    }
}
