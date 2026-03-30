package khom.pavlo.aitripplanner.domain.model

data class CitySuggestion(
    val id: String,
    val primaryText: String,
    val secondaryText: String,
    val latitude: Double?,
    val longitude: Double?,
)
