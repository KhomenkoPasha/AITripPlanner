package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.draw.clip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceActionsBar(
    markVisitedLabel: String,
    showOnMapLabel: String,
    openWebsiteLabel: String,
    removeLabel: String,
    replaceLabel: String,
    isVisited: Boolean,
    onToggleVisited: () -> Unit,
    modifier: Modifier = Modifier,
    onShowOnMap: (() -> Unit)? = null,
    onOpenWebsite: (() -> Unit)? = null,
) {
    TravelCardSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
            ) {
                PrimaryActionButton(
                    text = markVisitedLabel,
                    onClick = onToggleVisited,
                    modifier = Modifier.weight(1f),
                )
                OutlinedButton(
                    onClick = { onShowOnMap?.invoke() },
                    enabled = onShowOnMap != null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(TravelTheme.corners.medium),
                    shape = TravelTheme.corners.medium,
                ) {
                    Text(
                        text = showOnMapLabel,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
            ) {
                OutlinedButton(
                    onClick = { onOpenWebsite?.invoke() },
                    enabled = onOpenWebsite != null,
                    modifier = Modifier
                        .weight(1f)
                        .clip(TravelTheme.corners.medium),
                    shape = TravelTheme.corners.medium,
                ) {
                    Text(
                        text = openWebsiteLabel,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                OutlinedButton(
                    onClick = {},
                    enabled = false,
                    modifier = Modifier
                        .weight(1f)
                        .clip(TravelTheme.corners.medium),
                    shape = TravelTheme.corners.medium,
                ) {
                    Text(
                        text = removeLabel,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
