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

@Composable
fun PlannerScreen(
    state: PlannerScreenState,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onCityChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onSummaryChange: (String) -> Unit,
    onHeroNoteChange: (String) -> Unit,
    onSaveClick: () -> Unit,
    onBackClick: () -> Unit,
    onTripClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    val strings = appStrings()
    val isEdit = state.mode is PlannerMode.Edit

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
                            tripTitle = state.title,
                            summary = state.summary,
                            heroNote = state.heroNote,
                            cityLabel = strings.cityFieldLabel,
                            tripTitleLabel = strings.tripTitleFieldLabel,
                            summaryLabel = strings.summaryFieldLabel,
                            heroNoteLabel = strings.heroNoteFieldLabel,
                            helperText = strings.helperText,
                            actionLabel = if (isEdit) strings.editTripAction else strings.createTripAction,
                            onCityChange = onCityChange,
                            onTripTitleChange = onTitleChange,
                            onSummaryChange = onSummaryChange,
                            onHeroNoteChange = onHeroNoteChange,
                            onSubmit = onSaveClick,
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
            onTitleChange = {},
            onSummaryChange = {},
            onHeroNoteChange = {},
            onSaveClick = {},
            onBackClick = {},
            onTripClick = {},
        )
    }
}
