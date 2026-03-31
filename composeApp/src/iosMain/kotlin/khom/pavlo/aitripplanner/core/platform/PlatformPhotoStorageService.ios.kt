@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package khom.pavlo.aitripplanner.core.platform

import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import platform.Foundation.NSApplicationSupportDirectory
import platform.Foundation.create

private const val PHOTO_DIRECTORY = "place_photos"

actual class PlatformPhotoStorageService {
    private val fileManager = NSFileManager.defaultManager

    actual fun importPhoto(
        sourceUri: String,
        tripId: String,
        placeId: String,
        photoId: String,
    ): String {
        val sourceUrl = NSURL.URLWithString(sourceUri) ?: error("Invalid source photo uri")
        val baseDirectory = fileManager.URLForDirectory(
            directory = NSApplicationSupportDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = true,
            error = null,
        ) ?: error("Unable to resolve app storage directory")

        val targetDirectory = baseDirectory
            .URLByAppendingPathComponent(PHOTO_DIRECTORY)
            ?.URLByAppendingPathComponent(tripId)
            ?.URLByAppendingPathComponent(placeId)
            ?: error("Unable to build photo directory")

        if (!fileManager.fileExistsAtPath(targetDirectory.path!!)) {
            fileManager.createDirectoryAtURL(
                url = targetDirectory,
                withIntermediateDirectories = true,
                attributes = null,
                error = null,
            )
        }

        val fileExtension = sourceUrl.pathExtension?.takeIf { it.isNotBlank() } ?: "jpg"
        val targetUrl = targetDirectory.URLByAppendingPathComponent("$photoId.$fileExtension")
            ?: error("Unable to build target photo uri")

        if (fileManager.fileExistsAtPath(targetUrl.path!!)) {
            fileManager.removeItemAtURL(targetUrl, error = null)
        }
        if (!fileManager.copyItemAtURL(sourceUrl, targetUrl, error = null)) {
            error("Unable to copy selected photo")
        }

        return targetUrl.absoluteString ?: error("Unable to resolve local photo uri")
    }

    actual fun deletePhoto(localUri: String) {
        val url = NSURL.URLWithString(localUri) ?: NSURL.fileURLWithPath(localUri)
        val path = url.path ?: return
        if (fileManager.fileExistsAtPath(path) && !fileManager.removeItemAtURL(url, error = null)) {
            error("Unable to delete local photo file")
        }
    }
}
