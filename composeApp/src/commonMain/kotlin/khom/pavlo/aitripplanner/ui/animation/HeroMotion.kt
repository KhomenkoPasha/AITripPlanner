package khom.pavlo.aitripplanner.ui.animation

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Immutable
data class HeroMotionState(
    val label: String,
)

fun interface HeroAnimationRenderer {
    @Composable
    fun Render(modifier: Modifier, state: HeroMotionState)
}

object PlaceholderHeroAnimationRenderer : HeroAnimationRenderer {
    @Composable
    override fun Render(modifier: Modifier, state: HeroMotionState) {
        val transition = rememberInfiniteTransition(label = "hero")
        val drift = transition.animateFloat(
            initialValue = -10f,
            targetValue = 10f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "drift",
        )
        val pulse = transition.animateFloat(
            initialValue = 0.75f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "pulse",
        )

        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(168.dp)
                    .alpha(0.9f)
                    .offset(y = drift.value.dp)
                    .background(
                        brush = Brush.radialGradient(
                            listOf(
                                TravelTheme.extendedColors.accentGlow,
                                Color.Transparent,
                            ),
                        ),
                        shape = CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size((92f * pulse.value).dp)
                    .background(
                        color = TravelTheme.extendedColors.softAccent,
                        shape = CircleShape,
                    ),
            )
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .offset(x = 36.dp, y = (-24).dp)
                    .background(
                        color = TravelTheme.extendedColors.mutedAccent,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
fun HeroAnimationSlot(
    modifier: Modifier = Modifier,
    state: HeroMotionState = HeroMotionState("planner"),
    renderer: HeroAnimationRenderer = PlaceholderHeroAnimationRenderer,
) {
    renderer.Render(modifier.fillMaxSize(), state)
}
