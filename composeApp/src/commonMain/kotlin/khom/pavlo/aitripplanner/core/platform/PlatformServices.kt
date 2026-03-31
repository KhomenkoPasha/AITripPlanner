package khom.pavlo.aitripplanner.core.platform

import androidx.room.RoomDatabase
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase

expect fun getDatabaseBuilder(): RoomDatabase.Builder<TravelPlannerDatabase>

expect object PlatformTime {
    fun nowMillis(): Long
}

expect object PlatformMapLauncher {
    fun showOnMap(label: String, latitude: Double, longitude: Double): Boolean
    fun openInMaps(label: String, latitude: Double, longitude: Double): Boolean
}
