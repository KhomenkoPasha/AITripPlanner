package khom.pavlo.aitripplanner.ui.model

import androidx.compose.runtime.Immutable

@Immutable
data class DayRouteStopUiModel(
    val id: String,
    val numberLabel: String,
    val title: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
)

@Immutable
data class DayRouteMapUiModel(
    val tripId: String,
    val dayId: String,
    val city: String,
    val dayLabel: String,
    val title: String,
    val durationLabel: String,
    val distanceLabel: String,
    val stops: List<DayRouteStopUiModel>,
)
