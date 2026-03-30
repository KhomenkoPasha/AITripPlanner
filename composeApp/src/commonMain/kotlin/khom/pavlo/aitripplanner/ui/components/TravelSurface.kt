package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
internal fun TravelCardSurface(
    modifier: Modifier = Modifier,
    tonalElevation: Dp = 1.dp,
    shadowElevation: Dp = 10.dp,
    verticalPadding: Dp = 3.dp,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier.padding(vertical = verticalPadding),
        shape = TravelTheme.corners.large,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = TravelTheme.extendedColors.cardStroke,
        ),
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
        content = content,
    )
}

@Composable
internal fun DecorativeBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    TravelTheme.extendedColors.backgroundTop,
                    TravelTheme.extendedColors.backgroundBottom,
                ),
            ),
        ),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .fillMaxSize(0.55f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            TravelTheme.extendedColors.accentGlow,
                            MaterialTheme.colorScheme.background.copy(alpha = 0f),
                        ),
                    ),
                    shape = CircleShape,
                ),
        )
    }
}
