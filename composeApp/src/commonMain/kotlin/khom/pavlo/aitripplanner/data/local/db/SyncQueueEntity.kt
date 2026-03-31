package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "SyncQueueEntity",
    indices = [Index("entity_id")],
)
data class SyncQueueEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "entity_id") val entityId: String,
    @ColumnInfo(name = "entity_type") val entityType: String,
    val operation: String,
    @ColumnInfo(name = "payload_json") val payloadJson: String,
    val state: String,
    @ColumnInfo(name = "attempt_count") val attemptCount: Int,
    @ColumnInfo(name = "base_version") val baseVersion: Long?,
    @ColumnInfo(name = "conflict_token") val conflictToken: String?,
    @ColumnInfo(name = "last_error") val lastError: String?,
    @ColumnInfo(name = "created_at_epoch_ms") val createdAtEpochMillis: Long,
    @ColumnInfo(name = "updated_at_epoch_ms") val updatedAtEpochMillis: Long,
)
