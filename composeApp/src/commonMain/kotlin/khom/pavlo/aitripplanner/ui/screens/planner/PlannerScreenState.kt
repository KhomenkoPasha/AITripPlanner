package khom.pavlo.aitripplanner.ui.screens.planner

import androidx.compose.runtime.Immutable
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode

@Immutable
data class PlannerScreenState(
    val mode: PlannerMode = PlannerMode.Create,
    val city: String = "",
    val title: String = "",
    val summary: String = "",
    val heroNote: String = "",
    val helperText: String,
    val isSaving: Boolean = false,
    val isLoadingEditorData: Boolean = false,
    val saveError: String? = null,
    val syncStatusLabel: String? = null,
    val currentTrip: TripOverviewUiModel? = null,
    val savedTrips: List<TripOverviewUiModel> = emptyList(),
)
