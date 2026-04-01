package khom.pavlo.aitripplanner.ui.screens.place

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.core.platform.PlatformMapLauncher
import khom.pavlo.aitripplanner.ui.components.EmptyStateView
import khom.pavlo.aitripplanner.ui.components.ErrorStateView
import khom.pavlo.aitripplanner.ui.components.InAppWebViewDialog
import khom.pavlo.aitripplanner.ui.components.LoadingStateView
import khom.pavlo.aitripplanner.ui.components.PlaceActionsBar
import khom.pavlo.aitripplanner.ui.components.PlaceDescriptionSection
import khom.pavlo.aitripplanner.ui.components.PlaceGallerySection
import khom.pavlo.aitripplanner.ui.components.PlaceHeroImage
import khom.pavlo.aitripplanner.ui.components.PlaceLocalPhotosSection
import khom.pavlo.aitripplanner.ui.components.PlaceMapPreviewCard
import khom.pavlo.aitripplanner.ui.components.PlaceSummaryChips
import khom.pavlo.aitripplanner.ui.components.PlatformPlaceMap
import khom.pavlo.aitripplanner.ui.components.RouteContextCard
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.rememberPlatformPhotoPickerLauncher
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceDetailsScreen(
    state: PlaceDetailsScreenState,
    onBackClick: () -> Unit,
    onVisitedChange: (Boolean) -> Unit,
    onPhotoPicked: (String) -> Unit,
    onDeletePhoto: (String) -> Unit,
    onPhotoPickerError: (String) -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    val strings = appStrings()
    var websiteUrlToOpen by remember { mutableStateOf<String?>(null) }
    val photoPickerLauncher = rememberPlatformPhotoPickerLauncher(
        onPhotoPicked = onPhotoPicked,
        onError = onPhotoPickerError,
    )

    TravelAppScaffold(
        modifier = modifier,
        bottomBar = bottomBar,
    ) { innerPadding ->
        when {
            state.isLoading && state.place == null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(TravelTheme.spacing.lg),
                ) {
                    item {
                        LoadingStateView(
                            title = strings.placeDetailsLoadingTitle,
                            subtitle = strings.placeDetailsLoadingSubtitle,
                        )
                    }
                }
            }

            state.errorMessage != null && state.place == null -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(TravelTheme.spacing.lg),
                ) {
                    item {
                        ErrorStateView(
                            title = strings.placeDetailsLoadErrorTitle,
                            subtitle = state.errorMessage,
                        )
                    }
                }
            }

            state.place != null -> {
                val place = state.place
                val latitude = place.latitude
                val longitude = place.longitude
                val showOnMapAction: (() -> Unit)? = if (latitude != null && longitude != null) {
                    {
                        PlatformMapLauncher.showOnMap(
                            label = place.title,
                            latitude = latitude,
                            longitude = longitude,
                        )
                        Unit
                    }
                } else {
                    null
                }
                val openInMapsAction: (() -> Unit)? = if (latitude != null && longitude != null) {
                    {
                        PlatformMapLauncher.openInMaps(
                            label = place.title,
                            latitude = latitude,
                            longitude = longitude,
                        )
                        Unit
                    }
                } else {
                    null
                }
                val openWebsiteAction: (() -> Unit)? = place.websiteUrl
                    ?.normalizeWebsiteUrl()
                    ?.let { websiteUrl ->
                        {
                            websiteUrlToOpen = websiteUrl
                        }
                    }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(
                        start = TravelTheme.spacing.lg,
                        top = TravelTheme.spacing.md,
                        end = TravelTheme.spacing.lg,
                        bottom = TravelTheme.spacing.xxl,
                    ),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                ) {
                    item {
                        PlaceHeroImage(
                            title = place.title,
                            subtitle = place.address,
                            imageUrl = place.heroImageUrl,
                            statusLabel = place.statusLabel,
                            dayLabel = place.dayLabel,
                            photoContentDescription = strings.placePhotoContentDescription,
                            photoAttributionPrefix = strings.placePhotoAttributionPrefix,
                            photoAttribution = place.heroImageAttribution,
                            backActionLabel = strings.backAction,
                            onBackClick = onBackClick,
                        )
                    }
                    item {
                        val summaryChips = buildList {
                            add(place.visitTimeLabel)
                            add(place.routeContext.stopLabel)
                            place.categoryLabel?.let(::add)
                            place.openingStatusLabel?.let(::add)
                            add(place.bestTimeLabel)
                            place.neighborhoodLabel?.let(::add)
                            place.priceLabel?.let(::add)
                        }
                        PlaceSummaryChips(
                            chips = summaryChips,
                        )
                    }
                    state.actionErrorMessage?.let { message ->
                        item {
                            ErrorStateView(
                                title = strings.placeDetailsActionErrorTitle,
                                subtitle = message,
                            )
                        }
                    }
                    item {
                        RouteContextCard(
                            routeContext = place.routeContext,
                            title = strings.placeRouteContextTitle,
                            subtitle = strings.placeRouteContextSubtitle,
                            previousStopLabel = strings.placePreviousStopLabel,
                            nextStopLabel = strings.placeNextStopLabel,
                            noPreviousStopLabel = strings.placeNoPreviousStopLabel,
                            noNextStopLabel = strings.placeNoNextStopLabel,
                        )
                    }
                    item {
                        PlaceDescriptionSection(
                            title = strings.placeAboutTitle,
                            text = place.aboutText,
                        )
                    }
                    if (place.visitDetailsText.isNotBlank()) {
                        item {
                            PlaceDescriptionSection(
                                title = strings.placeVisitDetailsTitle,
                                text = place.visitDetailsText,
                                actionLabel = place.websiteUrl?.normalizeWebsiteUrl()?.webViewTitle(),
                                onActionClick = place.websiteUrl
                                    ?.normalizeWebsiteUrl()
                                    ?.let { websiteUrl ->
                                        {
                                            websiteUrlToOpen = websiteUrl
                                        }
                                    },
                            )
                        }
                    }
                    item {
                        PlaceDescriptionSection(
                            title = strings.placeWhyTitle,
                            text = place.whyInRouteText,
                        )
                    }
                    item {
                        PlaceDescriptionSection(
                            title = strings.placeTipsTitle,
                            text = place.tipsText,
                        )
                    }
                    item {
                        PlaceLocalPhotosSection(
                            title = strings.placeLocalPhotosTitle,
                            subtitle = strings.placeLocalPhotosSubtitle,
                            addPhotoLabel = strings.placeAddPhotoAction,
                            emptyTitle = strings.placeLocalPhotosEmptyTitle,
                            emptySubtitle = strings.placeLocalPhotosEmptySubtitle,
                            photoContentDescription = strings.placePhotoContentDescription,
                            deletePhotoContentDescription = strings.placeDeletePhotoAction,
                            photos = state.photos,
                            deletingPhotoIds = state.deletingPhotoIds,
                            isAddingPhoto = state.isAddingPhoto,
                            onAddPhotoClick = { photoPickerLauncher.launch() },
                            onDeletePhotoClick = onDeletePhoto,
                        )
                    }
                    item {
                        PlaceGallerySection(
                            title = strings.placeGalleryTitle,
                            subtitle = strings.placeGallerySubtitle,
                            images = place.gallery,
                            photoContentDescription = strings.placePhotoContentDescription,
                            placeholderLabel = strings.placeGalleryPlaceholderLabel,
                            photoAttributionPrefix = strings.placePhotoAttributionPrefix,
                        )
                    }
                    item {
                        PlaceMapPreviewCard(
                            title = strings.placeMapTitle,
                            subtitle = strings.placeMapSubtitle,
                            placeholderLabel = strings.placeMapPlaceholderLabel,
                            showOnMapLabel = strings.showOnMapAction,
                            openInMapsLabel = strings.openInMapsAction,
                            onShowOnMap = showOnMapAction,
                            onOpenInMaps = openInMapsAction,
                            mapContent = if (latitude != null && longitude != null) {
                                {
                                    PlatformPlaceMap(
                                        latitude = latitude,
                                        longitude = longitude,
                                        label = place.title,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                }
                            } else {
                                null
                            },
                        )
                    }
                    item {
                        PlaceActionsBar(
                            markVisitedLabel = if (place.isCompleted) {
                                strings.placeVisitedAction
                            } else {
                                strings.placeMarkVisitedAction
                            },
                            showOnMapLabel = strings.showOnMapAction,
                            openWebsiteLabel = strings.openWebsiteAction,
                            removeLabel = strings.placeRemoveAction,
                            replaceLabel = strings.placeReplaceAction,
                            isVisited = place.isCompleted,
                            onToggleVisited = { onVisitedChange(!place.isCompleted) },
                            onShowOnMap = showOnMapAction,
                            onOpenWebsite = openWebsiteAction,
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(TravelTheme.spacing.lg),
                ) {
                    item {
                        EmptyStateView(
                            title = strings.placeDetailsEmptyTitle,
                            subtitle = strings.placeDetailsEmptySubtitle,
                        )
                    }
                }
            }
        }
    }

    websiteUrlToOpen?.let { websiteUrl ->
        InAppWebViewDialog(
            url = websiteUrl,
            title = websiteUrl.webViewTitle(),
            closeLabel = strings.cancelAction,
            onDismiss = { websiteUrlToOpen = null },
        )
    }
}

private fun String.normalizeWebsiteUrl(): String {
    val trimmed = trim()
    if (trimmed.isBlank()) return trimmed
    return if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
        trimmed
    } else {
        "https://$trimmed"
    }
}

private fun String.webViewTitle(): String = removePrefix("https://")
    .removePrefix("http://")
    .substringBefore('/')

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun PlaceDetailsScreenPreview() {
    AppTheme {
        PlaceDetailsScreen(
            state = PreviewTrips.placeDetailsState(),
            onBackClick = {},
            onVisitedChange = {},
            onPhotoPicked = {},
            onDeletePhoto = {},
            onPhotoPickerError = {},
        )
    }
}
