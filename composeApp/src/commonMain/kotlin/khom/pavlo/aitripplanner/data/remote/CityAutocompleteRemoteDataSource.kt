package khom.pavlo.aitripplanner.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.CitySuggestion
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

data class PhotonConfig(
    val baseUrl: String = "https://photon.komoot.io",
)

interface CityAutocompleteRemoteDataSource {
    suspend fun searchCities(query: String, language: AppLanguage, limit: Int = 6): List<CitySuggestion>
}

class PhotonCityAutocompleteRemoteDataSource(
    private val client: HttpClient,
    private val config: PhotonConfig,
) : CityAutocompleteRemoteDataSource {
    override suspend fun searchCities(query: String, language: AppLanguage, limit: Int): List<CitySuggestion> {
        val normalizedQuery = query.trim()
        if (normalizedQuery.length < 2) return emptyList()

        val searchPlans = buildSearchPlans(normalizedQuery, language)
        val collected = linkedMapOf<String, CitySuggestion>()

        searchPlans.forEach { plan ->
            val response = client.get("${config.baseUrl}/api") {
                parameter("q", plan.query)
                parameter("limit", limit)
                plan.languageCode?.let { parameter("lang", it) }
                parameter("layer", "city")
                parameter("dedupe", 1)
            }

            response.body<PhotonSearchResponseDto>()
                .features
                .mapNotNull { feature -> feature.toSuggestion() }
                .forEach { suggestion ->
                    collected.putIfAbsent(suggestion.id, suggestion)
                }

            if (collected.size >= limit) return@forEach
        }

        return collected.values
            .distinctBy { suggestion -> suggestion.primaryText.lowercase() to suggestion.secondaryText.lowercase() }
            .take(limit)
    }
}

private data class PhotonSearchPlan(
    val query: String,
    val languageCode: String?,
)

@Serializable
private data class PhotonSearchResponseDto(
    val features: List<PhotonFeatureDto> = emptyList(),
)

@Serializable
private data class PhotonFeatureDto(
    val properties: PhotonPropertiesDto,
    val geometry: PhotonGeometryDto? = null,
)

@Serializable
private data class PhotonPropertiesDto(
    val name: String? = null,
    val city: String? = null,
    val state: String? = null,
    val country: String? = null,
    @SerialName("countrycode")
    val countryCode: String? = null,
)

@Serializable
private data class PhotonGeometryDto(
    val coordinates: List<Double> = emptyList(),
)

private fun PhotonFeatureDto.toSuggestion(): CitySuggestion? {
    val primary = properties.city?.ifBlank { null }
        ?: properties.name?.ifBlank { null }
        ?: return null

    val secondary = listOfNotNull(
        properties.state?.takeIf { it.isNotBlank() && !it.equals(primary, ignoreCase = true) },
        properties.country?.takeIf { it.isNotBlank() },
    ).joinToString(", ")

    val latitude = geometry?.coordinates?.getOrNull(1)
    val longitude = geometry?.coordinates?.getOrNull(0)

    return CitySuggestion(
        id = buildString {
            append(primary.lowercase())
            if (!properties.countryCode.isNullOrBlank()) {
                append("-")
                append(properties.countryCode.lowercase())
            }
            if (latitude != null && longitude != null) {
                append("-")
                append(latitude)
                append("-")
                append(longitude)
            }
        },
        primaryText = primary,
        secondaryText = secondary,
        latitude = latitude,
        longitude = longitude,
    )
}

private fun AppLanguage.toPhotonLanguage(): String = when (this) {
    AppLanguage.EN -> "en"
    AppLanguage.RU -> "ru"
    AppLanguage.UK -> "uk"
}

private fun buildSearchPlans(query: String, language: AppLanguage): List<PhotonSearchPlan> {
    val normalized = query.trim()
    val alias = normalized.cityAlias(language)
    val transliterated = normalized.transliterateCyrillic()

    return buildList {
        add(PhotonSearchPlan(query = normalized, languageCode = language.preferredPhotonResultLanguage()))
        if (alias != null && alias != normalized) {
            add(PhotonSearchPlan(query = alias, languageCode = "en"))
        }
        if (transliterated != normalized && transliterated.length >= 2) {
            add(PhotonSearchPlan(query = transliterated, languageCode = "en"))
        }
        add(PhotonSearchPlan(query = normalized, languageCode = null))
        if (alias != null && alias != normalized) {
            add(PhotonSearchPlan(query = alias, languageCode = null))
        }
        if (transliterated != normalized && transliterated.length >= 2) {
            add(PhotonSearchPlan(query = transliterated, languageCode = null))
        }
    }.distinct()
}

private fun AppLanguage.preferredPhotonResultLanguage(): String? = when (this) {
    AppLanguage.EN -> "en"
    // Photon public demo is commonly imported with a limited language set, so English is a safer fallback.
    AppLanguage.RU, AppLanguage.UK -> "en"
}

private fun String.cityAlias(language: AppLanguage): String? {
    val key = trim().lowercase()
    return when (language) {
        AppLanguage.RU -> russianCityAliases[key]
        AppLanguage.UK -> ukrainianCityAliases[key]
        AppLanguage.EN -> null
    }
}

private fun String.transliterateCyrillic(): String {
    val builder = StringBuilder(length)
    for (character in this) {
        builder.append(cyrillicToLatin[character] ?: character)
    }
    return builder.toString()
}

private val russianCityAliases = mapOf(
    "рим" to "rome",
    "париж" to "paris",
    "вена" to "vienna",
    "прага" to "prague",
    "варшава" to "warsaw",
    "будапешт" to "budapest",
    "загреб" to "zagreb",
    "киев" to "kyiv",
    "львов" to "lviv",
    "флоренция" to "florence",
    "мюнхен" to "munich",
)

private val ukrainianCityAliases = mapOf(
    "рим" to "rome",
    "париж" to "paris",
    "відень" to "vienna",
    "прага" to "prague",
    "варшава" to "warsaw",
    "будапешт" to "budapest",
    "загреб" to "zagreb",
    "київ" to "kyiv",
    "львів" to "lviv",
    "флоренція" to "florence",
    "мюнхен" to "munich",
)

private val cyrillicToLatin = mapOf(
    'А' to "A", 'а' to "a",
    'Б' to "B", 'б' to "b",
    'В' to "V", 'в' to "v",
    'Г' to "H", 'г' to "h",
    'Ґ' to "G", 'ґ' to "g",
    'Д' to "D", 'д' to "d",
    'Е' to "E", 'е' to "e",
    'Ё' to "E", 'ё' to "e",
    'Є' to "Ye", 'є' to "ie",
    'Ж' to "Zh", 'ж' to "zh",
    'З' to "Z", 'з' to "z",
    'И' to "I", 'и' to "i",
    'І' to "I", 'і' to "i",
    'Ї' to "Yi", 'ї' to "i",
    'Й' to "Y", 'й' to "i",
    'К' to "K", 'к' to "k",
    'Л' to "L", 'л' to "l",
    'М' to "M", 'м' to "m",
    'Н' to "N", 'н' to "n",
    'О' to "O", 'о' to "o",
    'П' to "P", 'п' to "p",
    'Р' to "R", 'р' to "r",
    'С' to "S", 'с' to "s",
    'Т' to "T", 'т' to "t",
    'У' to "U", 'у' to "u",
    'Ф' to "F", 'ф' to "f",
    'Х' to "Kh", 'х' to "kh",
    'Ц' to "Ts", 'ц' to "ts",
    'Ч' to "Ch", 'ч' to "ch",
    'Ш' to "Sh", 'ш' to "sh",
    'Щ' to "Shch", 'щ' to "shch",
    'Ы' to "Y", 'ы' to "y",
    'Э' to "E", 'э' to "e",
    'Ю' to "Yu", 'ю' to "yu",
    'Я' to "Ya", 'я' to "ya",
    'Ь' to "", 'ь' to "",
    'Ъ' to "", 'ъ' to "",
)
