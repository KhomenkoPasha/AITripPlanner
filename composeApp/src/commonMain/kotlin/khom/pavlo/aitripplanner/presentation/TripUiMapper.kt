package khom.pavlo.aitripplanner.presentation

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceGalleryImageUiModel
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
            places = day.places.map { place ->
                PlaceUiModel(
                    id = place.id,
                    name = place.name,
                    address = place.address,
                    visitTimeLabel = formatDuration(place.visitMinutes, language),
                    note = place.shortDescription.ifBlank { place.note },
                    photoUrl = place.photoUrl,
                    photoAttribution = place.photoAttribution,
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
    val galleryUrls = buildList {
        if (!place.photoUrl.isNullOrBlank()) add(place.photoUrl)
        addAll(place.photoUrls.filter { it.isNotBlank() })
    }.distinct()
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
        heroImageUrl = place.photoUrl,
        heroImageAttribution = place.photoAttribution,
        gallery = if (galleryUrls.isNotEmpty()) {
            galleryUrls.mapIndexed { index, imageUrl ->
                PlaceGalleryImageUiModel(
                    id = "${place.id}-gallery-$index",
                    imageUrl = imageUrl,
                    attribution = place.photoAttribution,
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
            hasPhoto = galleryUrls.isNotEmpty(),
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

private fun formatDuration(totalMinutes: Int, language: AppLanguage): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) language.hoursLabel(hours, minutes) else language.minutesLabel(minutes)
}

private fun formatDistance(distanceKm: Double, language: AppLanguage): String = language.distanceLabel(distanceKm)
