package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CitySuggestion
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.TripPreference
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PromptInputCard(
    city: String,
    citySuggestions: List<CitySuggestion>,
    isCitySuggestionsLoading: Boolean,
    shouldShowCitySuggestions: Boolean,
    tripTitle: String,
    prompt: String,
    days: String,
    placeCount: String,
    walkingMinutesPerDay: String,
    travelMode: TravelMode,
    travelModeOptions: List<Pair<TravelMode, String>>,
    heroNote: String,
    selectedInterests: List<Interest>,
    selectedPace: Pace?,
    selectedBudget: Budget?,
    selectedCompanionType: CompanionType?,
    selectedPreferences: List<TripPreference>,
    withChildren: Boolean,
    interestOptions: List<Pair<Interest, String>>,
    paceOptions: List<Pair<Pace, String>>,
    budgetOptions: List<Pair<Budget, String>>,
    companionOptions: List<Pair<CompanionType, String>>,
    preferenceOptions: List<Pair<TripPreference, String>>,
    cityLabel: String,
    tripTitleLabel: String,
    promptLabel: String,
    daysLabel: String,
    placeCountLabel: String,
    walkingMinutesPerDayLabel: String,
    heroNoteLabel: String,
    interestsTitle: String,
    paceTitle: String,
    budgetTitle: String,
    tripFormatTitle: String,
    preferencesTitle: String,
    withChildrenLabel: String,
    quickFiltersHelperText: String,
    helperText: String,
    citySuggestionsLoadingLabel: String,
    citySuggestionsEmptyLabel: String,
    actionLabel: String,
    onCityChange: (String) -> Unit,
    onCitySuggestionClick: (CitySuggestion) -> Unit,
    onTripTitleChange: (String) -> Unit,
    onPromptChange: (String) -> Unit,
    onDaysChange: (String) -> Unit,
    onPlaceCountChange: (String) -> Unit,
    onWalkingMinutesPerDayChange: (String) -> Unit,
    onTravelModeChange: (TravelMode) -> Unit,
    onInterestToggle: (Interest) -> Unit,
    onPaceSelected: (Pace) -> Unit,
    onBudgetSelected: (Budget) -> Unit,
    onCompanionTypeSelected: (CompanionType) -> Unit,
    onPreferenceToggle: (TripPreference) -> Unit,
    onWithChildrenToggle: () -> Unit,
    onHeroNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    errorMessage: String? = null,
) {
    TravelCardSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = cityLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            if (shouldShowCitySuggestions) {
                CitySuggestionsDropdown(
                    suggestions = citySuggestions,
                    isLoading = isCitySuggestionsLoading,
                    loadingLabel = citySuggestionsLoadingLabel,
                    emptyLabel = citySuggestionsEmptyLabel,
                    onSuggestionClick = onCitySuggestionClick,
                )
            }
            OutlinedTextField(
                value = tripTitle,
                onValueChange = onTripTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = tripTitleLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = days,
                onValueChange = onDaysChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = daysLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = placeCount,
                onValueChange = onPlaceCountChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = placeCountLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = walkingMinutesPerDay,
                onValueChange = onWalkingMinutesPerDayChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = walkingMinutesPerDayLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = prompt,
                onValueChange = onPromptChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = promptLabel) },
                minLines = 4,
                maxLines = 6,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = heroNote,
                onValueChange = onHeroNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = heroNoteLabel) },
                minLines = 2,
                maxLines = 3,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            QuickFiltersBlock(
                selectedInterests = selectedInterests,
                selectedPace = selectedPace,
                selectedBudget = selectedBudget,
                selectedTravelMode = travelMode,
                selectedCompanionType = selectedCompanionType,
                selectedPreferences = selectedPreferences,
                withChildren = withChildren,
                interestOptions = interestOptions,
                paceOptions = paceOptions,
                budgetOptions = budgetOptions,
                travelModeOptions = travelModeOptions,
                companionOptions = companionOptions,
                preferenceOptions = preferenceOptions,
                interestsTitle = interestsTitle,
                paceTitle = paceTitle,
                budgetTitle = budgetTitle,
                formatTitle = tripFormatTitle,
                preferencesTitle = preferencesTitle,
                withChildrenLabel = withChildrenLabel,
                helperText = quickFiltersHelperText,
                onInterestToggle = onInterestToggle,
                onPaceSelected = onPaceSelected,
                onBudgetSelected = onBudgetSelected,
                onTravelModeSelected = onTravelModeChange,
                onCompanionTypeSelected = onCompanionTypeSelected,
                onPreferenceToggle = onPreferenceToggle,
                onWithChildrenToggle = onWithChildrenToggle,
            )
            Text(
                text = errorMessage ?: helperText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (errorMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            PrimaryActionButton(
                text = actionLabel,
                onClick = onSubmit,
                enabled = enabled &&
                    city.isNotBlank() &&
                    tripTitle.isNotBlank() &&
                    prompt.isNotBlank() &&
                    days.isNotBlank() &&
                    placeCount.isNotBlank() &&
                    walkingMinutesPerDay.isNotBlank(),
                isLoading = isLoading,
            )
        }
    }
}

@Composable
private fun travelFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = TravelTheme.extendedColors.cardStroke,
    cursorColor = MaterialTheme.colorScheme.primary,
)
