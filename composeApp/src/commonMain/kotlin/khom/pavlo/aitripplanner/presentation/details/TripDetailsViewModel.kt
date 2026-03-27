package khom.pavlo.aitripplanner.presentation.details

import khom.pavlo.aitripplanner.domain.usecase.DeleteTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.EnsureSeedDataUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.SetDayExpandedUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.presentation.toDetailsUi
import khom.pavlo.aitripplanner.presentation.toStatusLabel
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreenState
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
    private val observeTripDetails: ObserveTripDetailsUseCase,
    private val observeSyncState: ObserveSyncStateUseCase,
    private val setDayExpanded: SetDayExpandedUseCase,
    private val deleteTrip: DeleteTripUseCase,
    private val ensureSeedData: EnsureSeedDataUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(TripDetailsScreenState(isLoading = true))
    private val mutableEvents = MutableSharedFlow<TripDetailsNavigationEvent>()

    val state: StateFlow<TripDetailsScreenState> = mutableState.asStateFlow()
    val events: SharedFlow<TripDetailsNavigationEvent> = mutableEvents.asSharedFlow()

    init {
        scope.launch { ensureSeedData() }
        scope.launch {
            observeTripDetails(tripId).collect { trip ->
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = if (trip == null) "Trip not found" else null,
                        trip = trip?.toDetailsUi(),
                    )
                }
            }
        }
        scope.launch {
            observeSyncState().collect { sync ->
                mutableState.update { it.copy(syncStatusLabel = sync.toStatusLabel()) }
            }
        }
    }

    fun onToggleDay(dayId: String, expanded: Boolean) {
        scope.launch { setDayExpanded(dayId, expanded) }
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
                        deleteErrorMessage = error.message ?: "Unable to delete trip",
                    )
                }
            }
        }
    }
}
