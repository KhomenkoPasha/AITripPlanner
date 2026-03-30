package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.MaterialTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceSummaryChips(
    chips: List<String>,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
    ) {
        chips.forEach { label ->
            TravelInfoChip(
                text = label,
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(bottom = 2.dp),
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                borderColor = TravelTheme.extendedColors.cardStroke,
            )
        }
    }
}
