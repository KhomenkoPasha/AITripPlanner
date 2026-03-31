package khom.pavlo.aitripplanner.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Query("SELECT * FROM TripEntity ORDER BY updated_at_epoch_ms DESC")
    fun observeTrips(): Flow<List<TripEntity>>

    @Query("SELECT * FROM DayEntity ORDER BY day_index ASC")
    fun observeAllDays(): Flow<List<DayEntity>>

    @Query("SELECT * FROM PlaceEntity ORDER BY sort_index ASC")
    fun observeAllPlaces(): Flow<List<PlaceEntity>>

    @Query("SELECT app_language FROM AppSettingsEntity WHERE id = 1")
    fun observeAppLanguage(): Flow<String?>

    @Query("SELECT app_language FROM AppSettingsEntity WHERE id = 1")
    suspend fun getCurrentLanguage(): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAppSettings(settings: AppSettingsEntity)

    @Query("SELECT id FROM DayEntity WHERE trip_id = :tripId")
    suspend fun selectDayIdsByTripId(tripId: String): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: DayEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlace(place: PlaceEntity)

    @Query("DELETE FROM TripEntity WHERE id = :tripId")
    suspend fun deleteTripById(tripId: String)

    @Query("DELETE FROM DayEntity WHERE trip_id = :tripId")
    suspend fun deleteDaysByTripId(tripId: String)

    @Query("DELETE FROM PlaceEntity WHERE day_id = :dayId")
    suspend fun deletePlacesByDayId(dayId: String)

    @Query("DELETE FROM SyncQueueEntity WHERE entity_id = :entityId")
    suspend fun deleteSyncItemsByEntityId(entityId: String)

    @Query("DELETE FROM PlaceEntity")
    suspend fun deleteAllPlaces()

    @Query("DELETE FROM DayEntity")
    suspend fun deleteAllDays()

    @Query("DELETE FROM TripEntity")
    suspend fun deleteAllTrips()

    @Query("UPDATE DayEntity SET is_expanded = :expanded WHERE id = :dayId")
    suspend fun setDayExpanded(dayId: String, expanded: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSyncItem(item: SyncQueueEntity)

    @Query("SELECT * FROM SyncQueueEntity WHERE state = 'PENDING' OR state = 'RETRY' ORDER BY created_at_epoch_ms ASC")
    suspend fun selectPendingSyncItems(): List<SyncQueueEntity>

    @Query("DELETE FROM SyncQueueEntity WHERE id = :queueId")
    suspend fun deleteSyncItem(queueId: String)

    @Query(
        """
        UPDATE SyncQueueEntity SET
          state = :state,
          attempt_count = :attemptCount,
          last_error = :lastError,
          updated_at_epoch_ms = :updatedAt
        WHERE id = :queueId
        """
    )
    suspend fun markSyncRetry(
        queueId: String,
        state: String,
        attemptCount: Int,
        lastError: String,
        updatedAt: Long,
    )

    @Query(
        """
        UPDATE TripEntity SET
          is_pending_sync = :isPendingSync,
          updated_at_epoch_ms = :updatedAt
        WHERE id = :tripId
        """
    )
    suspend fun markTripSynced(
        tripId: String,
        isPendingSync: Boolean,
        updatedAt: Long,
    )
}
