package khom.pavlo.aitripplanner.ui.preview

import khom.pavlo.aitripplanner.presentation.saved.SavedTripsScreenState
import khom.pavlo.aitripplanner.ui.model.DayItineraryUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceGalleryImageUiModel
import khom.pavlo.aitripplanner.ui.model.PlaceUiModel
import khom.pavlo.aitripplanner.ui.model.RouteContextUiModel
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.model.TripDetailsUiModel
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.navigation.PlannerMode
import khom.pavlo.aitripplanner.ui.screens.details.TripDetailsScreenState
import khom.pavlo.aitripplanner.ui.screens.place.PlaceDetailsScreenState
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
        isFavorite = true,
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
        isFavorite = false,
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
        isFavorite = true,
    )

    val romePlacesDay1 = listOf(
        PlaceUiModel("colosseum", "Colosseum", "Piazza del Colosseo, 1", "1h 30m", "Historic amphitheater and one of Rome's core landmarks.", "https://images.unsplash.com/photo-1552832230-c0197dd311b5?auto=format&fit=crop&w=1200&q=80", "Unsplash", true),
        PlaceUiModel("roman-forum", "Roman Forum", "Via della Salara Vecchia, 5/6", "1h 15m", "Ruins and public spaces from ancient Rome.", null, null, false),
        PlaceUiModel("pantheon", "Pantheon", "Piazza della Rotonda", "45m", "Iconic dome, lively square, and strong architectural contrast.", "https://images.unsplash.com/photo-1529154036614-a60975f5c760?auto=format&fit=crop&w=1200&q=80", "Unsplash", false),
        PlaceUiModel("trevi-fountain", "Trevi Fountain", "Piazza di Trevi", "30m", "Short scenic stop in a dense historic area.", null, null, false),
        PlaceUiModel("piazza-navona", "Piazza Navona", "Piazza Navona", "50m", "Baroque square with cafes and a slower evening rhythm.", null, null, false),
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
            hasRouteMap = true,
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
            hasRouteMap = false,
            places = listOf(
                PlaceUiModel("villa-borghese", "Villa Borghese", "Piazzale Napoleone I", "1h 20m", "Open green space with broad walking paths.", null, null, false),
                PlaceUiModel("galleria-borghese", "Galleria Borghese", "Piazzale Scipione Borghese, 5", "1h 30m", "Focused museum visit with a denser cultural stop.", null, null, false),
                PlaceUiModel("spanish-steps", "Spanish Steps", "Piazza di Spagna", "35m", "Compact scenic landmark in the central district.", null, null, false),
                PlaceUiModel("via-margutta", "Via Margutta", "Via Margutta", "30m", "Quiet street for a slower finish to the day.", null, null, false),
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

    val pantheonDetails = PlaceDetailsUiModel(
        tripId = romeOverview.id,
        dayId = "day-1",
        placeId = "pantheon",
        title = "Pantheon",
        address = "Piazza della Rotonda, Rome",
        city = "Rome",
        dayLabel = "Day 1",
        dayTitle = "Ancient core and evening squares",
        visitTimeLabel = "1h 15m",
        bestTimeLabel = "Best time: Midday",
        statusLabel = "Planned",
        categoryLabel = "Landmark",
        openingStatusLabel = "Open now",
        neighborhoodLabel = "Pigna",
        priceLabel = "Ticketed",
        isCompleted = false,
        latitude = 41.8986,
        longitude = 12.4769,
        heroImageUrl = "https://images.unsplash.com/photo-1529154036614-a60975f5c760?auto=format&fit=crop&w=1200&q=80",
        heroImageAttribution = "Unsplash",
        gallery = listOf(
            PlaceGalleryImageUiModel(
                id = "pantheon-1",
                imageUrl = "https://images.unsplash.com/photo-1529154036614-a60975f5c760?auto=format&fit=crop&w=1200&q=80",
                attribution = "Unsplash",
            ),
            PlaceGalleryImageUiModel(
                id = "pantheon-2",
                imageUrl = null,
                attribution = null,
            ),
            PlaceGalleryImageUiModel(
                id = "pantheon-3",
                imageUrl = null,
                attribution = null,
            ),
        ),
        aboutText = "Historic dome, dense urban context, and one of the strongest atmosphere shifts inside the Rome route.",
        whyInRouteText = "This stop anchors the middle of the day and keeps the walking flow balanced between heavier landmarks and quieter square time.",
        tipsText = "- Arrive before the square gets busy.\n- Keep extra time for the surrounding streets.",
        visitDetailsText = "Hours: Usually daytime entry with seasonal closing times.\nNeighborhood: Pigna\nWebsite: https://pantheonroma.com",
        websiteUrl = "https://pantheonroma.com",
        routeContext = RouteContextUiModel(
            dayLabel = "Day 1",
            dayTitle = "Ancient core and evening squares",
            stopLabel = "Stop 3 of 5",
            previousPlaceName = "Roman Forum",
            nextPlaceName = "Trevi Fountain",
        ),
    )

    fun plannerState() = PlannerScreenState(
        mode = PlannerMode.Create,
        city = "Rome",
        title = "Rome city highlights",
        prompt = "Three calm days with iconic monuments, gentle walks, coffee stops, and sunset viewpoints.",
        days = "3",
        placeCount = "4",
        walkingMinutesPerDay = "180",
        heroNote = "Soft pacing, warm light, and enough space for flexible edits later.",
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

    fun placeDetailsState() = PlaceDetailsScreenState(
        place = pantheonDetails,
    )

    fun savedTripsState() = SavedTripsScreenState(
        isLoading = false,
        syncStatusLabel = "Everything synced",
        trips = listOf(romeOverview, florenceOverview, lisbonOverview),
    )
}
