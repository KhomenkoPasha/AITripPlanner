package khom.pavlo.aitripplanner.ui.screens.planner

import androidx.compose.runtime.Immutable
import khom.pavlo.aitripplanner.domain.model.CitySuggestion
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.TripPreference
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode

@Immutable
data class PlannerScreenState(
    val mode: PlannerMode = PlannerMode.Create,
    val city: String = "",
    val title: String = "",
    val prompt: String = "",
    val days: String = "1",
    val placeCount: String = "4",
    val walkingMinutesPerDay: String = "180",
    val travelMode: TravelMode = TravelMode.WALKING,
    val heroNote: String = "",
    val selectedInterests: List<Interest> = emptyList(),
    val selectedPace: Pace? = null,
    val selectedBudget: Budget? = null,
    val selectedCompanionType: CompanionType? = null,
    val selectedPreferences: List<TripPreference> = emptyList(),
    val withChildren: Boolean = false,
    val citySuggestions: List<CitySuggestion> = emptyList(),
    val isCitySuggestionsLoading: Boolean = false,
    val shouldShowCitySuggestions: Boolean = false,
    val isSaving: Boolean = false,
    val isLoadingEditorData: Boolean = false,
    val saveError: String? = null,
    val syncStatusLabel: String? = null,
    val currentTrip: TripOverviewUiModel? = null,
    val savedTrips: List<TripOverviewUiModel> = emptyList(),
)
