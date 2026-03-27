package khom.pavlo.aitripplanner.core.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import platform.Foundation.NSDate

private const val DATABASE_NAME = "travel_planner.db"

actual class SqlDriverFactory {
    actual fun createDriver(): SqlDriver = NativeSqliteDriver(
        schema = TravelPlannerDatabase.Schema,
        name = DATABASE_NAME,
    )
}

actual object PlatformTime {
    actual fun nowMillis(): Long = (NSDate().timeIntervalSince1970 * 1000.0).toLong()
}
