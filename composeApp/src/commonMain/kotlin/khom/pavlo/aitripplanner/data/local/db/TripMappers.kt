package khom.pavlo.aitripplanner.data.local.db

import khom.pavlo.aitripplanner.domain.model.PlacePhoto
import khom.pavlo.aitripplanner.domain.model.SyncOperationType
import khom.pavlo.aitripplanner.domain.model.SyncQueueItem
import khom.pavlo.aitripplanner.domain.model.SyncQueueState
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripDay
import khom.pavlo.aitripplanner.domain.model.TripPlace
import kotlinx.serialization.json.Json

internal fun TripGraph.toDomain(json: Json): Trip = Trip(
    id = trip.id,
    city = trip.city,
    title = trip.title,
    summary = trip.summary,
    heroNote = trip.heroNote,
    durationMinutes = trip.durationMinutes,
    distanceKm = trip.distanceKm,
    isFavorite = trip.isFavorite,
    isOfflineOnly = trip.isOfflineOnly,
    isPendingSync = trip.isPendingSync,
    remoteVersion = trip.remoteVersion,
    updatedAtEpochMillis = trip.updatedAtEpochMillis,
    days = days.map { dayGraph ->
        TripDay(
            id = dayGraph.day.id,
            tripId = dayGraph.day.tripId,
            dayIndex = dayGraph.day.dayIndex,
            title = dayGraph.day.title,
            summary = dayGraph.day.summary,
            durationMinutes = dayGraph.day.durationMinutes,
            distanceKm = dayGraph.day.distanceKm,
            isExpanded = dayGraph.day.isExpanded,
            places = dayGraph.places.map { place ->
                TripPlace(
                    id = place.id,
                    dayId = place.dayId,
                    sortIndex = place.sortIndex,
                    name = place.name,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    address = place.address,
                    visitMinutes = place.visitMinutes,
                    note = place.note,
                    category = place.category,
                    shortDescription = place.shortDescription,
                    fullDescription = place.fullDescription,
                    whyIncluded = place.whyIncluded,
                    tips = place.tipsText.decodeLines(),
                    openingHoursText = place.openingHoursText,
                    bestTimeToVisit = place.bestTimeToVisit,
                    isOpenNow = place.isOpenNow,
                    websiteUrl = place.websiteUrl,
                    photos = decodeStoredPhotos(
                        json = json,
                        raw = place.photoUrlsText,
                        legacyPrimaryRef = place.photoUrl,
                        legacyPrimaryAttribution = place.photoAttribution,
                    ),
                    priceLevel = place.priceLevel,
                    visitNotes = place.visitNotes,
                    neighborhood = place.neighborhood,
                    stopIndex = place.stopIndex,
                    previousPlaceName = place.previousPlaceName,
                    nextPlaceName = place.nextPlaceName,
                    isCompleted = place.isCompleted,
                )
            },
        )
    },
)

internal fun Trip.toEntities(json: Json): TripGraph = TripGraph(
    trip = TripEntity(
        id = id,
        city = city,
        title = title,
        summary = summary,
        heroNote = heroNote,
        durationMinutes = durationMinutes,
        distanceKm = distanceKm,
        isFavorite = isFavorite,
        isOfflineOnly = isOfflineOnly,
        isPendingSync = isPendingSync,
        remoteVersion = remoteVersion,
        updatedAtEpochMillis = updatedAtEpochMillis,
    ),
    days = days.map { day ->
        DayGraph(
            day = DayEntity(
                id = day.id,
                tripId = id,
                dayIndex = day.dayIndex,
                title = day.title,
                summary = day.summary,
                durationMinutes = day.durationMinutes,
                distanceKm = day.distanceKm,
                isExpanded = day.isExpanded,
            ),
            places = day.places.map { place ->
                PlaceEntity(
                    id = place.id,
                    dayId = day.id,
                    sortIndex = place.sortIndex,
                    name = place.name,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    address = place.address,
                    visitMinutes = place.visitMinutes,
                    note = place.note,
                    category = place.category,
                    shortDescription = place.shortDescription,
                    fullDescription = place.fullDescription,
                    whyIncluded = place.whyIncluded,
                    tipsText = place.tips.encodeLines(),
                    openingHoursText = place.openingHoursText,
                    bestTimeToVisit = place.bestTimeToVisit,
                    isOpenNow = place.isOpenNow,
                    websiteUrl = place.websiteUrl,
                    photoUrl = place.photos.firstOrNull()?.ref,
                    photoUrlsText = place.photos.encodeStoredPhotos(json),
                    photoAttribution = place.photos.firstOrNull()?.attribution,
                    priceLevel = place.priceLevel,
                    visitNotes = place.visitNotes,
                    neighborhood = place.neighborhood,
                    stopIndex = place.stopIndex,
                    previousPlaceName = place.previousPlaceName,
                    nextPlaceName = place.nextPlaceName,
                    isCompleted = place.isCompleted,
                )
            },
        )
    },
)

internal fun SyncQueueEntity.toDomain(): SyncQueueItem = SyncQueueItem(
    id = id,
    entityId = entityId,
    entityType = entityType,
    operation = SyncOperationType.valueOf(operation),
    payloadJson = payloadJson,
    state = SyncQueueState.valueOf(state),
    attemptCount = attemptCount,
    baseVersion = baseVersion,
    conflictToken = conflictToken,
    lastError = lastError,
    createdAtEpochMillis = createdAtEpochMillis,
    updatedAtEpochMillis = updatedAtEpochMillis,
)

internal fun PlacePhotoEntity.toDomain(): PlacePhoto = PlacePhoto(
    id = id,
    tripId = tripId,
    placeId = placeId,
    localUri = localUri,
    createdAtEpochMillis = createdAtEpochMillis,
)
