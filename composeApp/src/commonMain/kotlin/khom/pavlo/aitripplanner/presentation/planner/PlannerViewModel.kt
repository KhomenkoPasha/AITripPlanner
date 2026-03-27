package khom.pavlo.aitripplanner.presentation.planner

import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.usecase.CreateTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.EnsureSeedDataUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripsUseCase
import khom.pavlo.aitripplanner.domain.usecase.UpdateTripUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.presentation.toOverviewUi
import khom.pavlo.aitripplanner.presentation.toStatusLabel
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode
import khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PlannerNavigationEvent {
    data class TripSaved(
        val tripId: String,
        val mode: PlannerMode,
    ) : PlannerNavigationEvent
}

class PlannerViewModel(
    private val observeTrips: ObserveTripsUseCase,
    private val observeSyncState: ObserveSyncStateUseCase,
    private val observeTripDetails: ObserveTripDetailsUseCase,
    private val createTrip: CreateTripUseCase,
    private val updateTrip: UpdateTripUseCase,
    private val ensureSeedData: EnsureSeedDataUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(
        PlannerScreenState(
            helperText = "Fill in the trip details. Changes save locally first and sync later.",
        ),
    )
    private val mutableEvents = MutableSharedFlow<PlannerNavigationEvent>()
    private var editorLoadJob: Job? = null

    val state: StateFlow<PlannerScreenState> = mutableState.asStateFlow()
    val events: SharedFlow<PlannerNavigationEvent> = mutableEvents.asSharedFlow()

    init {
        scope.launch { ensureSeedData() }
        scope.launch {
            observeTrips().collect { trips ->
                val overviewTrips = trips.map { it.toOverviewUi() }
                mutableState.update {
                    it.copy(
                        currentTrip = overviewTrips.firstOrNull(),
                        savedTrips = overviewTrips,
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

    fun setMode(mode: PlannerMode) {
        if (mode == state.value.mode && mode is PlannerMode.Edit) return
        editorLoadJob?.cancel()
        when (mode) {
            PlannerMode.Create -> {
                mutableState.update {
                    it.copy(
                        mode = PlannerMode.Create,
                        city = "",
                        title = "",
                        summary = "",
                        heroNote = "",
                        saveError = null,
                        isLoadingEditorData = false,
                    )
                }
            }

            is PlannerMode.Edit -> {
                mutableState.update {
                    it.copy(
                        mode = mode,
                        isLoadingEditorData = true,
                        saveError = null,
                    )
                }
                editorLoadJob = scope.launch {
                    val trip = observeTripDetails(mode.tripId).first()
                    if (trip == null) {
                        mutableState.update {
                            it.copy(
                                isLoadingEditorData = false,
                                saveError = "Trip not found",
                            )
                        }
                    } else {
                        mutableState.update {
                            it.copy(
                                mode = mode,
                                city = trip.city,
                                title = trip.title,
                                summary = trip.summary,
                                heroNote = trip.heroNote,
                                isLoadingEditorData = false,
                                saveError = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onCityChange(value: String) = mutableState.update { it.copy(city = value, saveError = null) }
    fun onTitleChange(value: String) = mutableState.update { it.copy(title = value, saveError = null) }
    fun onSummaryChange(value: String) = mutableState.update { it.copy(summary = value, saveError = null) }
    fun onHeroNoteChange(value: String) = mutableState.update { it.copy(heroNote = value, saveError = null) }

    fun onSaveTrip() {
        val snapshot = state.value
        if (snapshot.city.isBlank() || snapshot.title.isBlank() || snapshot.summary.isBlank()) {
            mutableState.update { it.copy(saveError = "City, title, and summary are required") }
            return
        }

        val input = TripEditorInput(
            city = snapshot.city.trim(),
            title = snapshot.title.trim(),
            summary = snapshot.summary.trim(),
            heroNote = snapshot.heroNote.trim().ifBlank { "A calm premium route with room for future maps, notes, and favorites." },
        )

        scope.launch {
            mutableState.update { it.copy(isSaving = true, saveError = null) }
            runCatching {
                when (val mode = snapshot.mode) {
                    PlannerMode.Create -> createTrip(input)
                    is PlannerMode.Edit -> updateTrip(mode.tripId, input)
                }
            }.onSuccess { trip ->
                mutableState.update { current ->
                    current.copy(
                        isSaving = false,
                        saveError = null,
                    )
                }
                mutableEvents.emit(PlannerNavigationEvent.TripSaved(trip.id, snapshot.mode))
                if (snapshot.mode == PlannerMode.Create) {
                    setMode(PlannerMode.Create)
                }
            }.onFailure { error ->
                mutableState.update {
                    it.copy(
                        isSaving = false,
                        saveError = error.message ?: "Unable to save trip",
                    )
                }
            }
        }
    }
}
