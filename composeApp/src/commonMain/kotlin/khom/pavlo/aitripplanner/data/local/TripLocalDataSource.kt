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
                                    address = placeRow.address,
                                    visitMinutes = placeRow.visit_minutes.toInt(),
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

    suspend fun hasTrips(): Boolean = withContext(Dispatchers.Default) {
        queries.selectTripCount().executeAsOne() > 0L
    }

    suspend fun upsertTrip(trip: Trip) = withContext(Dispatchers.Default) {
        queries.transaction {
            val existingDayIds = queries.selectDayIdsByTripId(trip.id).executeAsList()
            existingDayIds.forEach { dayId -> queries.deletePlacesByDayId(dayId) }
            queries.deleteDaysByTripId(trip.id)
            insertFullTrip(trip)
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
                    address = place.address,
                    visit_minutes = place.visitMinutes.toLong(),
                )
            }
        }
    }

    private fun Boolean.asLong(): Long = if (this) 1L else 0L
}
