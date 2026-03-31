package khom.pavlo.aitripplanner.ui.screens.dayroute

import aitripplanner.composeapp.generated.resources.Res
import aitripplanner.composeapp.generated.resources.places_count_label
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.LazyColumn
import khom.pavlo.aitripplanner.ui.components.EmptyStateView
import khom.pavlo.aitripplanner.ui.components.LoadingStateView
import khom.pavlo.aitripplanner.ui.components.PlatformDayRouteMap
import khom.pavlo.aitripplanner.ui.components.RouteSummaryRow
import khom.pavlo.aitripplanner.ui.components.SectionHeader
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.TravelCardSurface
import khom.pavlo.aitripplanner.ui.components.TravelInfoChip
import khom.pavlo.aitripplanner.ui.model.RouteSummaryUiModel
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun DayRouteMapScreen(
    state: DayRouteMapScreenState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    val strings = appStrings()

    TravelAppScaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.lg,
                        vertical = TravelTheme.spacing.sm,
                    ),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = onBackClick,
                    modifier = Modifier.clip(TravelTheme.corners.large),
                    shape = TravelTheme.corners.large,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = strings.backAction,
                    )
                    Text(text = strings.backAction)
                }
                TravelInfoChip(text = strings.dayRouteMapTitle)
            }
        },
        bottomBar = bottomBar,
    ) { innerPadding ->
        when {
            state.isLoading -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(TravelTheme.spacing.lg),
                ) {
                    item {
                        LoadingStateView(
                            title = strings.dayRouteMapTitle,
                            subtitle = strings.dayRouteMapSubtitle,
                        )
                    }
                }
            }

            state.route == null || state.route.stops.isEmpty() -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(TravelTheme.spacing.lg),
                ) {
                    item {
                        EmptyStateView(
                            title = strings.dayRouteMapEmptyTitle,
                            subtitle = strings.dayRouteMapEmptySubtitle,
                        )
                    }
                }
            }

            else -> {
                val route = state.route
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(
                                start = TravelTheme.spacing.lg,
                                top = TravelTheme.spacing.md,
                                end = TravelTheme.spacing.lg,
                                bottom = TravelTheme.spacing.xxl,
                            ),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
                    ) {
                        TravelCardSurface {
                            Column(
                                modifier = Modifier.padding(TravelTheme.spacing.xl),
                                verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
                                    ) {
                                        Text(
                                            text = route.dayLabel,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.tertiary,
                                        )
                                        Text(
                                            text = route.title,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = strings.dayRouteMapSubtitle,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.78f))
                                            .border(
                                                width = 1.dp,
                                                color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.14f),
                                                shape = CircleShape,
                                            )
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Map,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                        )
                                    }
                                }
                                RouteSummaryRow(
                                    summary = RouteSummaryUiModel(
                                        durationLabel = route.durationLabel,
                                        distanceLabel = route.distanceLabel,
                                        paceLabel = stringResource(Res.string.places_count_label, route.stops.size),
                                    ),
                                )
                            }
                        }
                        TravelCardSurface {
                            Column(
                                modifier = Modifier.padding(TravelTheme.spacing.lg),
                                verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                            ) {
                                SectionHeader(
                                    title = strings.dayRouteMapTitle,
                                    subtitle = route.city,
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(360.dp)
                                        .clip(TravelTheme.corners.large),
                                ) {
                                    PlatformDayRouteMap(
                                        stops = route.stops,
                                        modifier = Modifier.fillMaxSize(),
                                    )
                                }
                            }
                        }
                        SectionHeader(
                            title = strings.dayRouteMapStopsTitle,
                            subtitle = strings.dayRouteMapStopsSubtitle,
                        )
                        route.stops.forEach { stop ->
                            TravelCardSurface {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(TravelTheme.spacing.lg),
                                    horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                                    verticalAlignment = Alignment.Top,
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary)
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text(
                                            text = stop.numberLabel,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MaterialTheme.colorScheme.onPrimary,
                                        )
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        Text(
                                            text = stop.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                        Text(
                                            text = stop.address,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
