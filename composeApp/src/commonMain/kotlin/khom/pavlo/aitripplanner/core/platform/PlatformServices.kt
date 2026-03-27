package khom.pavlo.aitripplanner.core.platform

import app.cash.sqldelight.db.SqlDriver

expect class SqlDriverFactory {
    fun createDriver(): SqlDriver
}

expect object PlatformTime {
    fun nowMillis(): Long
}
