package khom.pavlo.aitripplanner.domain.repository

import khom.pavlo.aitripplanner.domain.model.PlacePhoto
import kotlinx.coroutines.flow.Flow

interface PlacePhotoRepository {
    fun observePlacePhotos(placeId: String): Flow<List<PlacePhoto>>
    suspend fun addPhoto(tripId: String, placeId: String, sourceUri: String): PlacePhoto
    suspend fun deletePhoto(photoId: String)
    suspend fun deletePhotosByPlace(placeId: String)
    suspend fun deletePhotosByTrip(tripId: String)
}
