package khom.pavlo.aitripplanner.core.platform

import android.net.Uri
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import java.io.File

private const val PHOTO_DIRECTORY = "place_photos"

actual class PlatformPhotoStorageService {
    private val appContext = AndroidPlatformRuntime.appContext

    actual fun importPhoto(
        sourceUri: String,
        tripId: String,
        placeId: String,
        photoId: String,
    ): String {
        val uri = Uri.parse(sourceUri)
        val extension = resolveExtension(uri)
        val directory = File(appContext.filesDir, "$PHOTO_DIRECTORY${File.separator}$tripId${File.separator}$placeId")
        if (!directory.exists() && !directory.mkdirs()) {
            error("Unable to create local photo directory")
        }
        val targetFile = File(directory, "$photoId.$extension")

        appContext.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to open selected photo")

        return Uri.fromFile(targetFile).toString()
    }

    actual fun deletePhoto(localUri: String) {
        val file = localUri.toLocalFile() ?: return
        if (file.exists() && !file.delete()) {
            error("Unable to delete local photo file")
        }
    }

    private fun resolveExtension(uri: Uri): String {
        appContext.contentResolver.getType(uri)?.let { mimeType ->
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)?.let { extension ->
                if (extension.isNotBlank()) return extension
            }
        }

        val fileName = appContext.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
            ?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
            }
        val suffix = fileName?.substringAfterLast('.', "")?.lowercase()
        return suffix?.takeIf { it.isNotBlank() } ?: "jpg"
    }
}

private fun String.toLocalFile(): File? {
    val uri = Uri.parse(this)
    return when {
        uri.scheme == "file" -> uri.path?.let(::File)
        uri.scheme.isNullOrBlank() -> File(this)
        else -> null
    }
}
