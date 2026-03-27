package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun RouteSummaryRow(
    summary: RouteSummaryUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
    ) {
        SummaryPill(text = summary.durationLabel)
        SummaryPill(text = summary.distanceLabel)
        SummaryPill(
            text = summary.paceLabel,
            backgroundColor = TravelTheme.extendedColors.mutedAccent,
            contentColor = MaterialTheme.colorScheme.secondary,
        )
    }
}

@Composable
private fun SummaryPill(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Text(
        text = text,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = CircleShape,
            )
            .padding(
                horizontal = TravelTheme.spacing.md,
                vertical = TravelTheme.spacing.xs,
            ),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
    )
}

