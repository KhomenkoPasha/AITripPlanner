package khom.pavlo.aitripplanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun PlatformPlaceMap(
    latitude: Double,
    longitude: Double,
    label: String,
    modifier: Modifier = Modifier,
)
