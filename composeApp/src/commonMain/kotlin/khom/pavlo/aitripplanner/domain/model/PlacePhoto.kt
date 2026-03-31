package khom.pavlo.aitripplanner.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class PlacePhoto(
    val id: String,
    val tripId: String,
    val placeId: String,
    val localUri: String,
    val createdAtEpochMillis: Long,
)
