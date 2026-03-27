package khom.pavlo.aitripplanner.sync

import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.data.local.TripLocalDataSource
import khom.pavlo.aitripplanner.data.remote.TripsRemoteDataSource
import khom.pavlo.aitripplanner.domain.model.AppSyncState
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

interface BackgroundSyncScheduler {
    suspend fun scheduleSync(trigger: SyncTrigger)
}

interface SyncEngine {
    val state: StateFlow<AppSyncState>
    suspend fun requestSync(trigger: SyncTrigger)
}

class TripSyncEngine(
    private val localDataSource: TripLocalDataSource,
    private val remoteDataSource: TripsRemoteDataSource,
) : SyncEngine {
    private val mutex = Mutex()
    private val mutableState = MutableStateFlow(AppSyncState())

    override val state: StateFlow<AppSyncState> = mutableState.asStateFlow()

    override suspend fun requestSync(trigger: SyncTrigger) {
        mutex.withLock {
            val queued = localDataSource.listPendingSyncItems()
            mutableState.value = mutableState.value.copy(
                isRunning = true,
                lastTrigger = trigger,
                queuedItems = queued.size,
                lastError = null,
            )

            queued.forEach { item ->
                remoteDataSource.pushPendingOperation(item).fold(
                    onSuccess = {
                        localDataSource.completeSync(
                            queueId = item.id,
                            tripId = item.entityId,
                            syncedAt = PlatformTime.nowMillis(),
                        )
                    },
                    onFailure = { error ->
                        localDataSource.markSyncRetry(
                            queueId = item.id,
                            attemptCount = item.attemptCount + 1,
                            error = error.message ?: "Sync failed",
                        )
                    },
                )
            }

            runCatching {
                remoteDataSource.fetchTrips()
            }.onSuccess { trips ->
                if (trips.isNotEmpty()) {
                    localDataSource.replaceTrips(trips)
                }
            }.onFailure { error ->
                mutableState.value = mutableState.value.copy(
                    lastError = error.message ?: "Refresh failed",
                )
            }

            val remaining = localDataSource.listPendingSyncItems().size
            mutableState.value = mutableState.value.copy(
                isRunning = false,
                queuedItems = remaining,
                lastCompletedAtEpochMillis = PlatformTime.nowMillis(),
            )
        }
    }
}
