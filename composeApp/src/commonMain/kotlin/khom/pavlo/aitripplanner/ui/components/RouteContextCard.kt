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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import khom.pavlo.aitripplanner.ui.model.RouteContextUiModel
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun RouteContextCard(
    routeContext: RouteContextUiModel,
    title: String,
    subtitle: String,
    previousStopLabel: String,
    nextStopLabel: String,
    noPreviousStopLabel: String,
    noNextStopLabel: String,
    modifier: Modifier = Modifier,
) {
    TravelCardSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            SectionHeader(title = title, subtitle = subtitle)
            Row(
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
            ) {
                TravelInfoChip(
                    text = routeContext.dayLabel,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.72f),
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                )
                TravelInfoChip(
                    text = routeContext.stopLabel,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                )
            }
            Text(
                text = routeContext.dayTitle,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
            ) {
                RouteNeighborCard(
                    label = previousStopLabel,
                    value = routeContext.previousPlaceName ?: noPreviousStopLabel,
                    modifier = Modifier.weight(1f),
                )
                RouteNeighborCard(
                    label = nextStopLabel,
                    value = routeContext.nextPlaceName ?: noNextStopLabel,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun RouteNeighborCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
                shape = TravelTheme.corners.medium,
            )
            .padding(TravelTheme.spacing.md),
        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
