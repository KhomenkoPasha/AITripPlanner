package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import khom.pavlo.aitripplanner.ui.model.PlacePhotoUiModel
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceLocalPhotosSection(
    title: String,
    subtitle: String,
    addPhotoLabel: String,
    emptyTitle: String,
    emptySubtitle: String,
    photoContentDescription: String,
    deletePhotoContentDescription: String,
    photos: List<PlacePhotoUiModel>,
    deletingPhotoIds: Set<String>,
    isAddingPhoto: Boolean,
    onAddPhotoClick: () -> Unit,
    onDeletePhotoClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }

    TravelCardSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(vertical = TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            SectionHeader(
                title = title,
                subtitle = subtitle,
                modifier = Modifier.padding(horizontal = TravelTheme.spacing.lg),
            )
            PrimaryActionButton(
                text = addPhotoLabel,
                onClick = onAddPhotoClick,
                isLoading = isAddingPhoto,
                modifier = Modifier.padding(horizontal = TravelTheme.spacing.lg),
            )
            if (photos.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = TravelTheme.spacing.lg,
                            end = TravelTheme.spacing.lg,
                            bottom = TravelTheme.spacing.xs,
                        )
                        .clip(TravelTheme.corners.medium)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.54f))
                        .padding(TravelTheme.spacing.lg),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PhotoLibrary,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = emptyTitle,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = emptySubtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = TravelTheme.spacing.lg),
                    horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                ) {
                    items(photos, key = { it.id }) { photo ->
                        Box(
                            modifier = Modifier
                                .width(188.dp)
                                .height(132.dp),
                        ) {
                            AsyncImage(
                                model = photo.localUri,
                                contentDescription = photoContentDescription,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(TravelTheme.corners.medium)
                                    .clickable {
                                        selectedImageUrl = photo.localUri
                                    },
                                contentScale = ContentScale.Crop,
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(TravelTheme.spacing.sm),
                            ) {
                                if (photo.id in deletingPhotoIds) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.82f))
                                            .padding(10.dp),
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                    }
                                } else {
                                    SoftIconActionButton(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = deletePhotoContentDescription,
                                        onClick = { onDeletePhotoClick(photo.id) },
                                        size = 34.dp,
                                        iconSize = 16.dp,
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                                        borderColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f),
                                        contentColor = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
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
            onDismiss = { selectedImageUrl = null },
        )
    }
}
