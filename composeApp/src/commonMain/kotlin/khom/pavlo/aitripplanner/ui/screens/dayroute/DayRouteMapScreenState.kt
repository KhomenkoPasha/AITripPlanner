package khom.pavlo.aitripplanner.ui.screens.dayroute

import androidx.compose.runtime.Immutable
import khom.pavlo.aitripplanner.ui.model.DayRouteMapUiModel

@Immutable
data class DayRouteMapScreenState(
    val route: DayRouteMapUiModel? = null,
    val isLoading: Boolean = false,
)
