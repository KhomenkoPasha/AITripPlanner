package khom.pavlo.aitripplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import aitripplanner.composeapp.generated.resources.Res
import aitripplanner.composeapp.generated.resources.places_count_label
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayItineraryCard(
    day: DayItineraryUiModel,
    completionActionLabel: String,
    completedStatusLabel: String,
    plannedStatusLabel: String,
    photoContentDescription: String,
    photoAttributionPrefix: String,
    deleteLabel: String,
    onPlaceClick: (String) -> Unit = {},
    onToggleExpand: (Boolean) -> Unit = {},
    onPlaceCompletionChange: (String, Boolean) -> Unit = { _, _ -> },
    onDeletePlace: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val strings = appStrings()
    val chevronRotation = animateFloatAsState(
        targetValue = if (day.isExpanded) 180f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.82f),
        label = "day_chevron_rotation",
    )

    TravelCardSurface(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier
                .animateContentSize()
                .padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (day.isExpanded) 0.42f else 0.26f),
                        shape = TravelTheme.corners.medium,
                    )
                    .border(
                        width = 1.dp,
                        color = TravelTheme.extendedColors.cardStroke.copy(alpha = if (day.isExpanded) 1f else 0.78f),
                        shape = TravelTheme.corners.medium,
                    )
                    .clickable { onToggleExpand(!day.isExpanded) }
                    .padding(TravelTheme.spacing.md),
                verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
            ) {
                Text(
                    text = day.dayLabel,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f),
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                    verticalAlignment = Alignment.Top,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                    ) {
                        Text(
                            text = day.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (day.isExpanded) 3 else 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = day.subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = if (day.isExpanded) 4 else 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    SoftIconActionButton(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = if (day.isExpanded) strings.collapseAction else strings.expandAction,
                        onClick = { onToggleExpand(!day.isExpanded) },
                        modifier = Modifier.graphicsLayer { rotationZ = chevronRotation.value },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                        borderColor = TravelTheme.extendedColors.cardStroke,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                RouteSummaryRow(
                    summary = RouteSummaryUiModel(
                        durationLabel = day.durationLabel,
                        distanceLabel = day.distanceLabel,
                        paceLabel = stringResource(Res.string.places_count_label, day.places.size),
                    ),
                )
            }
            AnimatedVisibility(
                visible = day.isExpanded,
                enter = fadeIn() + expandVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.86f),
                ),
                exit = fadeOut() + shrinkVertically(
                    animationSpec = spring(stiffness = Spring.StiffnessMedium, dampingRatio = 0.94f),
                ),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                    modifier = Modifier.animateContentSize(),
                ) {
                    HorizontalDivider(color = TravelTheme.extendedColors.cardStroke)
                    day.places.forEachIndexed { index, place ->
                        if (index > 0) {
                            HorizontalDivider(color = TravelTheme.extendedColors.cardStroke)
                        }
                        PlaceItemRow(
                            place = place,
                            completionActionLabel = completionActionLabel,
                            completedStatusLabel = completedStatusLabel,
                            plannedStatusLabel = plannedStatusLabel,
                            photoContentDescription = photoContentDescription,
                            photoAttributionPrefix = photoAttributionPrefix,
                            deleteLabel = deleteLabel,
                            onClick = { onPlaceClick(place.id) },
                            onCompletionChange = { completed ->
                                onPlaceCompletionChange(place.id, completed)
                            },
                            onDeleteClick = { onDeletePlace(place.id) },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun DayItineraryCardPreview() {
    AppTheme {
        DayItineraryCard(
            day = PreviewTrips.romeDayCards.first(),
            completionActionLabel = "Mark place as completed",
            completedStatusLabel = "Completed",
            plannedStatusLabel = "Planned",
            photoContentDescription = "Place photo",
            photoAttributionPrefix = "Photo:",
            deleteLabel = "Delete",
            onPlaceClick = {},
            modifier = Modifier.padding(TravelTheme.spacing.lg),
        )
    }
}
