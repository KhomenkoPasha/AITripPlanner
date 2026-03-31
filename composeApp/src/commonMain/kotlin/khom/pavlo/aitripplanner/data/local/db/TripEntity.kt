package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TripEntity")
data class TripEntity(
    @PrimaryKey val id: String,
    val city: String,
    val title: String,
    val summary: String,
    @ColumnInfo(name = "hero_note") val heroNote: String,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double,
    @ColumnInfo(name = "is_favorite") val isFavorite: Boolean,
    @ColumnInfo(name = "is_offline_only") val isOfflineOnly: Boolean,
    @ColumnInfo(name = "is_pending_sync") val isPendingSync: Boolean,
    @ColumnInfo(name = "remote_version") val remoteVersion: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMillis: Long,
)
