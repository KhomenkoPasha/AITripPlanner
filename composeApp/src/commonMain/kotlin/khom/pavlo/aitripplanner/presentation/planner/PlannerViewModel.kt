package khom.pavlo.aitripplanner.presentation.planner

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CitySuggestion
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.TripPreference
import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.usecase.CreateTripUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSyncStateUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripDetailsUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveTripsUseCase
import khom.pavlo.aitripplanner.domain.usecase.SearchCitiesUseCase
import khom.pavlo.aitripplanner.domain.usecase.UpdateTripUseCase
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.presentation.invalidDaysError
import khom.pavlo.aitripplanner.presentation.invalidPlaceCountError
import khom.pavlo.aitripplanner.presentation.invalidWalkingMinutesError
import khom.pavlo.aitripplanner.presentation.requiredPlannerFieldsError
import khom.pavlo.aitripplanner.presentation.saveTripError
import khom.pavlo.aitripplanner.presentation.toOverviewUi
import khom.pavlo.aitripplanner.presentation.toStatusLabel
import khom.pavlo.aitripplanner.presentation.tripNotFoundError
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode
import khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreenState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
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
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val observeTrips: ObserveTripsUseCase,
    private val observeSyncState: ObserveSyncStateUseCase,
    private val observeTripDetails: ObserveTripDetailsUseCase,
    private val createTrip: CreateTripUseCase,
    private val updateTrip: UpdateTripUseCase,
    private val searchCities: SearchCitiesUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(PlannerScreenState())
    private val mutableEvents = MutableSharedFlow<PlannerNavigationEvent>()
    private var editorLoadJob: Job? = null
    private var cityAutocompleteJob: Job? = null
    private var currentLanguage: AppLanguage = AppLanguage.EN

    val state: StateFlow<PlannerScreenState> = mutableState.asStateFlow()
    val events: SharedFlow<PlannerNavigationEvent> = mutableEvents.asSharedFlow()

    init {
        scope.launch {
            combine(observeTrips(), observeAppLanguage()) { trips, language ->
                language to trips
            }.collect { (language, trips) ->
                currentLanguage = language
                val overviewTrips = trips.map { trip -> trip.toOverviewUi(language) }
                mutableState.update {
                    it.copy(
                        currentTrip = overviewTrips.firstOrNull(),
                        savedTrips = overviewTrips,
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
                        prompt = "",
                        days = "1",
                        placeCount = "4",
                        walkingMinutesPerDay = "180",
                        travelMode = TravelMode.WALKING,
                        heroNote = "",
                        selectedInterests = emptyList(),
                        selectedPace = null,
                        selectedBudget = null,
                        selectedCompanionType = null,
                        selectedPreferences = emptyList(),
                        withChildren = false,
                        citySuggestions = emptyList(),
                        isCitySuggestionsLoading = false,
                        shouldShowCitySuggestions = false,
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
                                saveError = currentLanguage.tripNotFoundError(),
                            )
                        }
                    } else {
                        mutableState.update {
                            it.copy(
                                mode = mode,
                                city = trip.city,
                                title = trip.title,
                                prompt = trip.summary,
                                days = trip.days.size.coerceAtLeast(1).toString(),
                                placeCount = "4",
                                walkingMinutesPerDay = "180",
                                travelMode = TravelMode.WALKING,
                                heroNote = trip.heroNote,
                                selectedInterests = emptyList(),
                                selectedPace = null,
                                selectedBudget = null,
                                selectedCompanionType = null,
                                selectedPreferences = emptyList(),
                                withChildren = false,
                                citySuggestions = emptyList(),
                                isCitySuggestionsLoading = false,
                                shouldShowCitySuggestions = false,
                                isLoadingEditorData = false,
                                saveError = null,
                            )
                        }
                    }
                }
            }
        }
    }

    fun onCityChange(value: String) {
        mutableState.update { state ->
            state.copy(
                city = value,
                saveError = null,
                citySuggestions = if (value.trim().length < 2) emptyList() else state.citySuggestions,
                isCitySuggestionsLoading = value.trim().length >= 2,
                shouldShowCitySuggestions = value.trim().length >= 2,
            )
        }
        requestCitySuggestions(value)
    }

    fun onCitySuggestionSelected(suggestion: CitySuggestion) {
        cityAutocompleteJob?.cancel()
        mutableState.update {
            it.copy(
                city = suggestion.primaryText,
                citySuggestions = emptyList(),
                isCitySuggestionsLoading = false,
                shouldShowCitySuggestions = false,
                saveError = null,
            )
        }
    }
    fun onTitleChange(value: String) = mutableState.update { it.copy(title = value, saveError = null) }
    fun onPromptChange(value: String) = mutableState.update { it.copy(prompt = value, saveError = null) }
    fun onDaysChange(value: String) = mutableState.update { it.copy(days = value, saveError = null) }
    fun onPlaceCountChange(value: String) = mutableState.update { it.copy(placeCount = value, saveError = null) }
    fun onWalkingMinutesPerDayChange(value: String) = mutableState.update { it.copy(walkingMinutesPerDay = value, saveError = null) }
    fun onTravelModeChange(value: TravelMode) = mutableState.update { it.copy(travelMode = value, saveError = null) }
    fun onInterestToggle(value: Interest) = mutableState.update { state ->
        val updated = if (value in state.selectedInterests) {
            state.selectedInterests - value
        } else {
            state.selectedInterests + value
        }
        state.copy(selectedInterests = updated, saveError = null)
    }
    fun onPaceSelected(value: Pace?) = mutableState.update { state ->
        state.copy(
            selectedPace = if (state.selectedPace == value) null else value,
            saveError = null,
        )
    }
    fun onBudgetSelected(value: Budget?) = mutableState.update { state ->
        state.copy(
            selectedBudget = if (state.selectedBudget == value) null else value,
            saveError = null,
        )
    }
    fun onCompanionTypeSelected(value: CompanionType) = mutableState.update { state ->
        state.copy(
            selectedCompanionType = if (state.selectedCompanionType == value) null else value,
            saveError = null,
        )
    }
    fun onPreferenceToggle(value: TripPreference) = mutableState.update { state ->
        val updated = if (value in state.selectedPreferences) {
            state.selectedPreferences - value
        } else {
            state.selectedPreferences + value
        }
        state.copy(selectedPreferences = updated, saveError = null)
    }
    fun onWithChildrenToggle() = mutableState.update { state ->
        state.copy(withChildren = !state.withChildren, saveError = null)
    }
    fun onHeroNoteChange(value: String) = mutableState.update { it.copy(heroNote = value, saveError = null) }

    fun onSaveTrip(language: AppLanguage) {
        val snapshot = state.value
        val parsedDays = snapshot.days.trim().toIntOrNull()
        val parsedPlaceCount = snapshot.placeCount.trim().toIntOrNull()
        val parsedWalkingMinutes = snapshot.walkingMinutesPerDay.trim().toIntOrNull()
        if (snapshot.city.isBlank() || snapshot.title.isBlank() || snapshot.prompt.isBlank()) {
            mutableState.update { it.copy(saveError = currentLanguage.requiredPlannerFieldsError()) }
            return
        }
        if (parsedDays == null || parsedDays <= 0) {
            mutableState.update { it.copy(saveError = currentLanguage.invalidDaysError()) }
            return
        }
        if (parsedPlaceCount == null || parsedPlaceCount <= 0) {
            mutableState.update { it.copy(saveError = currentLanguage.invalidPlaceCountError()) }
            return
        }
        if (parsedWalkingMinutes == null || parsedWalkingMinutes <= 0) {
            mutableState.update { it.copy(saveError = currentLanguage.invalidWalkingMinutesError()) }
            return
        }

        val input = TripEditorInput(
            city = snapshot.city.trim(),
            title = snapshot.title.trim(),
            prompt = snapshot.prompt.trim(),
            days = parsedDays,
            placeCount = parsedPlaceCount,
            walkingMinutesPerDay = parsedWalkingMinutes,
            selectedInterests = snapshot.selectedInterests,
            selectedPace = snapshot.selectedPace,
            selectedBudget = snapshot.selectedBudget,
            selectedCompanionType = snapshot.selectedCompanionType,
            selectedPreferences = snapshot.selectedPreferences,
            withChildren = snapshot.withChildren,
            travelMode = snapshot.travelMode,
            heroNote = snapshot.heroNote.trim(),
            language = language,
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
                        saveError = error.message ?: currentLanguage.saveTripError(),
                    )
                }
            }
        }
    }

    private fun requestCitySuggestions(query: String) {
        cityAutocompleteJob?.cancel()
        val normalized = query.trim()
        if (normalized.length < 2) {
            mutableState.update {
                it.copy(
                    citySuggestions = emptyList(),
                    isCitySuggestionsLoading = false,
                    shouldShowCitySuggestions = false,
                )
            }
            return
        }

        cityAutocompleteJob = scope.launch {
            delay(300)
            runCatching {
                searchCities(normalized, currentLanguage)
            }.onSuccess { suggestions ->
                if (state.value.city.trim() == normalized) {
                    mutableState.update {
                        it.copy(
                            citySuggestions = suggestions,
                            isCitySuggestionsLoading = false,
                            shouldShowCitySuggestions = true,
                        )
                    }
                }
            }.onFailure {
                if (state.value.city.trim() == normalized) {
                    mutableState.update {
                        it.copy(
                            citySuggestions = emptyList(),
                            isCitySuggestionsLoading = false,
                            shouldShowCitySuggestions = true,
                        )
                    }
                }
            }
        }
    }
}
