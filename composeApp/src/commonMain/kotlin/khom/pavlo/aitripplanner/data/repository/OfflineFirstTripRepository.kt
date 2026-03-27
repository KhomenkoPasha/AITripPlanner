package khom.pavlo.aitripplanner.data.repository

import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.data.local.TripLocalDataSource
import khom.pavlo.aitripplanner.data.remote.SampleTripFactory
import khom.pavlo.aitripplanner.domain.model.SyncOperationType
import khom.pavlo.aitripplanner.domain.model.SyncQueueItem
import khom.pavlo.aitripplanner.domain.model.SyncQueueState
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.repository.TripRepository
import khom.pavlo.aitripplanner.sync.BackgroundSyncScheduler
import khom.pavlo.aitripplanner.sync.SyncEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random

class OfflineFirstTripRepository(
    private val localDataSource: TripLocalDataSource,
    private val syncEngine: SyncEngine,
    private val backgroundSyncScheduler: BackgroundSyncScheduler,
    private val json: Json,
) : TripRepository {
    override fun observeTrips(): Flow<List<Trip>> = localDataSource.observeTrips()

    override fun observeTrip(tripId: String): Flow<Trip?> = localDataSource.observeTrip(tripId)

    override fun observeSyncState() = syncEngine.state.onStart {
        emit(syncEngine.state.value)
    }

    override suspend fun ensureSeedData() {
        if (!localDataSource.hasTrips()) {
            localDataSource.replaceTrips(SampleTripFactory.seedTrips())
        }
    }

    override suspend fun generateTrip(prompt: String): Trip = createAndQueue(
        SampleTripFactory.fromPrompt(prompt),
    )

    override suspend fun createTrip(input: TripEditorInput): Trip = createAndQueue(
        SampleTripFactory.createFromInput(input),
    )

    override suspend fun updateTrip(tripId: String, input: TripEditorInput): Trip {
        val existing = observeTrip(tripId).first() ?: error("Trip not found")
        val updated = SampleTripFactory.updateExisting(existing, input)
        return createAndQueue(updated)
    }

    override suspend fun deleteTrip(tripId: String) {
        localDataSource.enqueueSync(
            SyncQueueItem(
                id = "sync-${Random.nextInt(100000, 999999)}",
                entityId = tripId,
                entityType = "trip",
                operation = SyncOperationType.DELETE_TRIP,
                payloadJson = json.encodeToString(DeleteTripSyncPayload(tripId)),
                state = SyncQueueState.PENDING,
                attemptCount = 0,
                baseVersion = null,
                conflictToken = PlatformTime.nowMillis().toString(),
                lastError = null,
                createdAtEpochMillis = PlatformTime.nowMillis(),
                updatedAtEpochMillis = PlatformTime.nowMillis(),
            ),
        )
        localDataSource.deleteTrip(tripId)
        backgroundSyncScheduler.scheduleSync(SyncTrigger.USER)
    }

    override suspend fun setDayExpanded(dayId: String, expanded: Boolean) {
        localDataSource.setDayExpanded(dayId, expanded)
    }

    override suspend fun requestSync(trigger: SyncTrigger) {
        backgroundSyncScheduler.scheduleSync(trigger)
        syncEngine.requestSync(trigger)
    }

    private suspend fun createAndQueue(trip: Trip): Trip {
        localDataSource.upsertTrip(trip)
        localDataSource.enqueueSync(
            SyncQueueItem(
                id = "sync-${Random.nextInt(100000, 999999)}",
                entityId = trip.id,
                entityType = "trip",
                operation = SyncOperationType.UPSERT_TRIP,
                payloadJson = json.encodeToString(TripSyncPayload.fromTrip(trip)),
                state = SyncQueueState.PENDING,
                attemptCount = 0,
                baseVersion = trip.remoteVersion,
                conflictToken = trip.updatedAtEpochMillis.toString(),
                lastError = null,
                createdAtEpochMillis = PlatformTime.nowMillis(),
                updatedAtEpochMillis = PlatformTime.nowMillis(),
            ),
        )
        backgroundSyncScheduler.scheduleSync(SyncTrigger.USER)
        return trip
    }
}

@Serializable
data class TripSyncPayload(
    val id: String,
    val city: String,
    val title: String,
    val summary: String,
    val heroNote: String,
    val updatedAtEpochMillis: Long,
) {
    companion object {
        fun fromTrip(trip: Trip) = TripSyncPayload(
            id = trip.id,
            city = trip.city,
            title = trip.title,
            summary = trip.summary,
            heroNote = trip.heroNote,
            updatedAtEpochMillis = trip.updatedAtEpochMillis,
        )
    }
}

@Serializable
data class DeleteTripSyncPayload(
    val id: String,
)
