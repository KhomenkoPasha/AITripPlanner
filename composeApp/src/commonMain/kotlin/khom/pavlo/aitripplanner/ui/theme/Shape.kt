package khom.pavlo.aitripplanner.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

internal val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(16.dp),
    small = RoundedCornerShape(20.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(30.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

@Immutable
data class TravelCorners(
    val small: RoundedCornerShape = RoundedCornerShape(18.dp),
    val medium: RoundedCornerShape = RoundedCornerShape(24.dp),
    val large: RoundedCornerShape = RoundedCornerShape(30.dp),
    val extraLarge: RoundedCornerShape = RoundedCornerShape(36.dp),
)

