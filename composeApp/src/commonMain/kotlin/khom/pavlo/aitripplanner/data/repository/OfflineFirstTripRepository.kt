package khom.pavlo.aitripplanner.data.repository

import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.data.local.TripLocalDataSource
import khom.pavlo.aitripplanner.data.remote.TripsRemoteDataSource
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripDay
import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.model.TripPlace
import khom.pavlo.aitripplanner.domain.repository.TripRepository
import khom.pavlo.aitripplanner.sync.SyncEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onStart
import kotlin.math.roundToInt

class OfflineFirstTripRepository(
    private val localDataSource: TripLocalDataSource,
    private val remoteDataSource: TripsRemoteDataSource,
    private val syncEngine: SyncEngine,
) : TripRepository {
    override fun observeTrips(): Flow<List<Trip>> = localDataSource.observeTrips()

    override fun observeTrip(tripId: String): Flow<Trip?> = localDataSource.observeTrip(tripId)

    override fun observeSyncState() = syncEngine.state.onStart {
        emit(syncEngine.state.value)
    }

    override suspend fun removeMockData() {
        localDataSource.removeLegacyMockTrips()
    }

    override suspend fun createTrip(input: TripEditorInput): Trip = generateAndStore(input)

    override suspend fun updateTrip(tripId: String, input: TripEditorInput): Trip {
        val existing = observeTrip(tripId).first() ?: error("Trip not found")
        return generateAndStore(input, existing)
    }

    override suspend fun deleteTrip(tripId: String) {
        localDataSource.deleteTrip(tripId)
    }

    override suspend fun removePlace(placeId: String) {
        val trip = observeTrips().first().firstOrNull { candidate ->
            candidate.days.any { day -> day.places.any { place -> place.id == placeId } }
        } ?: error("Trip containing place not found")

        val updated = trip.removePlace(placeId) ?: return
        localDataSource.upsertTrip(updated)
    }

    override suspend fun setPlaceCompleted(placeId: String, completed: Boolean) {
        val trip = observeTrips().first().firstOrNull { candidate ->
            candidate.days.any { day -> day.places.any { place -> place.id == placeId } }
        } ?: error("Trip containing place not found")

        val updated = trip.setPlaceCompleted(placeId, completed) ?: return
        localDataSource.upsertTrip(updated)
    }

    override suspend fun setDayExpanded(dayId: String, expanded: Boolean) {
        localDataSource.setDayExpanded(dayId, expanded)
    }

    override suspend fun requestSync(trigger: SyncTrigger) {
        syncEngine.requestSync(trigger)
    }

    private suspend fun generateAndStore(
        input: TripEditorInput,
        existingTrip: Trip? = null,
    ): Trip {
        val trip = remoteDataSource.generateTrip(input, existingTrip)
        localDataSource.upsertTrip(trip)
        return trip
    }
}

private fun Trip.removePlace(placeId: String): Trip? {
    var changed = false
    val updatedDays = days.map { day ->
        val updatedDay = day.removePlace(placeId)
        if (updatedDay != day) changed = true
        updatedDay
    }

    if (!changed) return null

    return copy(
        durationMinutes = updatedDays.sumOf { it.durationMinutes },
        distanceKm = updatedDays.sumOf { it.distanceKm },
        isPendingSync = true,
        updatedAtEpochMillis = PlatformTime.nowMillis(),
        days = updatedDays,
    )
}

private fun Trip.setPlaceCompleted(placeId: String, completed: Boolean): Trip? {
    var changed = false
    val updatedDays = days.map { day ->
        val updatedDay = day.setPlaceCompleted(placeId, completed)
        if (updatedDay != day) changed = true
        updatedDay
    }

    if (!changed) return null

    return copy(
        isPendingSync = true,
        updatedAtEpochMillis = PlatformTime.nowMillis(),
        days = updatedDays,
    )
}

private fun TripDay.removePlace(placeId: String): TripDay {
    if (places.none { it.id == placeId }) return this

    val remainingPlaces = places
        .filterNot { it.id == placeId }
        .mapIndexed { index, place -> place.copy(sortIndex = index) }

    val originalCount = places.size.coerceAtLeast(1)
    val remainingCount = remainingPlaces.size
    val visitMinutes = remainingPlaces.sumOf(TripPlace::visitMinutes)
    val scaledDuration = if (remainingCount == 0) {
        0
    } else {
        (durationMinutes * (remainingCount.toDouble() / originalCount.toDouble())).roundToInt()
    }
    val scaledDistance = if (remainingCount < 2) {
        0.0
    } else {
        distanceKm * (remainingCount.toDouble() / originalCount.toDouble())
    }

    return copy(
        summary = if (remainingPlaces.isEmpty()) "" else summary,
        durationMinutes = maxOf(visitMinutes, scaledDuration),
        distanceKm = scaledDistance,
        places = remainingPlaces,
    )
}

private fun TripDay.setPlaceCompleted(placeId: String, completed: Boolean): TripDay {
    var changed = false
    val updatedPlaces = places.map { place ->
        if (place.id == placeId && place.isCompleted != completed) {
            changed = true
            place.copy(isCompleted = completed)
        } else {
            place
        }
    }

    if (!changed) return this

    return copy(places = updatedPlaces)
}
