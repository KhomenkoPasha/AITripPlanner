package khom.pavlo.aitripplanner.ui.screens.saved

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.presentation.saved.SavedTripsScreenState
import khom.pavlo.aitripplanner.ui.animation.ShimmerPlaceholderCard
import khom.pavlo.aitripplanner.ui.animation.StaggeredAppearance
import khom.pavlo.aitripplanner.ui.components.DeleteTripConfirmationDialog
import khom.pavlo.aitripplanner.ui.components.EmptyStateView
import khom.pavlo.aitripplanner.ui.components.ErrorStateView
import khom.pavlo.aitripplanner.ui.components.LanguageSelector
import khom.pavlo.aitripplanner.ui.components.SavedTripCard
import khom.pavlo.aitripplanner.ui.components.SectionHeader
import khom.pavlo.aitripplanner.ui.components.ThemeSelector
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun SavedTripsScreen(
    state: SavedTripsScreenState,
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeMode,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onTripClick: (String) -> Unit,
    onEditTrip: (String) -> Unit,
    onDeleteTrip: (String) -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    val strings = appStrings()

    DeleteTripConfirmationDialog(
        visible = state.pendingDeleteTripId != null,
        title = strings.deleteTripTitle,
        message = strings.deleteTripMessage,
        confirmLabel = strings.deleteAction,
        cancelLabel = strings.cancelAction,
        onDismiss = onDismissDelete,
        onConfirm = onConfirmDelete,
    )

    TravelAppScaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.lg,
                        vertical = TravelTheme.spacing.md,
                    ),
                verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
            ) {
                Text(
                    text = strings.savedTripsTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs)) {
                        LanguageSelector(
                            selectedLanguage = selectedLanguage,
                            label = strings.languageLabel,
                            onLanguageSelected = onLanguageSelected,
                        )
                        ThemeSelector(
                            selectedTheme = selectedTheme,
                            label = strings.themeLabel,
                            systemLabel = strings.themeSystemLabel,
                            lightLabel = strings.themeLightLabel,
                            darkLabel = strings.themeDarkLabel,
                            onThemeSelected = onThemeSelected,
                        )
                    }
                }
            }
        },
        bottomBar = bottomBar,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
        ) {
            item {
                SectionHeader(
                    title = strings.savedTripsTitle,
                    subtitle = state.syncStatusLabel ?: strings.savedTripsSubtitle,
                )
            }
            if (state.deleteErrorMessage != null) {
                item {
                    ErrorStateView(
                        title = strings.savedDeleteFailedTitle,
                        subtitle = state.deleteErrorMessage,
                    )
                }
            }
            when {
                state.isLoading -> {
                    items(3) {
                        ShimmerPlaceholderCard()
                    }
                }

                state.errorMessage != null -> {
                    item {
                        ErrorStateView(
                            title = strings.savedErrorTitle,
                            subtitle = state.errorMessage,
                        )
                    }
                }

                state.trips.isEmpty() -> {
                    item {
                        EmptyStateView(
                            title = strings.savedEmptyTitle,
                            subtitle = strings.savedEmptySubtitle,
                        )
                    }
                }

                else -> {
                    itemsIndexed(state.trips, key = { _, item -> item.id }) { index, trip ->
                        AnimatedVisibility(
                            visible = !trip.isDeleting,
                            enter = fadeIn(animationSpec = tween(260)),
                            exit = shrinkVertically(animationSpec = tween(260)) + fadeOut(animationSpec = tween(220)),
                        ) {
                            StaggeredAppearance(index = index) {
                                SavedTripCard(
                                    trip = trip,
                                    onClick = { onTripClick(trip.id) },
                                    editLabel = strings.editAction,
                                    deleteLabel = strings.deleteAction,
                                    onEditClick = { onEditTrip(trip.id) },
                                    onDeleteClick = { onDeleteTrip(trip.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SavedTripsScreenPreview() {
    AppTheme {
        SavedTripsScreen(
            state = PreviewTrips.savedTripsState(),
            selectedLanguage = AppLanguage.EN,
            selectedTheme = AppThemeMode.SYSTEM,
            onLanguageSelected = {},
            onThemeSelected = {},
            onTripClick = {},
            onEditTrip = {},
            onDeleteTrip = {},
            onDismissDelete = {},
            onConfirmDelete = {},
        )
    }
}
