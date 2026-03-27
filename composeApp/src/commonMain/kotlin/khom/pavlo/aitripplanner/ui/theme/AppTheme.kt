package khom.pavlo.aitripplanner.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color

@Immutable
data class TravelExtendedColors(
    val backgroundTop: Color = SurfacePrimary,
    val backgroundBottom: Color = WarmBackgroundDeep,
    val cardStroke: Color = DividerSoft,
    val softAccent: Color = RoseSoft,
    val accentGlow: Color = Color(0x26C78F5D),
    val mutedAccent: Color = SageSoft,
    val successTint: Color = Color(0xFFEDF5EF),
    val errorTint: Color = Color(0xFFF8EAE6),
)

private val LocalTravelExtendedColors = staticCompositionLocalOf { TravelExtendedColors() }
private val LocalTravelCorners = staticCompositionLocalOf { TravelCorners() }

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(
        LocalTravelSpacing provides TravelSpacing(),
        LocalTravelExtendedColors provides TravelExtendedColors(),
        LocalTravelCorners provides TravelCorners(),
    ) {
        MaterialTheme(
            colorScheme = AppColorScheme,
            typography = AppTypography,
            shapes = AppShapes,
            content = content,
        )
    }
}

object TravelTheme {
    val spacing: TravelSpacing
        @Composable
        get() = LocalTravelSpacing.current

    val extendedColors: TravelExtendedColors
        @Composable
        get() = LocalTravelExtendedColors.current

    val corners: TravelCorners
        @Composable
        get() = LocalTravelCorners.current
}

