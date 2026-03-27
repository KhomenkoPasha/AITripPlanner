package khom.pavlo.aitripplanner.ui.theme

import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

val WarmBackground = Color(0xFFF7F2EA)
val WarmBackgroundDeep = Color(0xFFEFE6D9)
val SurfacePrimary = Color(0xFFFFFCF7)
val SurfaceSecondary = Color(0xFFF5EEE3)
val SurfaceTertiary = Color(0xFFEDE4D8)
val InkPrimary = Color(0xFF241F1A)
val InkSecondary = Color(0xFF6F675F)
val InkMuted = Color(0xFF94897D)
val SandAccent = Color(0xFFC78F5D)
val SageAccent = Color(0xFF7B9382)
val SageSoft = Color(0xFFDCE7DF)
val RoseSoft = Color(0xFFEDD8D0)
val DividerSoft = Color(0xFFE7DDD1)
val ErrorSoft = Color(0xFFBE6A5E)

internal val AppColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = SurfacePrimary,
    primaryContainer = SurfaceSecondary,
    onPrimaryContainer = InkPrimary,
    secondary = SageAccent,
    onSecondary = SurfacePrimary,
    secondaryContainer = SageSoft,
    onSecondaryContainer = InkPrimary,
    tertiary = SandAccent,
    onTertiary = SurfacePrimary,
    tertiaryContainer = RoseSoft,
    onTertiaryContainer = InkPrimary,
    background = WarmBackground,
    onBackground = InkPrimary,
    surface = SurfacePrimary,
    onSurface = InkPrimary,
    surfaceVariant = SurfaceSecondary,
    onSurfaceVariant = InkSecondary,
    outline = DividerSoft,
    outlineVariant = SurfaceTertiary,
    error = ErrorSoft,
    onError = SurfacePrimary,
    errorContainer = Color(0xFFF6E1DB),
    onErrorContainer = InkPrimary,
)

