package khom.pavlo.aitripplanner.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
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
    showTopRouteLine: Boolean = false,
    showBottomRouteLine: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val titleDecoration = if (place.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val supportingAlpha = if (place.isCompleted) 0.66f else 1f
    val hasActions = onCompletionChange != null || onDeleteClick != null
    val cardShape = TravelTheme.corners.large
    val photoShape = TravelTheme.corners.medium
    var selectedImageUrl by remember(place.photoUrl) { mutableStateOf<String?>(null) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .animateContentSize(),
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        PlaceRouteIndicator(
            showTopLine = showTopRouteLine,
            showBottomLine = showBottomRouteLine,
            isCompleted = place.isCompleted,
            modifier = Modifier.fillMaxHeight(),
        )
        Surface(
            modifier = Modifier
                .weight(1f)
                .clip(cardShape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            shape = cardShape,
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (place.isCompleted) 0.92f else 0.98f),
            border = BorderStroke(
                width = 1.dp,
                color = TravelTheme.extendedColors.cardStroke.copy(alpha = 0.92f),
            ),
            tonalElevation = 1.dp,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .animateContentSize(),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                ) {
                    if (!place.photoUrl.isNullOrBlank()) {
                        Surface(
                            shape = photoShape,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = TravelTheme.extendedColors.cardStroke,
                            ),
                        ) {
                            AsyncImage(
                                model = place.photoUrl,
                                contentDescription = photoContentDescription,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(148.dp)
                                    .clip(photoShape)
                                    .clickable {
                                        selectedImageUrl = place.photoUrl
                                    }
                                    .alpha(if (place.isCompleted) 0.78f else 1f),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
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
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        if (place.note.isNotBlank()) {
                            Text(
                                text = place.note,
                                modifier = Modifier.alpha(supportingAlpha),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.92f),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
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
                                modifier = Modifier
                                    .weight(1f)
                                    .alpha(supportingAlpha),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.9f),
                                textAlign = TextAlign.End,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
                if (hasActions) {
                    Column(
                        modifier = Modifier
                            .width(52.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                shape = TravelTheme.corners.medium,
                            )
                            .border(
                                width = 1.dp,
                                color = TravelTheme.extendedColors.cardStroke.copy(alpha = 0.88f),
                                shape = TravelTheme.corners.medium,
                            )
                            .padding(vertical = 8.dp, horizontal = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
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
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                                borderColor = MaterialTheme.colorScheme.error.copy(alpha = 0.12f),
                                contentColor = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                }
            }
        }
    }

    selectedImageUrl?.let { imageUrl ->
        PhotoViewerDialog(
            imageUrl = imageUrl,
            contentDescription = photoContentDescription,
            caption = place.photoAttribution?.let { "$photoAttributionPrefix $it" },
            onDismiss = { selectedImageUrl = null },
        )
    }
}

@Composable
private fun PlaceRouteIndicator(
    showTopLine: Boolean,
    showBottomLine: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier,
) {
    val lineColor = MaterialTheme.colorScheme.secondary.copy(alpha = if (isCompleted) 0.2f else 0.3f)
    val nodeColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = if (isCompleted) 0.72f else 0.92f)
    val nodeBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = if (isCompleted) 0.24f else 0.34f)
    val nodeSize = 10.dp
    val connectorInset = 8.dp
    val strokeWidth = 1.5.dp

    Box(
        modifier = modifier
            .width(20.dp)
            .drawBehind {
                val centerX = size.width / 2f
                val nodeRadius = nodeSize.toPx() / 2f
                val centerY = size.height / 2f
                val minCenterY = connectorInset.toPx() + nodeRadius
                val maxCenterY = size.height - connectorInset.toPx() - nodeRadius
                val clampedCenterY = centerY.coerceIn(minCenterY, maxCenterY)

                if (showTopLine) {
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, connectorInset.toPx()),
                        end = Offset(centerX, clampedCenterY - nodeRadius - 2.dp.toPx()),
                        strokeWidth = strokeWidth.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
                if (showBottomLine) {
                    drawLine(
                        color = lineColor,
                        start = Offset(centerX, clampedCenterY + nodeRadius + 2.dp.toPx()),
                        end = Offset(centerX, size.height - connectorInset.toPx()),
                        strokeWidth = strokeWidth.toPx(),
                        cap = StrokeCap.Round,
                    )
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(nodeSize)
                .background(
                    color = nodeColor,
                    shape = TravelTheme.corners.small,
                )
                .border(
                    width = 1.dp,
                    color = nodeBorderColor,
                    shape = TravelTheme.corners.small,
                ),
        )
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
