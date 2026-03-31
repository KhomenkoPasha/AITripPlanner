package khom.pavlo.aitripplanner.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
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

val NightBackground = Color(0xFF14110E)
val NightBackgroundDeep = Color(0xFF0D0B09)
val NightSurfacePrimary = Color(0xFF1C1814)
val NightSurfaceSecondary = Color(0xFF26201B)
val NightSurfaceTertiary = Color(0xFF312923)
val NightInkPrimary = Color(0xFFF4EDE3)
val NightInkSecondary = Color(0xFFD2C3B5)
val NightInkMuted = Color(0xFFA8998B)
val NightSandAccent = Color(0xFFE0AD7B)
val NightSageAccent = Color(0xFFA1B9A8)
val NightSageSoft = Color(0xFF334139)
val NightRoseSoft = Color(0xFF47352E)
val NightDividerSoft = Color(0xFF3A322C)
val NightErrorSoft = Color(0xFFFFB4A8)

internal val LightAppColorScheme = lightColorScheme(
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

internal val DarkAppColorScheme = darkColorScheme(
    primary = NightInkPrimary,
    onPrimary = NightBackground,
    primaryContainer = NightSurfaceTertiary,
    onPrimaryContainer = NightInkPrimary,
    secondary = NightSageAccent,
    onSecondary = NightBackground,
    secondaryContainer = NightSageSoft,
    onSecondaryContainer = NightInkPrimary,
    tertiary = NightSandAccent,
    onTertiary = NightBackground,
    tertiaryContainer = NightRoseSoft,
    onTertiaryContainer = NightInkPrimary,
    background = NightBackground,
    onBackground = NightInkPrimary,
    surface = NightSurfacePrimary,
    onSurface = NightInkPrimary,
    surfaceVariant = NightSurfaceSecondary,
    onSurfaceVariant = NightInkSecondary,
    outline = NightDividerSoft,
    outlineVariant = NightSurfaceTertiary,
    error = NightErrorSoft,
    onError = NightBackground,
    errorContainer = Color(0xFF5B2E26),
    onErrorContainer = Color(0xFFFFDBD5),
)

internal fun appColorScheme(darkTheme: Boolean): ColorScheme =
    if (darkTheme) DarkAppColorScheme else LightAppColorScheme
