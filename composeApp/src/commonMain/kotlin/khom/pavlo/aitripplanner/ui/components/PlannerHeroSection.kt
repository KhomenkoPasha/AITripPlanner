package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.animation.HeroAnimationSlot
import khom.pavlo.aitripplanner.ui.animation.HeroMotionState
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlannerHeroSection(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            Box(
                modifier = Modifier
                    .background(
                        color = TravelTheme.extendedColors.mutedAccent,
                        shape = CircleShape,
                    )
                    .padding(
                        horizontal = TravelTheme.spacing.md,
                        vertical = TravelTheme.spacing.xs,
                    ),
            ) {
                Text(
                    text = "Offline-first premium planner",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary,
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(160.dp),
        ) {
            HeroAnimationSlot(state = HeroMotionState("planner"))
        }
    }
}
