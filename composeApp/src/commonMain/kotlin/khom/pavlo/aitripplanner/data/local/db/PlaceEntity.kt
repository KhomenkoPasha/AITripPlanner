package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "PlaceEntity",
    indices = [Index("day_id")],
)
data class PlaceEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "day_id") val dayId: String,
    @ColumnInfo(name = "sort_index") val sortIndex: Int,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String,
    @ColumnInfo(name = "visit_minutes") val visitMinutes: Int,
    val note: String,
    val category: String?,
    @ColumnInfo(name = "short_description") val shortDescription: String,
    @ColumnInfo(name = "full_description") val fullDescription: String,
    @ColumnInfo(name = "why_included") val whyIncluded: String,
    @ColumnInfo(name = "tips_text") val tipsText: String,
    @ColumnInfo(name = "opening_hours_text") val openingHoursText: String,
    @ColumnInfo(name = "best_time_to_visit") val bestTimeToVisit: String,
    @ColumnInfo(name = "is_open_now") val isOpenNow: Boolean?,
    @ColumnInfo(name = "website_url") val websiteUrl: String?,
    @ColumnInfo(name = "photo_url") val photoUrl: String?,
    @ColumnInfo(name = "photo_urls_text") val photoUrlsText: String,
    @ColumnInfo(name = "photo_attribution") val photoAttribution: String?,
    @ColumnInfo(name = "price_level") val priceLevel: String?,
    @ColumnInfo(name = "visit_notes") val visitNotes: String,
    val neighborhood: String,
    @ColumnInfo(name = "stop_index") val stopIndex: Int?,
    @ColumnInfo(name = "previous_place_name") val previousPlaceName: String?,
    @ColumnInfo(name = "next_place_name") val nextPlaceName: String?,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean,
)
