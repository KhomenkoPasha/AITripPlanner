package khom.pavlo.aitripplanner.core.platform

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import platform.Foundation.NSDate
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

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

actual object PlatformMapLauncher {
    actual fun showOnMap(label: String, latitude: Double, longitude: Double): Boolean {
        val url = NSURL.URLWithString("http://maps.apple.com/?ll=$latitude,$longitude") ?: return false
        return UIApplication.sharedApplication.canOpenURL(url) && UIApplication.sharedApplication.openURL(url)
    }

    actual fun openInMaps(label: String, latitude: Double, longitude: Double): Boolean {
        val url = NSURL.URLWithString("http://maps.apple.com/?daddr=$latitude,$longitude") ?: return false
        return UIApplication.sharedApplication.canOpenURL(url) && UIApplication.sharedApplication.openURL(url)
    }
}
