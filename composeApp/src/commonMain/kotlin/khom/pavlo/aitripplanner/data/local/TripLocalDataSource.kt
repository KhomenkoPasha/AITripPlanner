package khom.pavlo.aitripplanner.data.local

import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.core.platform.getDatabaseBuilder
import khom.pavlo.aitripplanner.data.local.db.AppSettingsEntity
import khom.pavlo.aitripplanner.data.local.db.DayGraph
import khom.pavlo.aitripplanner.data.local.db.SyncQueueEntity
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import khom.pavlo.aitripplanner.data.local.db.TripGraph
import khom.pavlo.aitripplanner.data.local.db.buildTravelPlannerDatabase
import khom.pavlo.aitripplanner.data.local.db.toDomain
import khom.pavlo.aitripplanner.data.local.db.toEntities
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.SyncQueueItem
import khom.pavlo.aitripplanner.domain.model.SyncQueueState
import khom.pavlo.aitripplanner.domain.model.Trip
import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class DatabaseFactory {
    fun create(): TravelPlannerDatabase = buildTravelPlannerDatabase(getDatabaseBuilder())
}

class TripLocalDataSource(
    database: TravelPlannerDatabase,
) {
    private val database = database
    private val tripDao = database.tripDao()
    private val photoStorageJson = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    fun observeTrips(): Flow<List<Trip>> = combine(
        tripDao.observeTrips(),
        tripDao.observeAllDays(),
        tripDao.observeAllPlaces(),
    ) { trips, days, places ->
        trips.map { tripRow ->
            TripGraph(
                trip = tripRow,
                days = days
                    .filter { it.tripId == tripRow.id }
                    .sortedBy { it.dayIndex }
                    .map { dayRow ->
                        DayGraph(
                            day = dayRow,
                            places = places
                                .filter { it.dayId == dayRow.id }
                                .sortedBy { it.sortIndex },
                        )
                    },
            ).toDomain(photoStorageJson)
        }
    }

    fun observeTrip(tripId: String): Flow<Trip?> = observeTrips().map { trips ->
        trips.firstOrNull { it.id == tripId }
    }

    fun observeAppLanguage(): Flow<AppLanguage> = tripDao.observeAppLanguage()
        .map { language -> language?.let(AppLanguage::valueOf) ?: AppLanguage.EN }

    suspend fun getCurrentLanguage(): AppLanguage =
        tripDao.getCurrentLanguage()?.let(AppLanguage::valueOf) ?: AppLanguage.EN

    suspend fun setAppLanguage(language: AppLanguage) {
        tripDao.upsertAppSettings(AppSettingsEntity(appLanguage = language.name))
    }

    suspend fun upsertTrip(trip: Trip) {
        upsertTripInternal(trip)
    }

    suspend fun deleteTrip(
        tripId: String,
        deleteSyncItem: SyncQueueItem? = null,
    ) {
        database.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existingDayIds = tripDao.selectDayIdsByTripId(tripId)
                existingDayIds.forEach { dayId ->
                    tripDao.deletePlacesByDayId(dayId)
                }
                tripDao.deleteDaysByTripId(tripId)
                tripDao.deleteSyncItemsByEntityId(tripId)
                if (deleteSyncItem != null) {
                    insertSyncItem(deleteSyncItem)
                }
                tripDao.deleteTripById(tripId)
            }
        }
    }

    suspend fun replaceTrips(trips: List<Trip>) {
        database.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                tripDao.deleteAllPlaces()
                tripDao.deleteAllDays()
                tripDao.deleteAllTrips()
                trips.forEach { trip ->
                    insertFullTrip(trip)
                }
            }
        }
    }

    suspend fun mergeRemoteTrips(
        remoteTrips: List<Trip>,
        protectedTripIds: Set<String>,
    ) {
        val currentTrips = observeTrips().first()
        val currentTripsById = currentTrips.associateBy(Trip::id)
        val protectedTrips = currentTrips.filter { trip -> trip.id in protectedTripIds }
        val mergedTrips = buildList {
            addAll(
                remoteTrips
                    .filterNot { trip -> trip.id in protectedTripIds }
                    .map { remoteTrip ->
                        currentTripsById[remoteTrip.id]
                            ?.let(remoteTrip::withLocalPlaceCompletion)
                            ?: remoteTrip
                    },
            )
            addAll(protectedTrips)
        }.sortedByDescending { trip -> trip.updatedAtEpochMillis }

        replaceTrips(mergedTrips)
    }

    suspend fun setDayExpanded(dayId: String, expanded: Boolean) {
        tripDao.setDayExpanded(dayId = dayId, expanded = expanded)
    }

    suspend fun enqueueSync(item: SyncQueueItem) {
        insertSyncItem(item)
    }

    suspend fun listPendingSyncItems(): List<SyncQueueItem> =
        tripDao.selectPendingSyncItems().map { row -> row.toDomain() }

    suspend fun completeSync(queueId: String, tripId: String, syncedAt: Long) {
        database.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                tripDao.deleteSyncItem(queueId)
                tripDao.markTripSynced(
                    tripId = tripId,
                    isPendingSync = false,
                    updatedAt = syncedAt,
                )
            }
        }
    }

    suspend fun markSyncRetry(queueId: String, attemptCount: Int, error: String) {
        tripDao.markSyncRetry(
            queueId = queueId,
            state = SyncQueueState.RETRY.name,
            attemptCount = attemptCount,
            lastError = error,
            updatedAt = PlatformTime.nowMillis(),
        )
    }

    private suspend fun upsertTripInternal(trip: Trip) {
        database.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val existingDayIds = tripDao.selectDayIdsByTripId(trip.id)
                existingDayIds.forEach { dayId ->
                    tripDao.deletePlacesByDayId(dayId)
                }
                tripDao.deleteDaysByTripId(trip.id)
                insertFullTrip(trip)
            }
        }
    }

    private suspend fun insertSyncItem(item: SyncQueueItem) {
        tripDao.insertSyncItem(
            SyncQueueEntity(
                id = item.id,
                entityId = item.entityId,
                entityType = item.entityType,
                operation = item.operation.name,
                payloadJson = item.payloadJson,
                state = item.state.name,
                attemptCount = item.attemptCount,
                baseVersion = item.baseVersion,
                conflictToken = item.conflictToken,
                lastError = item.lastError,
                createdAtEpochMillis = item.createdAtEpochMillis,
                updatedAtEpochMillis = item.updatedAtEpochMillis,
            ),
        )
    }

    private suspend fun insertFullTrip(trip: Trip) {
        val graph = trip.toEntities(photoStorageJson)
        tripDao.insertTrip(graph.trip)
        graph.days.forEach { dayGraph ->
            tripDao.insertDay(dayGraph.day)
            dayGraph.places.forEach { place ->
                tripDao.insertPlace(place)
            }
        }
    }
}

private fun Trip.withLocalPlaceCompletion(localTrip: Trip): Trip = copy(
    days = days.map { remoteDay ->
        val localDay = localTrip.days.firstOrNull { it.id == remoteDay.id }
        if (localDay == null) {
            remoteDay
        } else {
            remoteDay.copy(
                places = remoteDay.places.map { remotePlace ->
                    localDay.places
                        .firstOrNull { it.id == remotePlace.id }
                        ?.let { localPlace -> remotePlace.copy(isCompleted = localPlace.isCompleted) }
                        ?: remotePlace
                },
            )
        }
    },
)
