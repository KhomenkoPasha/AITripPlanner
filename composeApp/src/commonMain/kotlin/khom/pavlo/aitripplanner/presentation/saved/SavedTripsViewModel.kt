package khom.pavlo.aitripplanner.presentation.saved

import khom.pavlo.aitripplanner.domain.usecase.DeleteTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.EnsureSeedDataUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripsUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.presentation.toOverviewUi
import khom.pavlo.aitripplanner.presentation.toStatusLabel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SavedTripsScreenState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val syncStatusLabel: String? = null,
    val trips: List<TripOverviewUiModel> = emptyList(),
    val pendingDeleteTripId: String? = null,
    val deletingTripIds: Set<String> = emptySet(),
    val deleteErrorMessage: String? = null,
)

class SavedTripsViewModel(
    private val observeTrips: ObserveTripsUseCase,
    private val observeSyncState: ObserveSyncStateUseCase,
    private val ensureSeedData: EnsureSeedDataUseCase,
    private val deleteTrip: DeleteTripUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(SavedTripsScreenState())
    val state: StateFlow<SavedTripsScreenState> = mutableState.asStateFlow()

    init {
        scope.launch { ensureSeedData() }
        scope.launch {
            observeTrips().collect { trips ->
                val deletingIds = state.value.deletingTripIds
                mutableState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        trips = trips.map { trip -> trip.toOverviewUi(isDeleting = trip.id in deletingIds) },
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

    fun requestDelete(tripId: String) {
        mutableState.update {
            it.copy(
                pendingDeleteTripId = tripId,
                deleteErrorMessage = null,
            )
        }
    }

    fun dismissDelete() {
        mutableState.update { it.copy(pendingDeleteTripId = null) }
    }

    fun confirmDelete() {
        val tripId = state.value.pendingDeleteTripId ?: return
        scope.launch {
            mutableState.update {
                it.copy(
                    pendingDeleteTripId = null,
                    deletingTripIds = it.deletingTripIds + tripId,
                    deleteErrorMessage = null,
                )
            }
            delay(220)
            runCatching {
                deleteTrip(tripId)
            }.onFailure { error ->
                mutableState.update {
                    it.copy(
                        deletingTripIds = it.deletingTripIds - tripId,
                        deleteErrorMessage = error.message ?: "Unable to delete trip",
                    )
                }
            }
        }
    }
}
