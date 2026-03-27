package khom.pavlo.aitripplanner.ui.animation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import kotlinx.coroutines.delay

@Composable
fun StaggeredAppearance(
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(index == 0) }
    LaunchedEffect(index) {
        delay(index * 80L)
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        modifier = modifier,
        enter = fadeIn(tween(550)) + slideInVertically(
            animationSpec = tween(550, easing = FastOutSlowInEasing),
            initialOffsetY = { it / 5 },
        ),
    ) {
        content()
    }
}

@Composable
fun ShimmerPlaceholderCard(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shift",
    )
    val brush = Brush.linearGradient(
        colors = listOf(
            Color(0xFFEDE4D8),
            Color(0xFFF8F3EC),
            Color(0xFFEDE4D8),
        ),
        start = androidx.compose.ui.geometry.Offset.Zero,
        end = androidx.compose.ui.geometry.Offset(300f * shift, 300f * shift),
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(28.dp))
            .padding(TravelTheme.spacing.xl),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(14.dp)
                .background(brush, RoundedCornerShape(100.dp)),
        )
        Box(
            modifier = Modifier
                .padding(top = TravelTheme.spacing.md)
                .fillMaxWidth(0.8f)
                .height(22.dp)
                .background(brush, RoundedCornerShape(100.dp)),
        )
        Box(
            modifier = Modifier
                .padding(top = TravelTheme.spacing.sm)
                .fillMaxWidth()
                .height(56.dp)
                .background(brush, RoundedCornerShape(18.dp)),
        )
    }
}
