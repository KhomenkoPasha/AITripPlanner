package khom.pavlo.aitripplanner.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.isSuccess
import io.ktor.http.ContentType
import io.ktor.http.contentType
import khom.pavlo.aitripplanner.core.platform.PlatformTime
import khom.pavlo.aitripplanner.domain.model.SyncQueueItem
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.PlaceRemotePhoto
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.Trip
import khom.pavlo.aitripplanner.domain.model.TripDay
import khom.pavlo.aitripplanner.domain.model.TripEditorInput
import khom.pavlo.aitripplanner.domain.model.TripPlace
import khom.pavlo.aitripplanner.domain.model.TripPreference
import khom.pavlo.aitripplanner.localBackendBaseUrl
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlin.random.Random

data class TravelPlannerConfig(
    val baseUrl: String = localBackendBaseUrl(),
)

interface TripsRemoteDataSource {
    suspend fun generateTrip(input: TripEditorInput, existingTrip: Trip? = null): Trip
    suspend fun fetchTrips(): List<Trip>
    suspend fun pushPendingOperation(item: SyncQueueItem): Result<Unit>
}

class KtorTripsRemoteDataSource(
    private val client: HttpClient,
    private val config: TravelPlannerConfig,
    private val json: Json,
) : TripsRemoteDataSource {
    override suspend fun generateTrip(input: TripEditorInput, existingTrip: Trip?): Trip {
        val response = client.post("${config.baseUrl}/api/trips/generate") {
            contentType(ContentType.Application.Json)
            setBody(RemoteGenerateTripRequest.fromInput(input))
        }

        if (!response.status.isSuccess()) {
            val errorBody = runCatching { response.body<RemoteErrorResponseDto>() }.getOrNull()
            val message = errorBody?.message ?: "Request failed with HTTP ${response.status.value}"
            throw IllegalStateException(message)
        }

        return response.body<RemoteGeneratedTripDto>().toDomain(
            input = input,
            existingTrip = existingTrip,
        )
    }

    override suspend fun fetchTrips(): List<Trip> {
        val response = client.get("${config.baseUrl}/api/trips")
        ensureTripRequestSuccess(response)
        val payload = response.body<JsonElement>()
        return payload.decodeTrips(json)
    }

    override suspend fun pushPendingOperation(item: SyncQueueItem): Result<Unit> = runCatching {
        when (item.operation) {
            khom.pavlo.aitripplanner.domain.model.SyncOperationType.UPSERT_TRIP -> {
                val payload = json.decodeFromString<RemoteTripSyncUpsertRequest>(item.payloadJson)
                val response = client.put("${config.baseUrl}/api/trips/${payload.trip.id}") {
                    contentType(ContentType.Application.Json)
                    setBody(payload)
                }
                ensureTripRequestSuccess(response)
            }

            khom.pavlo.aitripplanner.domain.model.SyncOperationType.DELETE_TRIP -> {
                val payload = json.decodeFromString<RemoteTripSyncDeletePayload>(item.payloadJson)
                val response = client.delete("${config.baseUrl}/api/trips/${payload.tripId}") {
                    url {
                        payload.baseVersion?.let { version ->
                            parameters.append("baseVersion", version.toString())
                        }
                    }
                }
                ensureTripRequestSuccess(response)
            }
        }
    }
}

@Serializable
data class RemoteTripSyncUpsertRequest(
    val trip: Trip,
    val baseVersion: Long? = null,
)

@Serializable
data class RemoteTripSyncDeletePayload(
    val tripId: String,
    val baseVersion: Long? = null,
)

@Serializable
data class RemoteTripsResponse(
    val trips: List<Trip> = emptyList(),
)

@Serializable
data class RemoteGenerateTripRequest(
    val prompt: String,
    val city: String? = null,
    val days: Int? = null,
    val placeCount: Int? = null,
    val walkingMinutesPerDay: Int? = null,
    val travelMode: String? = null,
    val language: String? = null,
) {
    companion object {
        fun fromInput(input: TripEditorInput): RemoteGenerateTripRequest {
            val normalized = input.normalized()
            return RemoteGenerateTripRequest(
                prompt = normalized.toBackendPrompt(),
                city = normalized.city.ifBlank { null },
                days = normalized.days,
                placeCount = normalized.placeCount,
                walkingMinutesPerDay = normalized.walkingMinutesPerDay,
                travelMode = normalized.travelMode.toApiValue(),
                language = normalized.language.toApiValue(),
            )
        }
    }
}

@Serializable
data class RemoteGeneratedTripDto(
    val city: String,
    val dayCount: Int,
    val summary: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val encodedPolyline: String,
    val days: List<RemoteGeneratedTripDayDto>,
    val warnings: List<String> = emptyList(),
)

@Serializable
data class RemoteGeneratedTripDayDto(
    val dayNumber: Int,
    val title: String,
    val places: List<RemoteGeneratedTripPlaceDto>,
    val route: RemoteGeneratedTripRouteDto,
)

@Serializable
data class RemoteGeneratedTripPlaceDto(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val estimatedVisitMinutes: Int,
    val note: String,
    val category: String? = null,
    val shortDescription: String? = null,
    val fullDescription: String? = null,
    val whyIncluded: String? = null,
    val tips: List<String> = emptyList(),
    val openingHoursText: String? = null,
    val bestTimeToVisit: String? = null,
    val isOpenNow: Boolean? = null,
    val websiteUrl: String? = null,
    val photos: List<RemotePlacePhotoDto> = emptyList(),
    val priceLevel: String? = null,
    val visitNotes: String? = null,
    val neighborhood: String? = null,
    val stopIndex: Int? = null,
    val previousPlaceName: String? = null,
    val nextPlaceName: String? = null,
)

@Serializable
data class RemotePlacePhotoDto(
    val ref: String,
    val attribution: String? = null,
)

@Serializable
data class RemoteGeneratedTripRouteDto(
    val summary: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val encodedPolyline: String,
)

@Serializable
data class RemoteErrorResponseDto(
    val code: String,
    val message: String,
)

private suspend fun ensureTripRequestSuccess(
    response: io.ktor.client.statement.HttpResponse,
) {
    if (response.status.isSuccess()) return

    val errorBody = runCatching { response.body<RemoteErrorResponseDto>() }.getOrNull()
    val message = errorBody?.message ?: "Request failed with HTTP ${response.status.value}"
    throw IllegalStateException(message)
}

private fun JsonElement.decodeTrips(json: Json): List<Trip> = decodeTripsOrNull(json) ?: emptyList()

private fun JsonElement.decodeTripsOrNull(json: Json): List<Trip>? = when (this) {
    is JsonArray -> runCatching { json.decodeFromString<List<Trip>>(toString()) }.getOrNull()
    is JsonObject -> {
        runCatching { json.decodeFromString<RemoteTripsResponse>(toString()).trips }.getOrNull()
            ?: listOf("trips", "items", "data", "result")
                .firstNotNullOfOrNull { key -> this[key]?.decodeTripsOrNull(json) }
            ?: runCatching { listOf(json.decodeFromString<Trip>(toString())) }.getOrNull()
    }

    else -> null
}

private fun RemoteGeneratedTripDto.toDomain(
    input: TripEditorInput,
    existingTrip: Trip?,
): Trip {
    val normalizedInput = input.normalized()
    val now = PlatformTime.nowMillis()
    val tripId = existingTrip?.id ?: "trip-$now-${Random.nextInt(1000, 9999)}"

    return Trip(
        id = tripId,
        city = city,
        title = normalizedInput.title,
        summary = summary,
        heroNote = normalizedInput.heroNote.ifBlank {
            warnings.firstOrNull() ?: existingTrip?.heroNote.orEmpty()
        },
        durationMinutes = durationSeconds.toMinutes(),
        distanceKm = distanceMeters / 1000.0,
        isFavorite = existingTrip?.isFavorite ?: false,
        isOfflineOnly = false,
        isPendingSync = false,
        remoteVersion = existingTrip?.remoteVersion ?: 0,
        updatedAtEpochMillis = now,
        days = days.map { day ->
            val dayId = "$tripId-day-${day.dayNumber}"
            TripDay(
                id = dayId,
                tripId = tripId,
                dayIndex = day.dayNumber,
                title = day.title,
                summary = day.route.summary.ifBlank {
                    day.places.joinToString(separator = ", ") { it.name }
                },
                durationMinutes = day.route.durationSeconds.toMinutes(),
                distanceKm = day.route.distanceMeters / 1000.0,
                isExpanded = day.dayNumber == 1,
                places = day.places.mapIndexed { index, place ->
                    TripPlace(
                        id = place.id,
                        dayId = dayId,
                        sortIndex = index,
                        name = place.name,
                        latitude = place.lat,
                        longitude = place.lng,
                        address = place.address,
                        visitMinutes = place.estimatedVisitMinutes,
                        note = place.note,
                        category = place.category,
                        shortDescription = place.shortDescription.orEmpty(),
                        fullDescription = place.fullDescription.orEmpty(),
                        whyIncluded = place.whyIncluded.orEmpty(),
                        tips = place.tips,
                        openingHoursText = place.openingHoursText.orEmpty(),
                        bestTimeToVisit = place.bestTimeToVisit.orEmpty(),
                        isOpenNow = place.isOpenNow,
                        websiteUrl = place.websiteUrl,
                        photos = place.photos.mapNotNull { photo ->
                            photo.ref
                                .trim()
                                .takeIf(String::isNotBlank)
                                ?.let { ref ->
                                    PlaceRemotePhoto(
                                        ref = ref,
                                        attribution = photo.attribution?.trim()?.ifBlank { null },
                                    )
                                }
                        },
                        priceLevel = place.priceLevel,
                        visitNotes = place.visitNotes.orEmpty(),
                        neighborhood = place.neighborhood.orEmpty(),
                        stopIndex = place.stopIndex,
                        previousPlaceName = place.previousPlaceName,
                        nextPlaceName = place.nextPlaceName,
                        isCompleted = false,
                    )
                },
            )
        },
    )
}

private fun TripEditorInput.normalized() = copy(
    city = city.trim(),
    title = title.trim(),
    prompt = prompt.trim(),
    heroNote = heroNote.trim(),
)

private fun TripEditorInput.toBackendPrompt(): String {
    val quickPreferences = buildQuickPreferences(language)
    return listOfNotNull(
        prompt.takeIf { it.isNotBlank() },
        heroNote.takeIf { it.isNotBlank() },
        quickPreferences.takeIf { it.isNotBlank() },
    ).joinToString(separator = "\n")
}

private fun TripEditorInput.buildQuickPreferences(language: AppLanguage): String {
    val manualText = listOf(prompt, heroNote).joinToString(separator = "\n").lowercase()

    fun includeIfMissing(value: String): Boolean = value.isNotBlank() && !manualText.contains(value.lowercase())

    val interestLabels = selectedInterests
        .map { it.toPromptValue(language) }
        .filter(::includeIfMissing)

    val formatLabels = buildList {
        travelMode.toPromptValue(language)
            .takeIf(::includeIfMissing)
            ?.let(::add)
    }

    val preferenceLabels = selectedPreferences
        .map { it.toPromptValue(language) }
        .filter(::includeIfMissing)

    val paceLabel = selectedPace
        ?.toPromptValue(language)
        ?.takeIf(::includeIfMissing)

    val budgetLabel = selectedBudget
        ?.toPromptValue(language)
        ?.takeIf(::includeIfMissing)

    val companionLabels = buildList {
        if (withChildren) {
            language.withChildrenPromptValue()
                .takeIf(::includeIfMissing)
                ?.let(::add)
        }
        selectedCompanionType
            ?.toPromptValue(language)
            ?.takeIf(::includeIfMissing)
            ?.let(::add)
    }

    val parts = buildList {
        if (interestLabels.isNotEmpty()) {
            add("${language.interestsPromptLabel()}: ${interestLabels.joinToString(", ")}")
        }
        paceLabel?.let { pace ->
            add("${language.pacePromptLabel()}: $pace")
        }
        budgetLabel?.let { budget ->
            add("${language.budgetPromptLabel()}: $budget")
        }
        if (formatLabels.isNotEmpty()) {
            add("${language.travelModePromptLabel()}: ${formatLabels.joinToString(", ")}")
        }
        if (companionLabels.isNotEmpty()) {
            add("${language.formatPromptLabel()}: ${companionLabels.joinToString(", ")}")
        }
        if (preferenceLabels.isNotEmpty()) {
            add("${language.preferencesPromptLabel()}: ${preferenceLabels.joinToString(", ")}")
        }
    }

    return if (parts.isEmpty()) "" else "${language.quickPreferencesPromptLabel()}: ${parts.joinToString("; ")}"
}

private fun Int.toMinutes(): Int = if (this <= 0) 0 else (this + 59) / 60

private fun AppLanguage.toApiValue(): String = when (this) {
    AppLanguage.EN -> "en"
    AppLanguage.RU -> "ru"
    AppLanguage.UK -> "uk"
}

private fun TravelMode.toApiValue(): String = when (this) {
    TravelMode.WALKING -> "walking"
    TravelMode.CAR -> "driving"
    TravelMode.PUBLIC_TRANSPORT -> "transit"
    TravelMode.TAXI -> "taxi"
}

private fun AppLanguage.quickPreferencesPromptLabel(): String = when (this) {
    AppLanguage.EN -> "Quick preferences"
    AppLanguage.RU -> "Быстрые параметры"
    AppLanguage.UK -> "Швидкі параметри"
}

private fun AppLanguage.interestsPromptLabel(): String = when (this) {
    AppLanguage.EN -> "interests"
    AppLanguage.RU -> "интересы"
    AppLanguage.UK -> "інтереси"
}

private fun AppLanguage.pacePromptLabel(): String = when (this) {
    AppLanguage.EN -> "pace"
    AppLanguage.RU -> "темп"
    AppLanguage.UK -> "темп"
}

private fun AppLanguage.budgetPromptLabel(): String = when (this) {
    AppLanguage.EN -> "budget"
    AppLanguage.RU -> "бюджет"
    AppLanguage.UK -> "бюджет"
}

private fun AppLanguage.formatPromptLabel(): String = when (this) {
    AppLanguage.EN -> "trip format"
    AppLanguage.RU -> "формат поездки"
    AppLanguage.UK -> "формат поїздки"
}

private fun AppLanguage.travelModePromptLabel(): String = when (this) {
    AppLanguage.EN -> "travel mode"
    AppLanguage.RU -> "способ передвижения"
    AppLanguage.UK -> "спосіб пересування"
}

private fun AppLanguage.preferencesPromptLabel(): String = when (this) {
    AppLanguage.EN -> "preferences"
    AppLanguage.RU -> "пожелания"
    AppLanguage.UK -> "побажання"
}

private fun AppLanguage.withChildrenPromptValue(): String = when (this) {
    AppLanguage.EN -> "with children"
    AppLanguage.RU -> "с ребенком"
    AppLanguage.UK -> "з дитиною"
}

private fun Interest.toPromptValue(language: AppLanguage): String = when (this) {
    Interest.MUSEUMS -> when (language) {
        AppLanguage.EN -> "museums"
        AppLanguage.RU -> "музеи"
        AppLanguage.UK -> "музеї"
    }
    Interest.HISTORY -> when (language) {
        AppLanguage.EN -> "history"
        AppLanguage.RU -> "история"
        AppLanguage.UK -> "історія"
    }
    Interest.FOOD -> when (language) {
        AppLanguage.EN -> "food"
        AppLanguage.RU -> "еда"
        AppLanguage.UK -> "їжа"
    }
    Interest.CAFE -> when (language) {
        AppLanguage.EN -> "cafes"
        AppLanguage.RU -> "кафе"
        AppLanguage.UK -> "кафе"
    }
    Interest.VIEWS -> when (language) {
        AppLanguage.EN -> "views"
        AppLanguage.RU -> "виды"
        AppLanguage.UK -> "краєвиди"
    }
    Interest.ARCHITECTURE -> when (language) {
        AppLanguage.EN -> "architecture"
        AppLanguage.RU -> "архитектура"
        AppLanguage.UK -> "архітектура"
    }
    Interest.NIGHTLIFE -> when (language) {
        AppLanguage.EN -> "nightlife"
        AppLanguage.RU -> "ночная жизнь"
        AppLanguage.UK -> "нічне життя"
    }
    Interest.NATURE -> when (language) {
        AppLanguage.EN -> "nature"
        AppLanguage.RU -> "природа"
        AppLanguage.UK -> "природа"
    }
}

private fun Pace.toPromptValue(language: AppLanguage): String = when (this) {
    Pace.RELAXED -> when (language) {
        AppLanguage.EN -> "relaxed"
        AppLanguage.RU -> "спокойный"
        AppLanguage.UK -> "спокійний"
    }
    Pace.NORMAL -> when (language) {
        AppLanguage.EN -> "normal"
        AppLanguage.RU -> "нормальный"
        AppLanguage.UK -> "звичайний"
    }
    Pace.INTENSIVE -> when (language) {
        AppLanguage.EN -> "intensive"
        AppLanguage.RU -> "интенсивный"
        AppLanguage.UK -> "інтенсивний"
    }
}

private fun Budget.toPromptValue(language: AppLanguage): String = when (this) {
    Budget.BUDGET -> when (language) {
        AppLanguage.EN -> "budget"
        AppLanguage.RU -> "экономно"
        AppLanguage.UK -> "економно"
    }
    Budget.MEDIUM -> when (language) {
        AppLanguage.EN -> "medium"
        AppLanguage.RU -> "средний"
        AppLanguage.UK -> "середній"
    }
    Budget.PREMIUM -> when (language) {
        AppLanguage.EN -> "premium"
        AppLanguage.RU -> "премиум"
        AppLanguage.UK -> "преміум"
    }
}

private fun CompanionType.toPromptValue(language: AppLanguage): String = when (this) {
    CompanionType.COUPLE -> when (language) {
        AppLanguage.EN -> "for couple"
        AppLanguage.RU -> "для пары"
        AppLanguage.UK -> "для пари"
    }
    CompanionType.SOLO -> when (language) {
        AppLanguage.EN -> "solo"
        AppLanguage.RU -> "соло"
        AppLanguage.UK -> "соло"
    }
}

private fun TripPreference.toPromptValue(language: AppLanguage): String = when (this) {
    TripPreference.MUST_SEE -> when (language) {
        AppLanguage.EN -> "must-see"
        AppLanguage.RU -> "топ места"
        AppLanguage.UK -> "топ місця"
    }
    TripPreference.LOCAL_SPOTS -> when (language) {
        AppLanguage.EN -> "local spots"
        AppLanguage.RU -> "локальные места"
        AppLanguage.UK -> "локальні місця"
    }
    TripPreference.HIDDEN_GEMS -> when (language) {
        AppLanguage.EN -> "hidden gems"
        AppLanguage.RU -> "скрытые места"
        AppLanguage.UK -> "приховані місця"
    }
    TripPreference.NO_CROWDS -> when (language) {
        AppLanguage.EN -> "no crowds"
        AppLanguage.RU -> "без толпы"
        AppLanguage.UK -> "без натовпу"
    }
    TripPreference.NO_RUSH -> when (language) {
        AppLanguage.EN -> "no rush"
        AppLanguage.RU -> "без спешки"
        AppLanguage.UK -> "без поспіху"
    }
    TripPreference.SUNSET -> when (language) {
        AppLanguage.EN -> "sunset"
        AppLanguage.RU -> "на закате"
        AppLanguage.UK -> "на заході сонця"
    }
    TripPreference.RAINY_DAY -> when (language) {
        AppLanguage.EN -> "rainy day"
        AppLanguage.RU -> "дождливый день"
        AppLanguage.UK -> "дощовий день"
    }
    TripPreference.SHORT_ROUTE -> when (language) {
        AppLanguage.EN -> "short route"
        AppLanguage.RU -> "короткий маршрут"
        AppLanguage.UK -> "короткий маршрут"
    }
    TripPreference.FREE_PLACES -> when (language) {
        AppLanguage.EN -> "free places"
        AppLanguage.RU -> "бесплатные места"
        AppLanguage.UK -> "безкоштовні місця"
    }
    TripPreference.INSTAGRAM_SPOTS -> when (language) {
        AppLanguage.EN -> "instagram spots"
        AppLanguage.RU -> "instagram места"
        AppLanguage.UK -> "instagram місця"
    }
}

private fun TravelMode.toPromptValue(language: AppLanguage): String = when (this) {
    TravelMode.WALKING -> when (language) {
        AppLanguage.EN -> "walking"
        AppLanguage.RU -> "пешком"
        AppLanguage.UK -> "пішки"
    }
    TravelMode.CAR -> when (language) {
        AppLanguage.EN -> "by car"
        AppLanguage.RU -> "на машине"
        AppLanguage.UK -> "на машині"
    }
    TravelMode.PUBLIC_TRANSPORT -> when (language) {
        AppLanguage.EN -> "public transport"
        AppLanguage.RU -> "общественный транспорт"
        AppLanguage.UK -> "громадський транспорт"
    }
    TravelMode.TAXI -> when (language) {
        AppLanguage.EN -> "taxi"
        AppLanguage.RU -> "такси"
        AppLanguage.UK -> "таксі"
    }
}
