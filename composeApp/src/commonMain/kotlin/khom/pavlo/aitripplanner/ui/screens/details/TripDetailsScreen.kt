package khom.pavlo.aitripplanner.ui.screens.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import khom.pavlo.aitripplanner.ui.animation.ShimmerPlaceholderCard
import khom.pavlo.aitripplanner.ui.animation.StaggeredAppearance
import khom.pavlo.aitripplanner.ui.components.DayItineraryCard
import khom.pavlo.aitripplanner.ui.components.DeleteTripActionButton
import khom.pavlo.aitripplanner.ui.components.DeleteTripConfirmationDialog
import khom.pavlo.aitripplanner.ui.components.EditTripActionButton
import khom.pavlo.aitripplanner.ui.components.EmptyStateView
import khom.pavlo.aitripplanner.ui.components.ErrorStateView
import khom.pavlo.aitripplanner.ui.components.LanguageSelector
import khom.pavlo.aitripplanner.ui.components.LoadingStateView
import khom.pavlo.aitripplanner.ui.components.RouteSummaryRow
import khom.pavlo.aitripplanner.ui.components.SectionHeader
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.TravelCardSurface
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun TripDetailsScreen(
    state: TripDetailsScreenState,
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismissDelete: () -> Unit,
    onConfirmDelete: () -> Unit,
    onToggleDay: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    val strings = appStrings()

    DeleteTripConfirmationDialog(
        visible = state.isDeleteDialogVisible,
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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.md,
                        vertical = TravelTheme.spacing.sm,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = strings.backAction,
                    )
                    Text(text = strings.backAction)
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
        AnimatedContent(
            targetState = Triple(state.isLoading, state.errorMessage, state.trip),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "details_state",
        ) { target ->
            val trip = target.third
            when {
                target.first && trip == null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(TravelTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                    ) {
                        items(3) { ShimmerPlaceholderCard() }
                    }
                }

                target.second != null && trip == null -> {
                    LazyColumn(contentPadding = PaddingValues(TravelTheme.spacing.lg)) {
                        item {
                            ErrorStateView(
                                title = strings.detailsLoadErrorTitle,
                                subtitle = target.second ?: "",
                            )
                        }
                    }
                }

                trip != null -> {
                    LazyColumn(
                        contentPadding = PaddingValues(TravelTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                    ) {
                        item {
                            TravelCardSurface {
                                Column(
                                    modifier = Modifier.padding(TravelTheme.spacing.xl),
                                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                                ) {
                                    Text(
                                        text = trip.city,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = trip.subtitle,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = trip.heroNote,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.secondary,
                                    )
                                    RouteSummaryRow(summary = trip.summary)
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                                    ) {
                                        EditTripActionButton(
                                            text = strings.editAction,
                                            onClick = onEditClick,
                                            enabled = !state.isDeleting,
                                        )
                                        DeleteTripActionButton(
                                            text = strings.deleteAction,
                                            onClick = onDeleteClick,
                                            isLoading = state.isDeleting,
                                        )
                                    }
                                }
                            }
                        }
                        if (state.deleteErrorMessage != null) {
                            item {
                                ErrorStateView(
                                    title = strings.detailsDeleteFailedTitle,
                                    subtitle = state.deleteErrorMessage,
                                )
                            }
                        }
                        if (state.isDeleting) {
                            item {
                                LoadingStateView(
                                    title = strings.detailsDeletingTitle,
                                    subtitle = strings.detailsDeletingSubtitle,
                                )
                            }
                        }
                        item {
                            SectionHeader(
                                title = strings.dayByDayTitle,
                                subtitle = trip.syncLabel,
                            )
                        }
                        items(trip.days) { day ->
                            StaggeredAppearance(index = day.dayLabel.removePrefix("Day ").toIntOrNull()?.minus(1) ?: 0) {
                                DayItineraryCard(
                                    day = day,
                                    onToggleExpand = { expanded -> onToggleDay(day.id, expanded) },
                                )
                            }
                        }
                    }
                }

                else -> {
                    LazyColumn(contentPadding = PaddingValues(TravelTheme.spacing.lg)) {
                        item {
                            EmptyStateView(
                                title = strings.detailsEmptyTitle,
                                subtitle = strings.detailsEmptySubtitle,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun TripDetailsScreenPreview() {
    AppTheme {
        TripDetailsScreen(
            state = PreviewTrips.detailsState(),
            selectedLanguage = AppLanguage.EN,
            onLanguageSelected = {},
            onBackClick = {},
            onEditClick = {},
            onDeleteClick = {},
            onDismissDelete = {},
            onConfirmDelete = {},
            onToggleDay = { _, _ -> },
        )
    }
}
