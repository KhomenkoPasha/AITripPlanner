package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun TravelModeSelector(
    label: String,
    selectedMode: TravelMode,
    options: List<Pair<TravelMode, String>>,
    onModeSelected: (TravelMode) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    androidx.compose.foundation.layout.Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
        ) {
            options.forEach { (mode, modeLabel) ->
                val selected = mode == selectedMode
                Text(
                    text = modeLabel,
                    modifier = Modifier
                        .clip(TravelTheme.corners.medium)
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
                            },
                            shape = TravelTheme.corners.medium,
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f)
                            } else {
                                TravelTheme.extendedColors.cardStroke
                            },
                            shape = TravelTheme.corners.medium,
                        )
                        .clickable(enabled = enabled, role = Role.RadioButton) {
                            onModeSelected(mode)
                        }
                        .semantics { this.selected = selected }
                        .padding(
                            horizontal = TravelTheme.spacing.md,
                            vertical = 10.dp,
                        ),
                    style = MaterialTheme.typography.labelLarge,
                    color = if (selected) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}
