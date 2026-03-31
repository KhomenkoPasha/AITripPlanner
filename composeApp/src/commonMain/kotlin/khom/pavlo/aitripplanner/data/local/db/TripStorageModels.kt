package khom.pavlo.aitripplanner.data.local.db

import khom.pavlo.aitripplanner.domain.model.PlaceRemotePhoto
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

internal data class TripGraph(
    val trip: TripEntity,
    val days: List<DayGraph>,
)

internal data class DayGraph(
    val day: DayEntity,
    val places: List<PlaceEntity>,
)

internal fun List<String>.encodeLines(): String = filter { it.isNotBlank() }.joinToString(separator = "\n")

internal fun String.decodeLines(): List<String> =
    lineSequence().map(String::trim).filter(String::isNotEmpty).toList()

internal fun List<PlaceRemotePhoto>.encodeStoredPhotos(json: Json): String = json.encodeToString(
    serializer = ListSerializer(StoredPlacePhoto.serializer()),
    value = map { StoredPlacePhoto(ref = it.ref, attribution = it.attribution) },
)

internal fun decodeStoredPhotos(
    json: Json,
    raw: String,
    legacyPrimaryRef: String?,
    legacyPrimaryAttribution: String?,
): List<PlaceRemotePhoto> {
    val normalized = raw.trim()
    if (normalized.startsWith("[")) {
        val decoded = runCatching {
            json.decodeFromString(
                deserializer = ListSerializer(StoredPlacePhoto.serializer()),
                string = normalized,
            )
        }.getOrNull()

        if (decoded != null) {
            return decoded.mapNotNull { stored ->
                stored.ref.trim().takeIf(String::isNotBlank)?.let { ref ->
                    PlaceRemotePhoto(
                        ref = ref,
                        attribution = stored.attribution?.trim()?.ifBlank { null },
                    )
                }
            }
        }
    }

    return buildList {
        legacyPrimaryRef
            ?.trim()
            ?.takeIf(String::isNotBlank)
            ?.let { ref ->
                add(
                    PlaceRemotePhoto(
                        ref = ref,
                        attribution = legacyPrimaryAttribution?.trim()?.ifBlank { null },
                    ),
                )
            }
        raw.decodeLines()
            .filterNot { photoRef -> any { it.ref == photoRef } }
            .forEach { photoRef ->
                add(PlaceRemotePhoto(ref = photoRef))
            }
    }
}

@Serializable
internal data class StoredPlacePhoto(
    val ref: String,
    val attribution: String? = null,
)
