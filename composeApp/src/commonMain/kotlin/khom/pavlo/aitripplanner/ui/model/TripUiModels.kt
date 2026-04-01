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
    val isFavorite: Boolean,
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
    val id: String,
    val name: String,
    val address: String,
    val visitTimeLabel: String,
    val note: String,
    val photoUrl: String?,
    val photoAttribution: String?,
    val isCompleted: Boolean,
)

@Immutable
data class PlaceGalleryImageUiModel(
    val id: String,
    val imageUrl: String?,
    val attribution: String?,
)

@Immutable
data class RouteContextUiModel(
    val dayLabel: String,
    val dayTitle: String,
    val stopLabel: String,
    val previousPlaceName: String?,
    val nextPlaceName: String?,
)

@Immutable
data class PlaceDetailsUiModel(
    val tripId: String,
    val dayId: String,
    val placeId: String,
    val title: String,
    val address: String,
    val city: String,
    val dayLabel: String,
    val dayTitle: String,
    val visitTimeLabel: String,
    val bestTimeLabel: String,
    val statusLabel: String,
    val categoryLabel: String?,
    val openingStatusLabel: String?,
    val neighborhoodLabel: String?,
    val priceLabel: String?,
    val isCompleted: Boolean,
    val latitude: Double?,
    val longitude: Double?,
    val heroImageUrl: String?,
    val heroImageAttribution: String?,
    val gallery: List<PlaceGalleryImageUiModel>,
    val aboutText: String,
    val whyInRouteText: String,
    val tipsText: String,
    val visitDetailsText: String,
    val websiteUrl: String?,
    val routeContext: RouteContextUiModel,
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
    val hasRouteMap: Boolean,
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
