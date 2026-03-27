package khom.pavlo.aitripplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun DayItineraryCard(
    day: DayItineraryUiModel,
    onToggleExpand: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    TravelCardSurface(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { onToggleExpand(!day.isExpanded) },
    ) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                Text(
                    text = if (day.isExpanded) "Collapse" else "Expand",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = day.title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = day.subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            RouteSummaryRow(
                summary = RouteSummaryUiModel(
                    durationLabel = day.durationLabel,
                    distanceLabel = day.distanceLabel,
                    paceLabel = "${day.places.size} places",
                ),
            )
            AnimatedVisibility(visible = day.isExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md)) {
                    day.places.forEachIndexed { index, place ->
                        if (index > 0) {
                            HorizontalDivider(color = TravelTheme.extendedColors.cardStroke)
                        }
                        PlaceItemRow(place = place)
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
            modifier = Modifier.padding(TravelTheme.spacing.lg),
        )
    }
}
