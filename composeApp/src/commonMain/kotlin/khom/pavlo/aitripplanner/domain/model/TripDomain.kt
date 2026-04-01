package khom.pavlo.aitripplanner.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class PlaceRemotePhoto(
    val ref: String,
    val attribution: String? = null,
)

@Immutable
@Serializable
data class Trip(
    val id: String,
    val city: String,
    val title: String,
    val summary: String,
    val heroNote: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val isFavorite: Boolean,
    val isOfflineOnly: Boolean,
    val isPendingSync: Boolean,
    val remoteVersion: Long,
    val updatedAtEpochMillis: Long,
    val days: List<TripDay>,
)

@Immutable
@Serializable
data class TripDay(
    val id: String,
    val tripId: String,
    val dayIndex: Int,
    val title: String,
    val summary: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val isExpanded: Boolean,
    val places: List<TripPlace>,
)

@Immutable
@Serializable
data class TripPlace(
    val id: String,
    val dayId: String,
    val sortIndex: Int,
    val name: String,
    val latitude: Double?,
    val longitude: Double?,
    val address: String,
    val visitMinutes: Int,
    val note: String,
    val category: String? = null,
    val shortDescription: String = "",
    val fullDescription: String = "",
    val whyIncluded: String = "",
    val tips: List<String> = emptyList(),
    val openingHoursText: String = "",
    val bestTimeToVisit: String = "",
    val isOpenNow: Boolean? = null,
    val websiteUrl: String? = null,
    val photos: List<PlaceRemotePhoto> = emptyList(),
    val priceLevel: String? = null,
    val visitNotes: String = "",
    val neighborhood: String = "",
    val stopIndex: Int? = null,
    val previousPlaceName: String? = null,
    val nextPlaceName: String? = null,
    val isCompleted: Boolean,
)

enum class SyncOperationType {
    UPSERT_TRIP,
    DELETE_TRIP,
}

enum class SyncQueueState {
    PENDING,
    RETRY,
}

enum class SyncTrigger {
    USER,
    APP_FOREGROUND,
    BACKGROUND,
}

data class SyncQueueItem(
    val id: String,
    val entityId: String,
    val entityType: String,
    val operation: SyncOperationType,
    val payloadJson: String,
    val state: SyncQueueState,
    val attemptCount: Int,
    val baseVersion: Long?,
    val conflictToken: String?,
    val lastError: String?,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

data class AppSyncState(
    val isRunning: Boolean = false,
    val lastTrigger: SyncTrigger? = null,
    val queuedItems: Int = 0,
    val lastError: String? = null,
    val lastCompletedAtEpochMillis: Long? = null,
)
