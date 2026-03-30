package khom.pavlo.aitripplanner.ui.screens.planner

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CitySuggestion
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.TripPreference
import khom.pavlo.aitripplanner.ui.animation.StaggeredAppearance
import khom.pavlo.aitripplanner.ui.components.EmptyStateView
import khom.pavlo.aitripplanner.ui.components.GeneratedTripCard
import khom.pavlo.aitripplanner.ui.components.LanguageSelector
import khom.pavlo.aitripplanner.ui.components.LoadingStateView
import khom.pavlo.aitripplanner.ui.components.PlannerHeroSection
import khom.pavlo.aitripplanner.ui.components.PromptInputCard
import khom.pavlo.aitripplanner.ui.components.SavedTripCard
import khom.pavlo.aitripplanner.ui.components.SectionHeader
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import khom.pavlo.aitripplanner.presentation.label

@Composable
fun PlannerScreen(
    state: PlannerScreenState,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onCityChange: (String) -> Unit,
    onCitySuggestionClick: (CitySuggestion) -> Unit,
    onTitleChange: (String) -> Unit,
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
    onSaveClick: (AppLanguage) -> Unit,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    val strings = appStrings()
    val isEdit = state.mode is PlannerMode.Edit
    val travelModeOptions = rememberTravelModeOptions(selectedLanguage)
    val interestOptions = rememberInterestOptions(selectedLanguage)
    val paceOptions = rememberPaceOptions(selectedLanguage)
    val budgetOptions = rememberBudgetOptions(selectedLanguage)
    val companionOptions = rememberCompanionOptions(selectedLanguage)
    val preferenceOptions = rememberPreferenceOptions(selectedLanguage)

    TravelAppScaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.lg,
                        vertical = TravelTheme.spacing.md,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                ) {
                    if (isEdit) {
                        TextButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = strings.backAction,
                            )
                            Text(text = strings.backAction)
                        }
                    }
                    Text(
                        text = if (isEdit) strings.editTripTitle else strings.createTripTitle,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                LanguageSelector(
                    selectedLanguage = selectedLanguage,
                    label = strings.languageLabel,
                    onLanguageSelected = onLanguageSelected,
                )
            }
        },
        bottomBar = bottomBar,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = TravelTheme.spacing.lg,
                vertical = TravelTheme.spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
        ) {
            item {
                PlannerHeroSection(
                    title = if (isEdit) strings.editTripTitle else strings.createTripTitle,
                    subtitle = if (isEdit) strings.editTripSubtitle else strings.createTripSubtitle,
                )
            }
            item {
                AnimatedContent(
                    targetState = state.isLoadingEditorData,
                    label = "planner_editor_loading",
                ) { isLoading ->
                    if (isLoading) {
                        LoadingStateView(
                            title = strings.loadingEditTitle,
                            subtitle = strings.loadingEditSubtitle,
                        )
                    } else {
                        PromptInputCard(
                            city = state.city,
                            citySuggestions = state.citySuggestions,
                            isCitySuggestionsLoading = state.isCitySuggestionsLoading,
                            shouldShowCitySuggestions = state.shouldShowCitySuggestions,
                            tripTitle = state.title,
                            prompt = state.prompt,
                            days = state.days,
                            placeCount = state.placeCount,
                            walkingMinutesPerDay = state.walkingMinutesPerDay,
                            travelMode = state.travelMode,
                            travelModeOptions = travelModeOptions,
                            heroNote = state.heroNote,
                            selectedInterests = state.selectedInterests,
                            selectedPace = state.selectedPace,
                            selectedBudget = state.selectedBudget,
                            selectedCompanionType = state.selectedCompanionType,
                            selectedPreferences = state.selectedPreferences,
                            withChildren = state.withChildren,
                            interestOptions = interestOptions,
                            paceOptions = paceOptions,
                            budgetOptions = budgetOptions,
                            companionOptions = companionOptions,
                            preferenceOptions = preferenceOptions,
                            cityLabel = strings.cityFieldLabel,
                            tripTitleLabel = strings.tripTitleFieldLabel,
                            promptLabel = strings.promptFieldLabel,
                            daysLabel = strings.daysFieldLabel,
                            placeCountLabel = strings.placeCountFieldLabel,
                            walkingMinutesPerDayLabel = strings.walkingMinutesPerDayFieldLabel,
                            heroNoteLabel = strings.heroNoteFieldLabel,
                            interestsTitle = strings.interestsTitle,
                            paceTitle = strings.paceTitle,
                            budgetTitle = strings.budgetTitle,
                            tripFormatTitle = strings.tripFormatTitle,
                            preferencesTitle = strings.preferencesTitle,
                            withChildrenLabel = strings.withChildrenLabel,
                            quickFiltersHelperText = strings.quickFiltersHelperText,
                            helperText = strings.helperText,
                            citySuggestionsLoadingLabel = strings.citySuggestionsLoadingLabel,
                            citySuggestionsEmptyLabel = strings.citySuggestionsEmptyLabel,
                            actionLabel = if (isEdit) strings.editTripAction else strings.createTripAction,
                            onCityChange = onCityChange,
                            onCitySuggestionClick = onCitySuggestionClick,
                            onTripTitleChange = onTitleChange,
                            onPromptChange = onPromptChange,
                            onDaysChange = onDaysChange,
                            onPlaceCountChange = onPlaceCountChange,
                            onWalkingMinutesPerDayChange = onWalkingMinutesPerDayChange,
                            onTravelModeChange = onTravelModeChange,
                            onInterestToggle = onInterestToggle,
                            onPaceSelected = onPaceSelected,
                            onBudgetSelected = onBudgetSelected,
                            onCompanionTypeSelected = onCompanionTypeSelected,
                            onPreferenceToggle = onPreferenceToggle,
                            onWithChildrenToggle = onWithChildrenToggle,
                            onHeroNoteChange = onHeroNoteChange,
                            onSubmit = { onSaveClick(selectedLanguage) },
                            isLoading = state.isSaving,
                            errorMessage = state.saveError,
                        )
                    }
                }
            }
            item {
                SectionHeader(
                    title = strings.currentTripTitle,
                    subtitle = state.syncStatusLabel ?: strings.currentTripSubtitle,
                )
            }
            item {
                AnimatedContent(
                    targetState = state.currentTrip,
                    label = "planner_current_trip",
                ) { trip ->
                    if (trip != null) {
                        GeneratedTripCard(
                            trip = trip,
                            onOpenClick = { onTripClick(trip.id) },
                        )
                    } else {
                        EmptyStateView(
                            title = strings.currentTripEmptyTitle,
                            subtitle = strings.currentTripEmptySubtitle,
                        )
                    }
                }
            }
            item {
                SectionHeader(
                    title = strings.recentTripsTitle,
                    subtitle = strings.recentTripsSubtitle,
                )
            }
            if (state.savedTrips.isEmpty()) {
                item {
                    EmptyStateView(
                        title = strings.recentTripsEmptyTitle,
                        subtitle = strings.recentTripsEmptySubtitle,
                    )
                }
            } else {
                itemsIndexed(state.savedTrips.take(3), key = { _, item -> item.id }) { index, trip ->
                    StaggeredAppearance(index = index) {
                        SavedTripCard(
                            trip = trip,
                            onClick = { onTripClick(trip.id) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlannerScreenPreview() {
    AppTheme {
        PlannerScreen(
            state = PreviewTrips.plannerState(),
            selectedLanguage = AppLanguage.EN,
            onLanguageSelected = {},
            onCityChange = {},
            onCitySuggestionClick = {},
            onTitleChange = {},
            onPromptChange = {},
            onDaysChange = {},
            onPlaceCountChange = {},
            onWalkingMinutesPerDayChange = {},
            onTravelModeChange = {},
            onInterestToggle = {},
            onPaceSelected = {},
            onBudgetSelected = {},
            onCompanionTypeSelected = {},
            onPreferenceToggle = {},
            onWithChildrenToggle = {},
            onHeroNoteChange = {},
            onSaveClick = {},
            onBackClick = {},
            onTripClick = {},
        )
    }
}

@Composable
private fun rememberTravelModeOptions(language: AppLanguage): List<Pair<TravelMode, String>> = listOf(
    TravelMode.WALKING to TravelMode.WALKING.label(language),
    TravelMode.CAR to TravelMode.CAR.label(language),
    TravelMode.PUBLIC_TRANSPORT to TravelMode.PUBLIC_TRANSPORT.label(language),
)

@Composable
private fun rememberInterestOptions(language: AppLanguage): List<Pair<Interest, String>> = listOf(
    Interest.MUSEUMS to Interest.MUSEUMS.label(language),
    Interest.HISTORY to Interest.HISTORY.label(language),
    Interest.FOOD to Interest.FOOD.label(language),
    Interest.CAFE to Interest.CAFE.label(language),
    Interest.VIEWS to Interest.VIEWS.label(language),
    Interest.ARCHITECTURE to Interest.ARCHITECTURE.label(language),
    Interest.NATURE to Interest.NATURE.label(language),
    Interest.NIGHTLIFE to Interest.NIGHTLIFE.label(language),
)

@Composable
private fun rememberPaceOptions(language: AppLanguage): List<Pair<Pace, String>> = listOf(
    Pace.RELAXED to Pace.RELAXED.label(language),
    Pace.NORMAL to Pace.NORMAL.label(language),
    Pace.INTENSIVE to Pace.INTENSIVE.label(language),
)

@Composable
private fun rememberBudgetOptions(language: AppLanguage): List<Pair<Budget, String>> = listOf(
    Budget.BUDGET to Budget.BUDGET.label(language),
    Budget.MEDIUM to Budget.MEDIUM.label(language),
    Budget.PREMIUM to Budget.PREMIUM.label(language),
)

@Composable
private fun rememberCompanionOptions(language: AppLanguage): List<Pair<CompanionType, String>> = listOf(
    CompanionType.COUPLE to CompanionType.COUPLE.label(language),
    CompanionType.SOLO to CompanionType.SOLO.label(language),
)

@Composable
private fun rememberPreferenceOptions(language: AppLanguage): List<Pair<TripPreference, String>> = listOf(
    TripPreference.MUST_SEE to TripPreference.MUST_SEE.label(language),
    TripPreference.LOCAL_SPOTS to TripPreference.LOCAL_SPOTS.label(language),
    TripPreference.HIDDEN_GEMS to TripPreference.HIDDEN_GEMS.label(language),
    TripPreference.NO_CROWDS to TripPreference.NO_CROWDS.label(language),
    TripPreference.NO_RUSH to TripPreference.NO_RUSH.label(language),
    TripPreference.SUNSET to TripPreference.SUNSET.label(language),
    TripPreference.RAINY_DAY to TripPreference.RAINY_DAY.label(language),
    TripPreference.SHORT_ROUTE to TripPreference.SHORT_ROUTE.label(language),
    TripPreference.FREE_PLACES to TripPreference.FREE_PLACES.label(language),
    TripPreference.INSTAGRAM_SPOTS to TripPreference.INSTAGRAM_SPOTS.label(language),
)
