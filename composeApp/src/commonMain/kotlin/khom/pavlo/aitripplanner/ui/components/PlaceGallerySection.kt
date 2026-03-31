package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import khom.pavlo.aitripplanner.ui.model.PlaceGalleryImageUiModel
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceGallerySection(
    title: String,
    subtitle: String,
    images: List<PlaceGalleryImageUiModel>,
    photoContentDescription: String,
    placeholderLabel: String,
    photoAttributionPrefix: String,
    modifier: Modifier = Modifier,
) {
    var selectedImageUrl by remember { mutableStateOf<String?>(null) }
    var selectedCaption by remember { mutableStateOf<String?>(null) }

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
            LazyRow(
                contentPadding = PaddingValues(horizontal = TravelTheme.spacing.lg),
                horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
            ) {
                items(images, key = { it.id }) { image ->
                    Column(
                        modifier = Modifier.width(188.dp),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                    ) {
                        if (!image.imageUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = image.imageUrl,
                                contentDescription = photoContentDescription,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(132.dp)
                                    .clip(TravelTheme.corners.medium)
                                    .clickable {
                                        selectedImageUrl = image.imageUrl
                                        selectedCaption = image.attribution?.let { "$photoAttributionPrefix $it" }
                                    },
                                contentScale = ContentScale.Crop,
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(132.dp)
                                    .clip(TravelTheme.corners.medium)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Collections,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.66f),
                                )
                                Text(
                                    text = placeholderLabel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                        Text(
                            text = if (!image.attribution.isNullOrBlank()) {
                                "$photoAttributionPrefix ${image.attribution}"
                            } else {
                                placeholderLabel
                            },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }

    selectedImageUrl?.let { imageUrl ->
        PhotoViewerDialog(
            imageUrl = imageUrl,
            contentDescription = photoContentDescription,
            caption = selectedCaption,
            onDismiss = {
                selectedImageUrl = null
                selectedCaption = null
            },
        )
    }
}
