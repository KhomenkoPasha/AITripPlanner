package khom.pavlo.aitripplanner.data.local

import khom.pavlo.aitripplanner.data.local.db.PlacePhotoEntity
import khom.pavlo.aitripplanner.data.local.db.TravelPlannerDatabase
import khom.pavlo.aitripplanner.domain.model.PlacePhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlacePhotoLocalDataSource(
    database: TravelPlannerDatabase,
) {
    private val placePhotoDao = database.placePhotoDao()

    fun observePlacePhotos(placeId: String): Flow<List<PlacePhoto>> = placePhotoDao.observePlacePhotos(placeId)
        .map { rows -> rows.map(PlacePhotoEntity::toDomain) }

    suspend fun getPhoto(photoId: String): PlacePhoto? =
        placePhotoDao.selectPhotoById(photoId)?.toDomain()

    suspend fun getPhotosByPlace(placeId: String): List<PlacePhoto> =
        placePhotoDao.selectPhotosByPlaceId(placeId).map(PlacePhotoEntity::toDomain)

    suspend fun getPhotosByTrip(tripId: String): List<PlacePhoto> =
        placePhotoDao.selectPhotosByTripId(tripId).map(PlacePhotoEntity::toDomain)

    suspend fun insertPhoto(photo: PlacePhoto) {
        placePhotoDao.insertPhoto(
            PlacePhotoEntity(
                id = photo.id,
                tripId = photo.tripId,
                placeId = photo.placeId,
                localUri = photo.localUri,
                createdAtEpochMillis = photo.createdAtEpochMillis,
            ),
        )
    }

    suspend fun deletePhoto(photoId: String) {
        placePhotoDao.deletePhotoById(photoId)
    }

    suspend fun deletePhotosByPlace(placeId: String) {
        placePhotoDao.deletePhotosByPlaceId(placeId)
    }

    suspend fun deletePhotosByTrip(tripId: String) {
        placePhotoDao.deletePhotosByTripId(tripId)
    }
}

private fun PlacePhotoEntity.toDomain(): PlacePhoto = PlacePhoto(
    id = id,
    tripId = tripId,
    placeId = placeId,
    localUri = localUri,
    createdAtEpochMillis = createdAtEpochMillis,
)
