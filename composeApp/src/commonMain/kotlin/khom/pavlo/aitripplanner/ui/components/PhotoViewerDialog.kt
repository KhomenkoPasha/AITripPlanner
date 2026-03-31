package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import kotlinx.coroutines.coroutineScope
import kotlin.math.abs

@Composable
fun PhotoViewerDialog(
    imageUrl: String,
    contentDescription: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    caption: String? = null,
) {
    var scale by remember(imageUrl) { mutableStateOf(1f) }
    var offsetX by remember(imageUrl) { mutableStateOf(0f) }
    var offsetY by remember(imageUrl) { mutableStateOf(0f) }
    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            stiffness = Spring.StiffnessLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "photo_viewer_scale",
    )
    val animatedOffsetX by animateFloatAsState(
        targetValue = offsetX,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "photo_viewer_offset_x",
    )
    val animatedOffsetY by animateFloatAsState(
        targetValue = offsetY,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioNoBouncy,
        ),
        label = "photo_viewer_offset_y",
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
        ),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TravelTheme.extendedColors.backgroundTop.copy(alpha = 0.98f),
                            TravelTheme.extendedColors.backgroundBottom.copy(alpha = 0.995f),
                        ),
                    ),
                )
                .onSizeChanged { containerSize = it },
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = contentDescription,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 72.dp)
                    .pointerInput(imageUrl) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (scale > 1.05f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    scale = 2.5f
                                }
                            },
                        )
                    }
                    .pointerInput(imageUrl, containerSize, scale, offsetX, offsetY) {
                        coroutineScope {
                            detectTransformGestures { _, pan, zoom, _ ->
                                val amplifiedZoom = (1f + (zoom - 1f) * 5f).coerceAtLeast(0.2f)
                                val newScale = (scale * amplifiedZoom).coerceIn(1f, 5f)
                                if (abs(newScale - 1f) < 0.01f) {
                                    scale = 1f
                                    offsetX = 0f
                                    offsetY = 0f
                                } else {
                                    val maxOffsetX = (containerSize.width * (newScale - 1f)) / 2f
                                    val maxOffsetY = (containerSize.height * (newScale - 1f)) / 2f
                                    scale = newScale
                                    offsetX = (offsetX + pan.x).coerceIn(-maxOffsetX, maxOffsetX)
                                    offsetY = (offsetY + pan.y).coerceIn(-maxOffsetY, maxOffsetY)
                                }
                            }
                        }
                    }
                    .graphicsLayer {
                        scaleX = animatedScale
                        scaleY = animatedScale
                        translationX = animatedOffsetX
                        translationY = animatedOffsetY
                    },
                contentScale = ContentScale.Fit,
            )

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.84f))
                    .size(44.dp),
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }

            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.84f),
                            shape = TravelTheme.corners.medium,
                        )
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
