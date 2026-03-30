package khom.pavlo.aitripplanner.ui.screens.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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
import khom.pavlo.aitripplanner.ui.components.TravelInfoChip
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.TravelCardSurface
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

private enum class TripDetailsContentState {
    Loading,
    Error,
    Content,
    Empty,
}

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
    onPlaceCompletionChange: (String, Boolean) -> Unit,
    onDeletePlace: (String) -> Unit,
    onOpenPlace: (String, String) -> Unit,
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

    val contentState = when {
        state.isLoading && state.trip == null -> TripDetailsContentState.Loading
        state.errorMessage != null && state.trip == null -> TripDetailsContentState.Error
        state.trip != null -> TripDetailsContentState.Content
        else -> TripDetailsContentState.Empty
    }

    TravelAppScaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.lg,
                        vertical = TravelTheme.spacing.sm,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    shape = TravelTheme.corners.large,
                ) {
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
            targetState = contentState,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            label = "details_state",
        ) { target ->
            when (target) {
                TripDetailsContentState.Loading -> {
                    LazyColumn(
                        contentPadding = PaddingValues(TravelTheme.spacing.lg),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                    ) {
                        items(3) { ShimmerPlaceholderCard() }
                    }
                }

                TripDetailsContentState.Error -> {
                    LazyColumn(contentPadding = PaddingValues(TravelTheme.spacing.lg)) {
                        item {
                            ErrorStateView(
                                title = strings.detailsLoadErrorTitle,
                                subtitle = state.errorMessage ?: "",
                            )
                        }
                    }
                }

                TripDetailsContentState.Content -> {
                    val trip = state.trip ?: return@AnimatedContent
                    LazyColumn(
                        contentPadding = PaddingValues(
                            start = TravelTheme.spacing.lg,
                            top = TravelTheme.spacing.md,
                            end = TravelTheme.spacing.lg,
                            bottom = TravelTheme.spacing.xxl,
                        ),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xl),
                    ) {
                        item {
                            TravelCardSurface {
                                Column(
                                    modifier = Modifier.padding(TravelTheme.spacing.xl),
                                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top,
                                    ) {
                                        Text(
                                            text = trip.city,
                                            modifier = Modifier
                                                .background(
                                                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.68f),
                                                    shape = CircleShape,
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f),
                                                    shape = CircleShape,
                                                )
                                                .padding(
                                                    horizontal = TravelTheme.spacing.sm,
                                                    vertical = 6.dp,
                                                ),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                        TravelInfoChip(
                                            text = state.syncStatusLabel ?: trip.syncLabel,
                                            backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f),
                                            contentColor = MaterialTheme.colorScheme.secondary,
                                            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                                        )
                                    }
                                    Text(
                                        text = trip.subtitle,
                                        style = MaterialTheme.typography.headlineLarge,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 4,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    if (trip.heroNote.isNotBlank()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                                            verticalAlignment = Alignment.Top,
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(top = 7.dp)
                                                    .width(10.dp)
                                                    .height(10.dp)
                                                    .background(
                                                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.22f),
                                                        shape = CircleShape,
                                                    ),
                                            )
                                            Text(
                                                text = trip.heroNote,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.secondary,
                                                maxLines = 4,
                                                overflow = TextOverflow.Ellipsis,
                                            )
                                        }
                                    }
                                    RouteSummaryRow(summary = trip.summary)
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        EditTripActionButton(
                                            text = strings.editAction,
                                            onClick = onEditClick,
                                            enabled = !state.isDeleting,
                                            modifier = Modifier.weight(1f, fill = true),
                                        )
                                        DeleteTripActionButton(
                                            text = strings.deleteAction,
                                            onClick = onDeleteClick,
                                            isLoading = state.isDeleting,
                                            modifier = Modifier.weight(1f, fill = true),
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
                                subtitle = state.syncStatusLabel ?: trip.syncLabel,
                            )
                        }
                        if (trip.days.isEmpty()) {
                            item {
                                EmptyStateView(
                                    title = strings.detailsEmptyDaysTitle,
                                    subtitle = strings.detailsEmptyDaysSubtitle,
                                )
                            }
                        } else {
                            itemsIndexed(trip.days, key = { _, day -> day.id }) { index, day ->
                                StaggeredAppearance(index = index) {
                                    DayItineraryCard(
                                        day = day,
                                        completionActionLabel = strings.placeCompletionAction,
                                        completedStatusLabel = strings.placeCompletedStatus,
                                        plannedStatusLabel = strings.placePlannedStatus,
                                        photoContentDescription = strings.placePhotoContentDescription,
                                        photoAttributionPrefix = strings.placePhotoAttributionPrefix,
                                        deleteLabel = strings.deleteAction,
                                        onToggleExpand = { expanded -> onToggleDay(day.id, expanded) },
                                        onPlaceClick = { placeId -> onOpenPlace(day.id, placeId) },
                                        onPlaceCompletionChange = onPlaceCompletionChange,
                                        onDeletePlace = onDeletePlace,
                                    )
                                }
                            }
                        }
                    }
                }

                TripDetailsContentState.Empty -> {
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
            onPlaceCompletionChange = { _, _ -> },
            onDeletePlace = {},
            onOpenPlace = { _, _ -> },
        )
    }
}
