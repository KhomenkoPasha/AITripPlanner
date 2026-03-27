package khom.pavlo.aitripplanner.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class TripOverviewUiModel(
    val id: String,
    val city: String,
    val title: String,
    val subtitle: String,
    val daysLabel: String,
    val durationLabel: String,
    val distanceLabel: String,
    val syncLabel: String,
    val isDeleting: Boolean = false,
)

@Immutable
data class RouteSummaryUiModel(
    val durationLabel: String,
    val distanceLabel: String,
    val paceLabel: String,
)

@Immutable
data class PlaceUiModel(
    val name: String,
    val address: String,
    val visitTimeLabel: String,
)

@Immutable
data class DayItineraryUiModel(
    val id: String,
    val dayLabel: String,
    val title: String,
    val subtitle: String,
    val durationLabel: String,
    val distanceLabel: String,
    val isExpanded: Boolean,
    val places: List<PlaceUiModel>,
)

@Immutable
data class TripDetailsUiModel(
    val id: String,
    val city: String,
    val subtitle: String,
    val heroNote: String,
    val syncLabel: String,
    val summary: RouteSummaryUiModel,
    val days: List<DayItineraryUiModel>,
)
