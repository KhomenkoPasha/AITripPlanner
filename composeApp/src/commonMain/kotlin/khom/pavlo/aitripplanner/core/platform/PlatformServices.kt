package khom.pavlo.aitripplanner.core.platform

import app.cash.sqldelight.db.SqlDriver

expect class SqlDriverFactory {
    fun createDriver(): SqlDriver
}

expect object PlatformTime {
    fun nowMillis(): Long
}

expect object PlatformMapLauncher {
    fun showOnMap(label: String, latitude: Double, longitude: Double): Boolean
    fun openInMaps(label: String, latitude: Double, longitude: Double): Boolean
}
