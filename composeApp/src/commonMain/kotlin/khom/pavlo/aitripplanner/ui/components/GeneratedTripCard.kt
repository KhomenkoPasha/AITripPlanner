package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun GeneratedTripCard(
    trip: TripOverviewUiModel,
    onOpenClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = appStrings()

    TravelCardSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                ) {
                    Text(
                        text = strings.generatedTripLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                    Text(
                        text = trip.title,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = trip.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = trip.city,
                    modifier = Modifier
                        .background(
                            color = TravelTheme.extendedColors.softAccent,
                            shape = CircleShape,
                        )
                        .padding(
                            horizontal = TravelTheme.spacing.md,
                            vertical = TravelTheme.spacing.xs,
                        ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            RouteSummaryRow(
                summary = RouteSummaryUiModel(
                    durationLabel = trip.durationLabel,
                    distanceLabel = trip.distanceLabel,
                    paceLabel = trip.daysLabel,
                ),
            )
            Text(
                text = trip.syncLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
            PrimaryActionButton(
                text = strings.openTripDetailsAction,
                onClick = onOpenClick,
            )
        }
    }
}
