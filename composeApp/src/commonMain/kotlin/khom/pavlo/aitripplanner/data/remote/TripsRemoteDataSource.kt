package khom.pavlo.aitripplanner.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.domain.model.SyncQueueItem
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripDay
import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.model.TripPlace
import kotlinx.serialization.Serializable
import kotlin.random.Random

data class TravelPlannerConfig(
    val baseUrl: String = "https://example.com",
)

interface TripsRemoteDataSource {
    suspend fun fetchTrips(): List<Trip>
    suspend fun pushPendingOperation(item: SyncQueueItem): Result<Unit>
}

class KtorTripsRemoteDataSource(
    private val client: HttpClient,
    private val config: TravelPlannerConfig,
) : TripsRemoteDataSource {
    override suspend fun fetchTrips(): List<Trip> = runCatching {
        client.get("${config.baseUrl}/v1/trips").body<List<RemoteTripDto>>().map { it.toDomain() }
    }.getOrElse {
        SampleTripFactory.seedTrips()
    }

    override suspend fun pushPendingOperation(item: SyncQueueItem): Result<Unit> = runCatching {
        client.post("${config.baseUrl}/v1/sync/trips") {
            contentType(ContentType.Application.Json)
            setBody(RemoteSyncRequest(queue = item))
        }
    }.map { Unit }
}

@Serializable
data class RemoteTripDto(
    val id: String,
    val city: String,
    val title: String,
    val summary: String,
    val heroNote: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val isFavorite: Boolean = false,
    val isOfflineOnly: Boolean = false,
    val isPendingSync: Boolean = false,
    val remoteVersion: Long = 1,
    val updatedAtEpochMillis: Long,
    val days: List<RemoteTripDayDto>,
)

@Serializable
data class RemoteTripDayDto(
    val id: String,
    val dayIndex: Int,
    val title: String,
    val summary: String,
    val durationMinutes: Int,
    val distanceKm: Double,
    val places: List<RemoteTripPlaceDto>,
)

@Serializable
data class RemoteTripPlaceDto(
    val id: String,
    val sortIndex: Int,
    val name: String,
    val address: String,
    val visitMinutes: Int,
)

@Serializable
data class RemoteSyncRequest(
    val queueId: String,
    val entityId: String,
    val operation: String,
    val payloadJson: String,
    val baseVersion: Long?,
    val conflictToken: String?,
) {
    constructor(queue: SyncQueueItem) : this(
        queueId = queue.id,
        entityId = queue.entityId,
        operation = queue.operation.name,
        payloadJson = queue.payloadJson,
        baseVersion = queue.baseVersion,
        conflictToken = queue.conflictToken,
    )
}

object SampleTripFactory {
    fun seedTrips(): List<Trip> = listOf(
        createTrip(
            input = TripEditorInput(
                city = "Rome",
                title = "Rome city highlights",
                summary = "A calm 3-day route shaped around iconic sites, easy walks, and golden-hour breaks.",
                heroNote = "Classic monuments, soft neighborhood pacing, and enough room to slow down.",
            ),
            favorite = true,
        ),
        createTrip(
            input = TripEditorInput(
                city = "Kyoto",
                title = "Quiet temples and gardens",
                summary = "Temple mornings, tea houses, and a compact walking route.",
                heroNote = "A soft, reflective city break with low-friction transitions.",
            ),
        ),
        createTrip(
            input = TripEditorInput(
                city = "Lisbon",
                title = "Coastal neighborhoods",
                summary = "Viewpoints, trams, tiled streets, and sunset pacing.",
                heroNote = "A bright route mixing landmarks and softer local rhythm.",
            ),
        ),
    )

    fun createFromInput(
        input: TripEditorInput,
        pendingSync: Boolean = true,
        offlineOnly: Boolean = true,
    ): Trip = createTrip(
        input = input,
        pendingSync = pendingSync,
        offlineOnly = offlineOnly,
    )

    fun updateExisting(existing: Trip, input: TripEditorInput): Trip = existing.copy(
        city = input.city,
        title = input.title,
        summary = input.summary,
        heroNote = input.heroNote,
        isOfflineOnly = true,
        isPendingSync = true,
        updatedAtEpochMillis = PlatformTime.nowMillis(),
    )

    fun fromPrompt(prompt: String): Trip {
        val city = prompt.split(" ").firstOrNull()?.replaceFirstChar { it.uppercase() }?.takeIf { it.length > 2 } ?: "Rome"
        return createFromInput(
            TripEditorInput(
                city = city,
                title = "$city calm route",
                summary = prompt,
                heroNote = "Designed for soft pacing, long walks, and a premium mobile planning flow.",
            ),
        )
    }

    private fun createTrip(
        input: TripEditorInput,
        favorite: Boolean = false,
        pendingSync: Boolean = false,
        offlineOnly: Boolean = false,
    ): Trip {
        val now = PlatformTime.nowMillis()
        val city = input.city.ifBlank { "Rome" }
        val tripId = "${city.lowercase()}-${Random.nextInt(1000, 9999)}"
        val day1Id = "$tripId-day-1"
        val day2Id = "$tripId-day-2"
        return Trip(
            id = tripId,
            city = city,
            title = input.title.ifBlank { "$city calm route" },
            summary = input.summary,
            heroNote = input.heroNote,
            durationMinutes = 940,
            distanceKm = 12.8,
            isFavorite = favorite,
            isOfflineOnly = offlineOnly,
            isPendingSync = pendingSync,
            remoteVersion = 0,
            updatedAtEpochMillis = now,
            days = listOf(
                TripDay(
                    id = day1Id,
                    tripId = tripId,
                    dayIndex = 1,
                    title = "Landmarks and slow afternoon",
                    summary = "A flagship route with iconic stops and enough time between locations.",
                    durationMinutes = 310,
                    distanceKm = 4.2,
                    isExpanded = true,
                    places = listOf(
                        TripPlace("$day1Id-place-1", day1Id, 0, if (city == "Rome") "Colosseum" else "$city Old Town", "Historic center", 90),
                        TripPlace("$day1Id-place-2", day1Id, 1, if (city == "Rome") "Roman Forum" else "$city Riverside Walk", "Central district", 75),
                        TripPlace("$day1Id-place-3", day1Id, 2, if (city == "Rome") "Pantheon" else "$city Museum Quarter", "Museum area", 45),
                    ),
                ),
                TripDay(
                    id = day2Id,
                    tripId = tripId,
                    dayIndex = 2,
                    title = "Neighborhood details and evening finish",
                    summary = "Less dense routing with cafes, views, and a sunset endpoint.",
                    durationMinutes = 280,
                    distanceKm = 3.6,
                    isExpanded = false,
                    places = listOf(
                        TripPlace("$day2Id-place-1", day2Id, 0, if (city == "Rome") "Trevi Fountain" else "$city Viewpoint", "Scenic zone", 35),
                        TripPlace("$day2Id-place-2", day2Id, 1, if (city == "Rome") "Piazza Navona" else "$city Main Square", "Old center", 60),
                        TripPlace("$day2Id-place-3", day2Id, 2, if (city == "Rome") "Trastevere" else "$city Design District", "Creative quarter", 80),
                    ),
                ),
            ),
        )
    }
}

private fun RemoteTripDto.toDomain(): Trip = Trip(
    id = id,
    city = city,
    title = title,
    summary = summary,
    heroNote = heroNote,
    durationMinutes = durationMinutes,
    distanceKm = distanceKm,
    isFavorite = isFavorite,
    isOfflineOnly = isOfflineOnly,
    isPendingSync = isPendingSync,
    remoteVersion = remoteVersion,
    updatedAtEpochMillis = updatedAtEpochMillis,
    days = days.map { day ->
        TripDay(
            id = day.id,
            tripId = id,
            dayIndex = day.dayIndex,
            title = day.title,
            summary = day.summary,
            durationMinutes = day.durationMinutes,
            distanceKm = day.distanceKm,
            isExpanded = day.dayIndex == 1,
            places = day.places.map { place ->
                TripPlace(
                    id = place.id,
                    dayId = day.id,
                    sortIndex = place.sortIndex,
                    name = place.name,
                    address = place.address,
                    visitMinutes = place.visitMinutes,
                )
            },
        )
    },
)
