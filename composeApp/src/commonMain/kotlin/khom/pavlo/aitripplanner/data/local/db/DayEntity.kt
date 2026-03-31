package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "DayEntity",
    indices = [Index("trip_id")],
)
data class DayEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "trip_id") val tripId: String,
    @ColumnInfo(name = "day_index") val dayIndex: Int,
    val title: String,
    val summary: String,
    @ColumnInfo(name = "duration_minutes") val durationMinutes: Int,
    @ColumnInfo(name = "distance_km") val distanceKm: Double,
    @ColumnInfo(name = "is_expanded") val isExpanded: Boolean,
)
