package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceHeroImage(
    title: String,
    subtitle: String,
    imageUrl: String?,
    statusLabel: String,
    dayLabel: String,
    photoContentDescription: String,
    photoAttributionPrefix: String,
    photoAttribution: String?,
    backActionLabel: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(TravelTheme.corners.large),
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = imageUrl,
                contentDescription = photoContentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
                                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.98f),
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                )
            }
        }

        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.82f),
                        ),
                    ),
                ),
        )

        Column(
            modifier = Modifier
                .matchParentSize()
                .padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                SoftIconActionButton(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = backActionLabel,
                    onClick = onBackClick,
                    size = 42.dp,
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f),
                    borderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.22f),
                    contentColor = MaterialTheme.colorScheme.onSurface,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs)) {
                    TravelInfoChip(
                        text = dayLabel,
                        backgroundColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.18f),
                    )
                    TravelInfoChip(
                        text = statusLabel,
                        backgroundColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.82f),
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f),
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!photoAttribution.isNullOrBlank()) {
                    Text(
                        text = "$photoAttributionPrefix $photoAttribution",
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                                shape = CircleShape,
                            )
                            .padding(horizontal = TravelTheme.spacing.sm, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
