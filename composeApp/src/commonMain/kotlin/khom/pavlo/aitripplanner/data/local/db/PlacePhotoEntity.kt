package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "PlacePhotoEntity",
    indices = [Index("trip_id"), Index("place_id")],
)
data class PlacePhotoEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    @ColumnInfo(name = "place_id") val placeId: String,
    @ColumnInfo(name = "local_uri") val localUri: String,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMillis: Long,
)
