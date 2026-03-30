package khom.pavlo.aitripplanner.presentation.details

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.usecase.DeleteTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.RemovePlaceUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetDayExpandedUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetPlaceCompletedUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.presentation.deletePlaceError
import khom.pavlo.aitripplanner.presentation.deleteTripError
import khom.pavlo.aitripplanner.presentation.toDetailsUi
import khom.pavlo.aitripplanner.presentation.toStatusLabel
import khom.pavlo.aitripplanner.presentation.tripNotFoundError
import khom.pavlo.aitripplanner.presentation.updatePlaceStatusError
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreenState
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface TripDetailsNavigationEvent {
    data class TripDeleted(val tripId: String) : TripDetailsNavigationEvent
}

class TripDetailsViewModel(
    private val tripId: String,
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val observeTripDetails: ObserveTripDetailsUseCase,
    private val observeSyncState: ObserveSyncStateUseCase,
    private val setDayExpanded: SetDayExpandedUseCase,
    private val setPlaceCompleted: SetPlaceCompletedUseCase,
    private val removePlace: RemovePlaceUseCase,
    private val deleteTrip: DeleteTripUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(TripDetailsScreenState(isLoading = true))
    private val mutableEvents = MutableSharedFlow<TripDetailsNavigationEvent>()
    private var currentLanguage: AppLanguage = AppLanguage.EN

    val state: StateFlow<TripDetailsScreenState> = mutableState.asStateFlow()
    val events: SharedFlow<TripDetailsNavigationEvent> = mutableEvents.asSharedFlow()

    init {
        scope.launch {
            combine(observeTripDetails(tripId), observeAppLanguage()) { trip, language ->
                language to trip
            }.collect { (language, trip) ->
                currentLanguage = language
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (trip == null) language.tripNotFoundError() else null,
                        trip = trip?.toDetailsUi(language),
                    )
                }
            }
        }
        scope.launch {
            combine(observeSyncState(), observeAppLanguage()) { sync, language ->
                currentLanguage = language
                sync.toStatusLabel(language)
            }.collect { syncLabel ->
                mutableState.update { it.copy(syncStatusLabel = syncLabel) }
            }
        }
    }

    fun onToggleDay(dayId: String, expanded: Boolean) {
        scope.launch { setDayExpanded(dayId, expanded) }
    }

    fun onDeletePlace(placeId: String) {
        scope.launch {
            runCatching {
                removePlace(placeId)
            }.onFailure { error ->
                mutableState.update {
                    it.copy(deleteErrorMessage = error.message ?: currentLanguage.deletePlaceError())
                }
            }
        }
    }

    fun onPlaceCompletionChange(placeId: String, completed: Boolean) {
        scope.launch {
            runCatching {
                setPlaceCompleted(placeId, completed)
            }.onFailure { error ->
                mutableState.update {
                    it.copy(deleteErrorMessage = error.message ?: currentLanguage.updatePlaceStatusError())
                }
            }
        }
    }

    fun showDeleteDialog() {
        mutableState.update { it.copy(isDeleteDialogVisible = true, deleteErrorMessage = null) }
    }

    fun hideDeleteDialog() {
        mutableState.update { it.copy(isDeleteDialogVisible = false) }
    }

    fun confirmDelete() {
        scope.launch {
            mutableState.update {
                it.copy(
                    isDeleteDialogVisible = false,
                    isDeleting = true,
                    deleteErrorMessage = null,
                )
            }
            runCatching {
                deleteTrip(tripId)
            }.onSuccess {
                mutableState.update { it.copy(isDeleting = false) }
                mutableEvents.emit(TripDetailsNavigationEvent.TripDeleted(tripId))
            }.onFailure { error ->
                mutableState.update {
                    it.copy(
                        isDeleting = false,
                        deleteErrorMessage = error.message ?: currentLanguage.deleteTripError(),
                    )
                }
            }
        }
    }
}
