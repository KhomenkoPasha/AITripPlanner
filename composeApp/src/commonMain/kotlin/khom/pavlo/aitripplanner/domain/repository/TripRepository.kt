package khom.pavlo.aitripplanner.domain.repository

import kotlinx.coroutines.flow.Flow
import khom.pavlo.aitripplanner.domain.model.AppSyncState
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripEditorInput

interface TripRepository {
    fun observeTrips(): Flow<List<Trip>>
    fun observeTrip(tripId: String): Flow<Trip?>
    fun observeSyncState(): Flow<AppSyncState>
    suspend fun ensureSeedData()
    suspend fun generateTrip(prompt: String): Trip
    suspend fun createTrip(input: TripEditorInput): Trip
    suspend fun updateTrip(tripId: String, input: TripEditorInput): Trip
    suspend fun deleteTrip(tripId: String)
    suspend fun setDayExpanded(dayId: String, expanded: Boolean)
    suspend fun requestSync(trigger: SyncTrigger)
}
