package khom.pavlo.aitripplanner.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PlacePhotoDao {
    @Query("SELECT * FROM PlacePhotoEntity WHERE place_id = :placeId ORDER BY created_at_epoch_ms DESC")
    fun observePlacePhotos(placeId: String): Flow<List<PlacePhotoEntity>>

    @Query("SELECT * FROM PlacePhotoEntity WHERE id = :photoId")
    suspend fun selectPhotoById(photoId: String): PlacePhotoEntity?

    @Query("SELECT * FROM PlacePhotoEntity WHERE place_id = :placeId ORDER BY created_at_epoch_ms DESC")
    suspend fun selectPhotosByPlaceId(placeId: String): List<PlacePhotoEntity>

    @Query("SELECT * FROM PlacePhotoEntity WHERE trip_id = :tripId ORDER BY created_at_epoch_ms DESC")
    suspend fun selectPhotosByTripId(tripId: String): List<PlacePhotoEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: PlacePhotoEntity)

    @Query("DELETE FROM PlacePhotoEntity WHERE id = :photoId")
    suspend fun deletePhotoById(photoId: String)

    @Query("DELETE FROM PlacePhotoEntity WHERE place_id = :placeId")
    suspend fun deletePhotosByPlaceId(placeId: String)

    @Query("DELETE FROM PlacePhotoEntity WHERE trip_id = :tripId")
    suspend fun deletePhotosByTripId(tripId: String)
}
