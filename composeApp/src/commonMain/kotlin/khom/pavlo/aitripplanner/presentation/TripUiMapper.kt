package khom.pavlo.aitripplanner.presentation

import khom.pavlo.aitripplanner.domain.model.AppSyncState
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceUiModel
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.model.TripDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel

internal fun Trip.toOverviewUi(
    isDeleting: Boolean = false,
) = TripOverviewUiModel(
    id = id,
    city = city,
    title = title,
    subtitle = summary,
    daysLabel = "${days.size} days",
    durationLabel = formatDuration(durationMinutes),
    distanceLabel = formatDistance(distanceKm),
    syncLabel = if (isPendingSync) "Pending sync" else "Synced",
    isDeleting = isDeleting,
)

internal fun Trip.toDetailsUi() = TripDetailsUiModel(
    id = id,
    city = city,
    subtitle = summary,
    heroNote = heroNote,
    syncLabel = if (isPendingSync) "Offline draft" else "Cloud synced",
    summary = RouteSummaryUiModel(
        durationLabel = formatDuration(durationMinutes),
        distanceLabel = formatDistance(distanceKm),
        paceLabel = if (distanceKm < 10) "Easy pace" else "Balanced pace",
    ),
    days = days.map { day ->
        DayItineraryUiModel(
            id = day.id,
            dayLabel = "Day ${day.dayIndex}",
            title = day.title,
            subtitle = day.summary,
            durationLabel = formatDuration(day.durationMinutes),
            distanceLabel = formatDistance(day.distanceKm),
            isExpanded = day.isExpanded,
            places = day.places.map { place ->
                PlaceUiModel(
                    name = place.name,
                    address = place.address,
                    visitTimeLabel = formatDuration(place.visitMinutes),
                )
            },
        )
    },
)

internal fun AppSyncState.toStatusLabel(): String? = when {
    isRunning -> "Syncing $queuedItems changes"
    lastError != null -> "Sync paused"
    queuedItems > 0 -> "$queuedItems changes queued"
    lastCompletedAtEpochMillis != null -> "Everything synced"
    else -> null
}

private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatDistance(distanceKm: Double): String = "${"%.1f".format(distanceKm)} km"
