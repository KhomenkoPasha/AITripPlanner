package khom.pavlo.aitripplanner.domain.model

data class TripEditorInput(
    val city: String,
    val title: String,
    val prompt: String,
    val days: Int,
    val placeCount: Int,
    val walkingMinutesPerDay: Int,
    val selectedInterests: List<Interest>,
    val selectedPace: Pace?,
    val selectedBudget: Budget?,
    val selectedCompanionType: CompanionType?,
    val selectedPreferences: List<TripPreference>,
    val withChildren: Boolean,
    val travelMode: TravelMode,
    val heroNote: String,
    val language: AppLanguage,
)
