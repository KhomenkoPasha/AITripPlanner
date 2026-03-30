package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun RouteSummaryRow(
    summary: RouteSummaryUiModel,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
    ) {
        TravelInfoChip(text = summary.durationLabel)
        TravelInfoChip(text = summary.distanceLabel)
        TravelInfoChip(
            text = summary.paceLabel,
            backgroundColor = TravelTheme.extendedColors.mutedAccent,
            contentColor = MaterialTheme.colorScheme.secondary,
            borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
        )
    }
}

@Composable
internal fun TravelInfoChip(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.76f),
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    borderColor: Color = TravelTheme.extendedColors.cardStroke,
) {
    Text(
        text = text,
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = TravelTheme.corners.small,
            )
            .border(
                width = 1.dp,
                color = borderColor,
                shape = TravelTheme.corners.small,
            )
            .padding(
                horizontal = TravelTheme.spacing.sm,
                vertical = 7.dp,
            ),
        style = MaterialTheme.typography.labelMedium,
        color = contentColor,
    )
}
