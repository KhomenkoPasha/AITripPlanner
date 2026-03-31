package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.domain.model.Budget
import khom.pavlo.aitripplanner.domain.model.CompanionType
import khom.pavlo.aitripplanner.domain.model.Interest
import khom.pavlo.aitripplanner.domain.model.Pace
import khom.pavlo.aitripplanner.domain.model.TravelMode
import khom.pavlo.aitripplanner.domain.model.TripPreference
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun QuickFiltersBlock(
    selectedInterests: List<Interest>,
    selectedPace: Pace?,
    selectedBudget: Budget?,
    selectedTravelMode: TravelMode,
    selectedCompanionType: CompanionType?,
    selectedPreferences: List<TripPreference>,
    withChildren: Boolean,
    interestOptions: List<Pair<Interest, String>>,
    paceOptions: List<Pair<Pace, String>>,
    budgetOptions: List<Pair<Budget, String>>,
    travelModeOptions: List<Pair<TravelMode, String>>,
    companionOptions: List<Pair<CompanionType, String>>,
    preferenceOptions: List<Pair<TripPreference, String>>,
    interestsTitle: String,
    paceTitle: String,
    budgetTitle: String,
    formatTitle: String,
    preferencesTitle: String,
    withChildrenLabel: String,
    helperText: String,
    onInterestToggle: (Interest) -> Unit,
    onPaceSelected: (Pace) -> Unit,
    onBudgetSelected: (Budget) -> Unit,
    onTravelModeSelected: (TravelMode) -> Unit,
    onCompanionTypeSelected: (CompanionType) -> Unit,
    onPreferenceToggle: (TripPreference) -> Unit,
    onWithChildrenToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
    ) {
        FilterGroup(
            title = interestsTitle,
            items = interestOptions.map { (value, label) ->
                FilterChipItem(
                    label = label,
                    selected = value in selectedInterests,
                    onClick = { onInterestToggle(value) },
                )
            },
        )
        FilterGroup(
            title = paceTitle,
            items = paceOptions.map { (value, label) ->
                FilterChipItem(
                    label = label,
                    selected = value == selectedPace,
                    onClick = { onPaceSelected(value) },
                )
            },
        )
        FilterGroup(
            title = budgetTitle,
            items = budgetOptions.map { (value, label) ->
                FilterChipItem(
                    label = label,
                    selected = value == selectedBudget,
                    onClick = { onBudgetSelected(value) },
                )
            },
        )
        FilterGroup(
            title = formatTitle,
            items = buildList {
                add(
                    FilterChipItem(
                        label = withChildrenLabel,
                        selected = withChildren,
                        onClick = onWithChildrenToggle,
                    ),
                )
                addAll(
                    travelModeOptions.map { (value, label) ->
                        FilterChipItem(
                            label = label,
                            selected = value == selectedTravelMode,
                            onClick = { onTravelModeSelected(value) },
                        )
                    },
                )
                addAll(
                    companionOptions.map { (value, label) ->
                        FilterChipItem(
                            label = label,
                            selected = value == selectedCompanionType,
                            onClick = { onCompanionTypeSelected(value) },
                        )
                    },
                )
            },
        )
        FilterGroup(
            title = preferencesTitle,
            items = preferenceOptions.map { (value, label) ->
                FilterChipItem(
                    label = label,
                    selected = value in selectedPreferences,
                    onClick = { onPreferenceToggle(value) },
                )
            },
        )
        Text(
            text = helperText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun FilterGroup(
    title: String,
    items: List<FilterChipItem>,
) {
    Column(verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs),
        ) {
            items.forEach { item ->
                SelectableFilterChip(
                    label = item.label,
                    selected = item.selected,
                    onClick = item.onClick,
                )
            }
        }
    }
}

private data class FilterChipItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun SelectableFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Text(
        text = label,
        modifier = Modifier
            .clip(TravelTheme.corners.medium)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.82f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.56f)
                },
                shape = TravelTheme.corners.medium,
            )
            .border(
                width = 1.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f)
                } else {
                    TravelTheme.extendedColors.cardStroke
                },
                shape = TravelTheme.corners.medium,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = TravelTheme.spacing.md, vertical = 10.dp),
        style = MaterialTheme.typography.labelLarge,
        color = if (selected) {
            MaterialTheme.colorScheme.onTertiaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
    )
}
