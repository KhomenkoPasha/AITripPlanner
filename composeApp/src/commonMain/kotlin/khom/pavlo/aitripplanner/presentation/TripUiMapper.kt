package khom.pavlo.aitripplanner.presentation

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.PlacePhoto
import khom.pavlo.aitripplanner.domain.model.PlaceRemotePhoto
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.localBackendBaseUrl
import io.ktor.http.encodeURLParameter
import khom.pavlo.aitripplanner.ui.model.DayRouteMapUiModel
import khom.pavlo.aitripplanner.ui.model.DayRouteStopUiModel
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceGalleryImageUiModel
import khom.pavlo.aitripplanner.ui.model.PlacePhotoUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceUiModel
import khom.pavlo.aitripplanner.ui.model.RouteContextUiModel
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.model.TripDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel

internal fun Trip.toOverviewUi(
    language: AppLanguage,
    isDeleting: Boolean = false,
) = TripOverviewUiModel(
    id = id,
    city = city,
    title = title,
    subtitle = summary,
    daysLabel = language.daysLabel(days.size),
    durationLabel = formatDuration(durationMinutes, language),
    distanceLabel = formatDistance(distanceKm, language),
    syncLabel = if (isPendingSync) language.pendingSyncLabel() else language.syncedLabel(),
    isFavorite = isFavorite,
    isDeleting = isDeleting,
)

internal fun Trip.toDetailsUi(language: AppLanguage) = TripDetailsUiModel(
    id = id,
    city = city,
    subtitle = summary,
    heroNote = heroNote,
    syncLabel = if (isPendingSync) language.offlineDraftLabel() else language.cloudSyncedLabel(),
    summary = RouteSummaryUiModel(
        durationLabel = formatDuration(durationMinutes, language),
        distanceLabel = formatDistance(distanceKm, language),
        paceLabel = if (distanceKm < 10) language.easyPaceLabel() else language.balancedPaceLabel(),
    ),
    days = days.map { day ->
        DayItineraryUiModel(
            id = day.id,
            dayLabel = language.dayLabel(day.dayIndex),
            title = day.title,
            subtitle = if (day.summary.isBlank() && day.places.isEmpty()) {
                language.noStopsPlannedYetLabel()
            } else {
                day.summary
            },
            durationLabel = formatDuration(day.durationMinutes, language),
            distanceLabel = formatDistance(day.distanceKm, language),
            isExpanded = day.isExpanded,
            hasRouteMap = day.places.any { place -> place.latitude != null && place.longitude != null },
            places = day.places.map { place ->
                PlaceUiModel(
                    id = place.id,
                    name = place.name,
                    address = place.address,
                    visitTimeLabel = formatDuration(place.visitMinutes, language),
                    note = place.shortDescription.ifBlank { place.note },
                    photoUrl = place.photos.firstOrNull()?.toBackendImageUrl(),
                    photoAttribution = place.photos.firstOrNull()?.attribution,
                    isCompleted = place.isCompleted,
                )
            },
        )
    },
)

internal fun Trip.toPlaceDetailsUi(
    language: AppLanguage,
    dayId: String,
    placeId: String,
): PlaceDetailsUiModel? {
    val day = days.firstOrNull { it.id == dayId } ?: return null
    val placeIndex = day.places.indexOfFirst { it.id == placeId }
    if (placeIndex < 0) return null

    val place = day.places[placeIndex]
    val stopNumber = (place.stopIndex ?: placeIndex) + 1
    val totalStops = day.places.size
    val backendPhotos = place.photos.distinctBy { it.ref }
    val openingStatusLabel = language.openingStatusLabel(place.isOpenNow)
    val categoryLabel = language.categoryLabel(place.category)
    val priceLabel = language.priceLevelLabel(place.priceLevel)
    val neighborhoodLabel = place.neighborhood.takeIf { it.isNotBlank() }

    return PlaceDetailsUiModel(
        tripId = id,
        dayId = dayId,
        placeId = placeId,
        title = place.name,
        address = place.address,
        city = city,
        dayLabel = language.dayLabel(day.dayIndex),
        dayTitle = day.title,
        visitTimeLabel = formatDuration(place.visitMinutes, language),
        bestTimeLabel = place.bestTimeToVisit.ifBlank { language.bestTimeLabel(stopNumber, totalStops) },
        statusLabel = if (place.isCompleted) language.placeCompletedStatusLabel() else language.placePlannedStatusLabel(),
        categoryLabel = categoryLabel,
        openingStatusLabel = openingStatusLabel,
        neighborhoodLabel = neighborhoodLabel,
        priceLabel = priceLabel,
        isCompleted = place.isCompleted,
        latitude = place.latitude,
        longitude = place.longitude,
        heroImageUrl = backendPhotos.firstOrNull()?.toBackendImageUrl(),
        heroImageAttribution = backendPhotos.firstOrNull()?.attribution,
        gallery = if (backendPhotos.isNotEmpty()) {
            backendPhotos.mapIndexed { index, photo ->
                PlaceGalleryImageUiModel(
                    id = "${place.id}-gallery-$index",
                    imageUrl = photo.toBackendImageUrl(),
                    attribution = photo.attribution,
                )
            }
        } else {
            listOf(
                PlaceGalleryImageUiModel(
                    id = "${place.id}-placeholder-1",
                    imageUrl = null,
                    attribution = null,
                ),
                PlaceGalleryImageUiModel(
                    id = "${place.id}-placeholder-2",
                    imageUrl = null,
                    attribution = null,
                ),
            )
        },
        aboutText = language.placeAboutText(
            placeName = place.name,
            address = place.address,
            note = place.note,
            shortDescription = place.shortDescription,
            fullDescription = place.fullDescription,
        ),
        whyInRouteText = language.placeWhyInRouteText(
            placeName = place.name,
            dayTitle = day.title,
            position = stopNumber,
            total = totalStops,
            whyIncluded = place.whyIncluded,
        ),
        tipsText = language.placeTipsText(
            tips = place.tips,
            visitNotes = place.visitNotes,
            visitTimeLabel = formatDuration(place.visitMinutes, language),
            address = place.address,
            hasPhoto = backendPhotos.isNotEmpty(),
        ),
        visitDetailsText = language.placeVisitDetailsText(
            openingHoursText = place.openingHoursText,
            websiteUrl = place.websiteUrl,
            neighborhood = place.neighborhood,
        ),
        websiteUrl = place.websiteUrl,
        routeContext = RouteContextUiModel(
            dayLabel = language.dayLabel(day.dayIndex),
            dayTitle = day.title,
            stopLabel = language.stopLabel(stopNumber, totalStops),
            previousPlaceName = place.previousPlaceName ?: day.places.getOrNull(placeIndex - 1)?.name,
            nextPlaceName = place.nextPlaceName ?: day.places.getOrNull(placeIndex + 1)?.name,
        ),
    )
}

internal fun Trip.toDayRouteMapUi(
    language: AppLanguage,
    dayId: String,
): DayRouteMapUiModel? {
    val day = days.firstOrNull { it.id == dayId } ?: return null
    val stops = day.places.mapIndexedNotNull { index, place ->
        val latitude = place.latitude ?: return@mapIndexedNotNull null
        val longitude = place.longitude ?: return@mapIndexedNotNull null
        DayRouteStopUiModel(
            id = place.id,
            numberLabel = (index + 1).toString(),
            title = place.name,
            address = place.address,
            latitude = latitude,
            longitude = longitude,
        )
    }

    return DayRouteMapUiModel(
        tripId = id,
        dayId = dayId,
        city = city,
        dayLabel = language.dayLabel(day.dayIndex),
        title = day.title,
        durationLabel = formatDuration(day.durationMinutes, language),
        distanceLabel = formatDistance(day.distanceKm, language),
        stops = stops,
    )
}

internal fun PlacePhoto.toUi(): PlacePhotoUiModel = PlacePhotoUiModel(
    id = id,
    localUri = localUri,
)

private fun formatDuration(totalMinutes: Int, language: AppLanguage): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) language.hoursLabel(hours, minutes) else language.minutesLabel(minutes)
}

private fun formatDistance(distanceKm: Double, language: AppLanguage): String = language.distanceLabel(distanceKm)

private fun PlaceRemotePhoto.toBackendImageUrl(maxWidth: Int = 1200): String {
    val encodedRef = ref.encodeURLParameter()
    return "${localBackendBaseUrl()}/api/place-photos?ref=$encodedRef&maxWidth=$maxWidth"
}
