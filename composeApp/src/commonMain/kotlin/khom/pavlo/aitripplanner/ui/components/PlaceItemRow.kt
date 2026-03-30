package khom.pavlo.aitripplanner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import khom.pavlo.aitripplanner.ui.model.PlaceUiModel
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceItemRow(
    place: PlaceUiModel,
    completionActionLabel: String,
    completedStatusLabel: String,
    plannedStatusLabel: String,
    photoContentDescription: String,
    photoAttributionPrefix: String,
    deleteLabel: String,
    onClick: (() -> Unit)? = null,
    onCompletionChange: ((Boolean) -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val titleDecoration = if (place.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val supportingAlpha = if (place.isCompleted) 0.66f else 1f
    val hasActions = onCompletionChange != null || onDeleteClick != null

    Row(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .width(14.dp)
                .height(14.dp)
                .background(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = TravelTheme.corners.small,
                )
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                    shape = TravelTheme.corners.small,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .height(6.dp)
                    .background(
                        color = MaterialTheme.colorScheme.tertiary,
                        shape = CircleShape,
                    ),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .then(
                    if (onClick != null) {
                        Modifier.clickable(onClick = onClick)
                    } else {
                        Modifier
                    },
                ),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                ) {
                    if (!place.photoUrl.isNullOrBlank()) {
                        Surface(
                            shape = TravelTheme.corners.medium,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = TravelTheme.extendedColors.cardStroke,
                            ),
                        ) {
                            AsyncImage(
                                model = place.photoUrl,
                                contentDescription = photoContentDescription,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(136.dp)
                                    .alpha(if (place.isCompleted) 0.78f else 1f),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = titleDecoration,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = place.address,
                        modifier = Modifier.alpha(supportingAlpha),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (place.note.isNotBlank()) {
                        Text(
                            text = place.note,
                            modifier = Modifier.alpha(supportingAlpha),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 4,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        TravelInfoChip(
                            text = place.visitTimeLabel,
                            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f),
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                        )
                        if (!place.photoAttribution.isNullOrBlank()) {
                            Text(
                                text = "$photoAttributionPrefix ${place.photoAttribution}",
                                modifier = Modifier.alpha(supportingAlpha),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (hasActions) {
                    Column(
                        modifier = Modifier.width(44.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                    ) {
                        if (onCompletionChange != null) {
                            AppCheckbox(
                                checked = place.isCompleted,
                                onCheckedChange = onCompletionChange,
                                contentDescription = completionActionLabel,
                                checkedStateLabel = completedStatusLabel,
                                uncheckedStateLabel = plannedStatusLabel,
                            )
                        }
                        if (onDeleteClick != null) {
                            SoftIconActionButton(
                                imageVector = Icons.Outlined.DeleteOutline,
                                contentDescription = deleteLabel,
                                onClick = onDeleteClick,
                                size = 36.dp,
                                iconSize = 17.dp,
                                containerColor = TravelTheme.extendedColors.errorTint,
                                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun PlaceItemRowPreview() {
    AppTheme {
        PlaceItemRow(
            place = PreviewTrips.romePlacesDay1.first(),
            completionActionLabel = "Mark place as completed",
            completedStatusLabel = "Completed",
            plannedStatusLabel = "Planned",
            photoContentDescription = "Place photo",
            photoAttributionPrefix = "Photo:",
            deleteLabel = "Delete",
            onClick = {},
            modifier = Modifier.padding(TravelTheme.spacing.lg),
        )
    }
}
