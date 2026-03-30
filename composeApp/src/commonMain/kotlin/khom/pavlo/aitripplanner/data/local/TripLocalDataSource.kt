package khom.pavlo.aitripplanner.data.local

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.core.platform.SqlDriverFactory
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.SyncOperationType
import khom.pavlo.aitripplanner.domain.model.SyncQueueItem
import khom.pavlo.aitripplanner.domain.model.SyncQueueState
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripDay
import khom.pavlo.aitripplanner.domain.model.TripPlace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DatabaseFactory(
    private val driverFactory: SqlDriverFactory,
) {
    fun create(): TravelPlannerDatabase = TravelPlannerDatabase(driverFactory.createDriver())
}

class TripLocalDataSource(
    database: TravelPlannerDatabase,
) {
    private val queries = database.tripsQueries

    init {
        queries.seedSettings()
    }

    fun observeTrips(): Flow<List<Trip>> = combine(
        queries.selectTrips().asFlow().mapToList(Dispatchers.Default),
        queries.selectAllDays().asFlow().mapToList(Dispatchers.Default),
        queries.selectAllPlaces().asFlow().mapToList(Dispatchers.Default),
    ) { trips, days, places ->
        trips.map { tripRow ->
            val tripDays = days
                .filter { it.trip_id == tripRow.id }
                .sortedBy { it.day_index.toInt() }
                .map { dayRow ->
                    TripDay(
                        id = dayRow.id,
                        tripId = dayRow.trip_id,
                        dayIndex = dayRow.day_index.toInt(),
                        title = dayRow.title,
                        summary = dayRow.summary,
                        durationMinutes = dayRow.duration_minutes.toInt(),
                        distanceKm = dayRow.distance_km,
                        isExpanded = dayRow.is_expanded != 0L,
                        places = places
                            .filter { it.day_id == dayRow.id }
                            .sortedBy { it.sort_index.toInt() }
                            .map { placeRow ->
                                TripPlace(
                                    id = placeRow.id,
                                    dayId = placeRow.day_id,
                                    sortIndex = placeRow.sort_index.toInt(),
                                    name = placeRow.name,
                                    latitude = placeRow.latitude,
                                    longitude = placeRow.longitude,
                                    address = placeRow.address,
                                    visitMinutes = placeRow.visit_minutes.toInt(),
                                    note = placeRow.note,
                                    category = placeRow.category,
                                    shortDescription = placeRow.short_description,
                                    fullDescription = placeRow.full_description,
                                    whyIncluded = placeRow.why_included,
                                    tips = placeRow.tips_text.decodeLines(),
                                    openingHoursText = placeRow.opening_hours_text,
                                    bestTimeToVisit = placeRow.best_time_to_visit,
                                    isOpenNow = placeRow.is_open_now?.let { it != 0L },
                                    websiteUrl = placeRow.website_url,
                                    photoUrl = placeRow.photo_url,
                                    photoUrls = placeRow.photo_urls_text.decodeLines(),
                                    photoAttribution = placeRow.photo_attribution,
                                    priceLevel = placeRow.price_level,
                                    visitNotes = placeRow.visit_notes,
                                    neighborhood = placeRow.neighborhood,
                                    stopIndex = placeRow.stop_index?.toInt(),
                                    previousPlaceName = placeRow.previous_place_name,
                                    nextPlaceName = placeRow.next_place_name,
                                    isCompleted = placeRow.is_completed != 0L,
                                )
                            },
                    )
                }

            Trip(
                id = tripRow.id,
                city = tripRow.city,
                title = tripRow.title,
                summary = tripRow.summary,
                heroNote = tripRow.hero_note,
                durationMinutes = tripRow.duration_minutes.toInt(),
                distanceKm = tripRow.distance_km,
                isFavorite = tripRow.is_favorite != 0L,
                isOfflineOnly = tripRow.is_offline_only != 0L,
                isPendingSync = tripRow.is_pending_sync != 0L,
                remoteVersion = tripRow.remote_version,
                updatedAtEpochMillis = tripRow.updated_at_epoch_ms,
                days = tripDays,
            )
        }
    }

    fun observeTrip(tripId: String): Flow<Trip?> = observeTrips().map { trips ->
        trips.firstOrNull { it.id == tripId }
    }

    fun observeAppLanguage(): Flow<AppLanguage> = queries.selectAppLanguage()
        .asFlow()
        .mapToOneOrNull(Dispatchers.Default)
        .map { language -> language?.let(AppLanguage::valueOf) ?: AppLanguage.EN }

    suspend fun getCurrentLanguage(): AppLanguage = withContext(Dispatchers.Default) {
        queries.selectAppLanguage().executeAsOneOrNull()?.let(AppLanguage::valueOf) ?: AppLanguage.EN
    }

    suspend fun setAppLanguage(language: AppLanguage) = withContext(Dispatchers.Default) {
        queries.updateAppLanguage(language.name)
    }

    suspend fun upsertTrip(trip: Trip) = withContext(Dispatchers.Default) {
        queries.transaction {
            val existingDayIds = queries.selectDayIdsByTripId(trip.id).executeAsList()
            existingDayIds.forEach { dayId -> queries.deletePlacesByDayId(dayId) }
            queries.deleteDaysByTripId(trip.id)
            insertFullTrip(trip)
        }
    }

    suspend fun removeLegacyMockTrips() = withContext(Dispatchers.Default) {
        queries.transaction {
            queries.deleteLegacyMockPlaces()
            queries.deleteLegacyMockDays()
            queries.deleteLegacyMockSyncItems()
            queries.deleteLegacyMockTrips()
        }
    }

    suspend fun deleteTrip(tripId: String) = withContext(Dispatchers.Default) {
        queries.transaction {
            val existingDayIds = queries.selectDayIdsByTripId(tripId).executeAsList()
            existingDayIds.forEach { dayId -> queries.deletePlacesByDayId(dayId) }
            queries.deleteDaysByTripId(tripId)
            queries.deleteSyncItemsByEntityId(tripId)
            queries.deleteTripById(tripId)
        }
    }

    suspend fun replaceTrips(trips: List<Trip>) = withContext(Dispatchers.Default) {
        queries.transaction {
            queries.deleteAllPlaces()
            queries.deleteAllDays()
            queries.deleteAllTrips()
            trips.forEach(::insertFullTrip)
        }
    }

    suspend fun setDayExpanded(dayId: String, expanded: Boolean) = withContext(Dispatchers.Default) {
        queries.setDayExpanded(is_expanded = expanded.asLong(), id = dayId)
    }

    suspend fun enqueueSync(item: SyncQueueItem) = withContext(Dispatchers.Default) {
        queries.enqueueSyncItem(
            id = item.id,
            entity_id = item.entityId,
            entity_type = item.entityType,
            operation = item.operation.name,
            payload_json = item.payloadJson,
            state = item.state.name,
            attempt_count = item.attemptCount.toLong(),
            base_version = item.baseVersion,
            conflict_token = item.conflictToken,
            last_error = item.lastError,
            created_at_epoch_ms = item.createdAtEpochMillis,
            updated_at_epoch_ms = item.updatedAtEpochMillis,
        )
    }

    suspend fun listPendingSyncItems(): List<SyncQueueItem> = withContext(Dispatchers.Default) {
        queries.selectPendingSyncItems().executeAsList().map { row ->
            SyncQueueItem(
                id = row.id,
                entityId = row.entity_id,
                entityType = row.entity_type,
                operation = SyncOperationType.valueOf(row.operation),
                payloadJson = row.payload_json,
                state = SyncQueueState.valueOf(row.state),
                attemptCount = row.attempt_count.toInt(),
                baseVersion = row.base_version,
                conflictToken = row.conflict_token,
                lastError = row.last_error,
                createdAtEpochMillis = row.created_at_epoch_ms,
                updatedAtEpochMillis = row.updated_at_epoch_ms,
            )
        }
    }

    suspend fun completeSync(queueId: String, tripId: String, syncedAt: Long) = withContext(Dispatchers.Default) {
        queries.transaction {
            queries.deleteSyncItem(queueId)
            queries.markTripSynced(
                is_pending_sync = 0,
                updated_at_epoch_ms = syncedAt,
                id = tripId,
            )
        }
    }

    suspend fun markSyncRetry(queueId: String, attemptCount: Int, error: String) = withContext(Dispatchers.Default) {
        queries.markSyncRetry(
            state = SyncQueueState.RETRY.name,
            attempt_count = attemptCount.toLong(),
            last_error = error,
            updated_at_epoch_ms = PlatformTime.nowMillis(),
            id = queueId,
        )
    }

    private fun insertFullTrip(trip: Trip) {
        queries.insertTrip(
            id = trip.id,
            city = trip.city,
            title = trip.title,
            summary = trip.summary,
            hero_note = trip.heroNote,
            duration_minutes = trip.durationMinutes.toLong(),
            distance_km = trip.distanceKm,
            is_favorite = trip.isFavorite.asLong(),
            is_offline_only = trip.isOfflineOnly.asLong(),
            is_pending_sync = trip.isPendingSync.asLong(),
            remote_version = trip.remoteVersion,
            updated_at_epoch_ms = trip.updatedAtEpochMillis,
        )
        trip.days.forEach { day ->
            queries.insertDay(
                id = day.id,
                trip_id = trip.id,
                day_index = day.dayIndex.toLong(),
                title = day.title,
                summary = day.summary,
                duration_minutes = day.durationMinutes.toLong(),
                distance_km = day.distanceKm,
                is_expanded = day.isExpanded.asLong(),
            )
            day.places.forEach { place ->
                queries.insertPlace(
                    id = place.id,
                    day_id = day.id,
                    sort_index = place.sortIndex.toLong(),
                    name = place.name,
                    latitude = place.latitude,
                    longitude = place.longitude,
                    address = place.address,
                    visit_minutes = place.visitMinutes.toLong(),
                    note = place.note,
                    category = place.category,
                    short_description = place.shortDescription,
                    full_description = place.fullDescription,
                    why_included = place.whyIncluded,
                    tips_text = place.tips.encodeLines(),
                    opening_hours_text = place.openingHoursText,
                    best_time_to_visit = place.bestTimeToVisit,
                    is_open_now = place.isOpenNow?.asLong(),
                    website_url = place.websiteUrl,
                    photo_url = place.photoUrl,
                    photo_urls_text = place.photoUrls.encodeLines(),
                    photo_attribution = place.photoAttribution,
                    price_level = place.priceLevel,
                    visit_notes = place.visitNotes,
                    neighborhood = place.neighborhood,
                    stop_index = place.stopIndex?.toLong(),
                    previous_place_name = place.previousPlaceName,
                    next_place_name = place.nextPlaceName,
                    is_completed = place.isCompleted.asLong(),
                )
            }
        }
    }

    private fun Boolean.asLong(): Long = if (this) 1L else 0L
    private fun List<String>.encodeLines(): String = filter { it.isNotBlank() }.joinToString(separator = "\n")
    private fun String.decodeLines(): List<String> = lineSequence().map(String::trim).filter(String::isNotEmpty).toList()
}
