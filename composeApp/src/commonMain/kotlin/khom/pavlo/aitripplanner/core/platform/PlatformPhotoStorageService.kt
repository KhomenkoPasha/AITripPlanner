package khom.pavlo.aitripplanner.core.platform

expect class PlatformPhotoStorageService() {
    fun importPhoto(
        sourceUri: String,
        tripId: String,
        placeId: String,
        photoId: String,
    ): String

    fun deletePhoto(localUri: String)
}
