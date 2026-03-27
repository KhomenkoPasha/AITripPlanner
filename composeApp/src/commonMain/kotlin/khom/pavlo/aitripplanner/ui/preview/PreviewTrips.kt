package khom.pavlo.aitripplanner.ui.preview

import khom.pavlo.aitripplanner.presentation.saved.SavedTripsScreenState
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceUiModel
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.model.TripDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreenState
import khom.pavlo.aitripplanner.ui.screens.planner.PlannerScreenState

object PreviewTrips {
    val romeOverview = TripOverviewUiModel(
        id = "rome-spring",
        city = "Rome",
        title = "Rome city highlights",
        subtitle = "A calm 3-day route shaped around iconic sites, easy walks, and long golden-hour breaks.",
        daysLabel = "3 days",
        durationLabel = "16h 0m",
        distanceLabel = "12.4 km",
        syncLabel = "Pending sync",
    )

    private val florenceOverview = TripOverviewUiModel(
        id = "florence-weekend",
        city = "Florence",
        title = "Quiet art weekend",
        subtitle = "Gallery mornings, riverside walks, and two slow dinners in the historic center.",
        daysLabel = "2 days",
        durationLabel = "9h 0m",
        distanceLabel = "6.8 km",
        syncLabel = "Synced",
    )

    private val lisbonOverview = TripOverviewUiModel(
        id = "lisbon-coast",
        city = "Lisbon",
        title = "Sunlit neighborhood loop",
        subtitle = "Trams, tiled streets, and a gentle mix of viewpoints, cafes, and local food spots.",
        daysLabel = "4 days",
        durationLabel = "18h 0m",
        distanceLabel = "15.2 km",
        syncLabel = "Synced",
    )

    val romePlacesDay1 = listOf(
        PlaceUiModel("Colosseum", "Piazza del Colosseo, 1", "1h 30m"),
        PlaceUiModel("Roman Forum", "Via della Salara Vecchia, 5/6", "1h 15m"),
        PlaceUiModel("Pantheon", "Piazza della Rotonda", "45m"),
        PlaceUiModel("Trevi Fountain", "Piazza di Trevi", "30m"),
        PlaceUiModel("Piazza Navona", "Piazza Navona", "50m"),
    )

    val romeDayCards = listOf(
        DayItineraryUiModel(
            id = "day-1",
            dayLabel = "Day 1",
            title = "Ancient core and evening squares",
            subtitle = "Historic landmarks balanced with short walking segments and space for breaks.",
            durationLabel = "5h 10m",
            distanceLabel = "4.2 km",
            isExpanded = true,
            places = romePlacesDay1,
        ),
        DayItineraryUiModel(
            id = "day-2",
            dayLabel = "Day 2",
            title = "Villa gardens and neighborhood cafes",
            subtitle = "A slower route through green spaces, museums, and a long lunch around Flaminio.",
            durationLabel = "4h 35m",
            distanceLabel = "3.1 km",
            isExpanded = false,
            places = listOf(
                PlaceUiModel("Villa Borghese", "Piazzale Napoleone I", "1h 20m"),
                PlaceUiModel("Galleria Borghese", "Piazzale Scipione Borghese, 5", "1h 30m"),
                PlaceUiModel("Spanish Steps", "Piazza di Spagna", "35m"),
                PlaceUiModel("Via Margutta", "Via Margutta", "30m"),
            ),
        ),
    )

    val romeDetails = TripDetailsUiModel(
        id = romeOverview.id,
        city = "Rome",
        subtitle = "Classic monuments, soft neighborhood pacing, and enough space to enjoy the city instead of rushing through it.",
        heroNote = "Future-ready hero slot for richer branded motion and map preview blocks.",
        syncLabel = "Offline draft",
        summary = RouteSummaryUiModel(
            durationLabel = "16h 0m",
            distanceLabel = "12.4 km",
            paceLabel = "Easy pace",
        ),
        days = romeDayCards,
    )

    fun plannerState() = PlannerScreenState(
        mode = PlannerMode.Create,
        city = "Rome",
        title = "Rome city highlights",
        summary = "Three calm days with iconic monuments, gentle walks, coffee stops, and sunset viewpoints.",
        heroNote = "Soft pacing, warm light, and enough space for flexible edits later.",
        helperText = "City, title, and summary are required.",
        syncStatusLabel = "2 changes queued",
        currentTrip = romeOverview,
        savedTrips = listOf(romeOverview, florenceOverview, lisbonOverview),
    )

    fun plannerEditState() = plannerState().copy(
        mode = PlannerMode.Edit(romeOverview.id),
    )

    fun detailsState() = TripDetailsScreenState(
        trip = romeDetails,
        syncStatusLabel = "Syncing 1 change",
    )

    fun detailsDeletingState() = detailsState().copy(isDeleting = true)

    fun savedTripsState() = SavedTripsScreenState(
        isLoading = false,
        syncStatusLabel = "Everything synced",
        trips = listOf(romeOverview, florenceOverview, lisbonOverview),
    )
}
