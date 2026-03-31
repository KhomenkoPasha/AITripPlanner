package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun SavedTripCard(
    trip: TripOverviewUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    editLabel: String? = null,
    deleteLabel: String? = null,
    onEditClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
) {
    TravelCardSurface(
        modifier = modifier
            .fillMaxWidth()
            .clip(TravelTheme.corners.large)
            .clickable(onClick = onClick),
    ) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xxs),
                ) {
                    Text(
                        text = trip.city,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Text(
                    text = trip.daysLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = trip.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RouteSummaryRow(
                summary = RouteSummaryUiModel(
                    durationLabel = trip.durationLabel,
                    distanceLabel = trip.distanceLabel,
                    paceLabel = trip.syncLabel,
                ),
            )
            if (onEditClick != null && onDeleteClick != null && editLabel != null && deleteLabel != null) {
                TripCardActions(
                    editLabel = editLabel,
                    deleteLabel = deleteLabel,
                    onEditClick = onEditClick,
                    onDeleteClick = onDeleteClick,
                    isDeleting = trip.isDeleting,
                )
            }
        }
    }
}

@Preview
@Composable
private fun SavedTripCardPreview() {
    AppTheme {
        SavedTripCard(
            trip = PreviewTrips.romeOverview,
            onClick = {},
            editLabel = "Edit",
            deleteLabel = "Delete",
            onEditClick = {},
            onDeleteClick = {},
            modifier = Modifier.padding(TravelTheme.spacing.lg),
        )
    }
}
