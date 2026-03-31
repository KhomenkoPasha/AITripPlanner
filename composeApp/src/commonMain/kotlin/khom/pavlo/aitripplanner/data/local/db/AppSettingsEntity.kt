package khom.pavlo.aitripplanner.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AppSettingsEntity")
data class AppSettingsEntity(
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(name = "app_language") val appLanguage: String,
)
