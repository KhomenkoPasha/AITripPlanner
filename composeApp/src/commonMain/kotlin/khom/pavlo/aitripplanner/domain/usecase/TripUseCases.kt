package khom.pavlo.aitripplanner.domain.usecase

import kotlinx.coroutines.flow.Flow
import khom.pavlo.aitripplanner.domain.model.AppSyncState
import khom.pavlo.aitripplanner.domain.model.SyncTrigger
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.repository.TripRepository

class ObserveTripsUseCase(
    private val repository: TripRepository,
) {
    operator fun invoke(): Flow<List<Trip>> = repository.observeTrips()
}

class ObserveTripDetailsUseCase(
    private val repository: TripRepository,
) {
    operator fun invoke(tripId: String): Flow<Trip?> = repository.observeTrip(tripId)
}

class ObserveSyncStateUseCase(
    private val repository: TripRepository,
) {
    operator fun invoke(): Flow<AppSyncState> = repository.observeSyncState()
}

class GenerateTripPlanUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke(prompt: String): Trip = repository.generateTrip(prompt)
}

class CreateTripUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke(input: TripEditorInput): Trip = repository.createTrip(input)
}

class UpdateTripUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke(tripId: String, input: TripEditorInput): Trip = repository.updateTrip(tripId, input)
}

class DeleteTripUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke(tripId: String) = repository.deleteTrip(tripId)
}

class EnsureSeedDataUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke() = repository.ensureSeedData()
}

class SetDayExpandedUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke(dayId: String, expanded: Boolean) {
        repository.setDayExpanded(dayId, expanded)
    }
}

class RequestSyncUseCase(
    private val repository: TripRepository,
) {
    suspend operator fun invoke(trigger: SyncTrigger) {
        repository.requestSync(trigger)
    }
}
