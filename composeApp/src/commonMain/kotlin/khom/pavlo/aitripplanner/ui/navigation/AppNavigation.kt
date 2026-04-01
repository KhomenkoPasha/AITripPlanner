package khom.pavlo.aitripplanner.ui.navigation

enum class BottomTab {
    CREATE_NEW_TRIP,
    MY_TRIPS,
    PROFILE,
}

enum class AuthRoute {
    LOGIN,
    REGISTER,
}

sealed interface PlannerMode {
    data object Create : PlannerMode
    data class Edit(val tripId: String) : PlannerMode
}

sealed interface AppRoute {
    data class Planner(val mode: PlannerMode) : AppRoute
    data object MyTrips : AppRoute
    data object Profile : AppRoute
    data class TripDetails(
        val tripId: String,
        val originTab: BottomTab,
    ) : AppRoute
    data class DayRouteMap(
        val tripId: String,
        val dayId: String,
        val originTab: BottomTab,
    ) : AppRoute
    data class PlaceDetails(
        val tripId: String,
        val dayId: String,
        val placeId: String,
        val originTab: BottomTab,
    ) : AppRoute
}

fun AppRoute.bottomTab(): BottomTab = when (this) {
    is AppRoute.Planner -> BottomTab.CREATE_NEW_TRIP
    AppRoute.MyTrips -> BottomTab.MY_TRIPS
    AppRoute.Profile -> BottomTab.PROFILE
    is AppRoute.TripDetails -> originTab
    is AppRoute.DayRouteMap -> originTab
    is AppRoute.PlaceDetails -> originTab
}
