package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceMapPreviewCard(
    title: String,
    subtitle: String,
    placeholderLabel: String,
    showOnMapLabel: String,
    openInMapsLabel: String,
    modifier: Modifier = Modifier,
    onShowOnMap: (() -> Unit)? = null,
    onOpenInMaps: (() -> Unit)? = null,
) {
    TravelCardSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            SectionHeader(title = title, subtitle = subtitle)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.72f),
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.68f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                            ),
                        ),
                        shape = TravelTheme.corners.medium,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Outlined.Map,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = placeholderLabel,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
            ) {
                PrimaryActionButton(
                    text = showOnMapLabel,
                    onClick = { onShowOnMap?.invoke() },
                    enabled = onShowOnMap != null,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = { onOpenInMaps?.invoke() },
                    enabled = onOpenInMaps != null,
                    modifier = Modifier.weight(1f),
                    shape = TravelTheme.corners.medium,
                ) {
                    Text(
                        text = openInMapsLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
