package khom.pavlo.aitripplanner.domain.usecase

import khom.pavlo.aitripplanner.domain.model.PlacePhoto
import khom.pavlo.aitripplanner.domain.repository.PlacePhotoRepository
import kotlinx.coroutines.flow.Flow

class ObservePlacePhotosUseCase(
    private val repository: PlacePhotoRepository,
) {
    operator fun invoke(placeId: String): Flow<List<PlacePhoto>> = repository.observePlacePhotos(placeId)
}

class AddPlacePhotoUseCase(
    private val repository: PlacePhotoRepository,
) {
    suspend operator fun invoke(tripId: String, placeId: String, sourceUri: String): PlacePhoto =
        repository.addPhoto(tripId, placeId, sourceUri)
}

class DeletePlacePhotoUseCase(
    private val repository: PlacePhotoRepository,
) {
    suspend operator fun invoke(photoId: String) {
        repository.deletePhoto(photoId)
    }
}
