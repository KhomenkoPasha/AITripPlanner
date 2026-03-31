@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package khom.pavlo.aitripplanner.core.platform

import androidx.room.Room
import androidx.room.RoomDatabase
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIApplication
import platform.posix.time

private const val DATABASE_NAME = "travel_planner.db"

actual fun getDatabaseBuilder(): RoomDatabase.Builder<TravelPlannerDatabase> {
    val fileManager = NSFileManager.defaultManager
    val dbUrl = fileManager.URLForDirectory(
        directory = NSDocumentDirectory,
        inDomain = NSUserDomainMask,
        appropriateForURL = null,
        create = false,
        error = null,
    )?.URLByAppendingPathComponent(DATABASE_NAME)
        ?: error("Unable to resolve database path")

    return Room.databaseBuilder<TravelPlannerDatabase>(
        name = dbUrl.path ?: DATABASE_NAME,
    )
}

actual object PlatformTime {
    actual fun nowMillis(): Long = time(null) * 1000L
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
