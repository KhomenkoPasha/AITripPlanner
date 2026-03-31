package khom.pavlo.aitripplanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.ui.model.DayRouteStopUiModel

@Composable
expect fun PlatformDayRouteMap(
    stops: List<DayRouteStopUiModel>,
    modifier: Modifier = Modifier,
)
