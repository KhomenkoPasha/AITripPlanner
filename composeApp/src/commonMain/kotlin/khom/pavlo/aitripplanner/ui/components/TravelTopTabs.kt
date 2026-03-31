package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun TravelTopTabs(
    options: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                shape = CircleShape,
            )
            .padding(TravelTheme.spacing.xs),
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            Text(
                text = option,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                    )
                    .clickable { onSelected(option) }
                    .padding(
                        horizontal = TravelTheme.spacing.md,
                        vertical = TravelTheme.spacing.sm,
                    ),
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
