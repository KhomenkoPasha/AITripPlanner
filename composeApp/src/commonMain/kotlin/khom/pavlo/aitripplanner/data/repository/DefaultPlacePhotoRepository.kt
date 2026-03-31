package khom.pavlo.aitripplanner.data.repository

import khom.pavlo.aitripplanner.core.platform.PlatformPhotoStorageService
import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.data.local.PlacePhotoLocalDataSource
import khom.pavlo.aitripplanner.domain.model.PlacePhoto
import khom.pavlo.aitripplanner.domain.repository.PlacePhotoRepository
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

class DefaultPlacePhotoRepository(
    private val localDataSource: PlacePhotoLocalDataSource,
    private val storageService: PlatformPhotoStorageService,
) : PlacePhotoRepository {
    override fun observePlacePhotos(placeId: String): Flow<List<PlacePhoto>> =
        localDataSource.observePlacePhotos(placeId)

    override suspend fun addPhoto(tripId: String, placeId: String, sourceUri: String): PlacePhoto {
        val photoId = "photo-${PlatformTime.nowMillis()}-${Random.nextInt(1000, 9999)}"
        val localUri = storageService.importPhoto(
            sourceUri = sourceUri,
            tripId = tripId,
            placeId = placeId,
            photoId = photoId,
        )
        val photo = PlacePhoto(
            id = photoId,
            tripId = tripId,
            placeId = placeId,
            localUri = localUri,
            createdAtEpochMillis = PlatformTime.nowMillis(),
        )
        runCatching {
            localDataSource.insertPhoto(photo)
        }.onFailure {
            runCatching { storageService.deletePhoto(localUri) }
        }.getOrThrow()
        return photo
    }

    override suspend fun deletePhoto(photoId: String) {
        val photo = localDataSource.getPhoto(photoId) ?: return
        storageService.deletePhoto(photo.localUri)
        localDataSource.deletePhoto(photoId)
    }

    override suspend fun deletePhotosByPlace(placeId: String) {
        val photos = localDataSource.getPhotosByPlace(placeId)
        photos.forEach { photo ->
            storageService.deletePhoto(photo.localUri)
        }
        localDataSource.deletePhotosByPlace(placeId)
    }

    override suspend fun deletePhotosByTrip(tripId: String) {
        val photos = localDataSource.getPhotosByTrip(tripId)
        photos.forEach { photo ->
            storageService.deletePhoto(photo.localUri)
        }
        localDataSource.deletePhotosByTrip(tripId)
    }
}
