package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.domain.model.CitySuggestion
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun CitySuggestionsDropdown(
    suggestions: List<CitySuggestion>,
    isLoading: Boolean,
    loadingLabel: String,
    emptyLabel: String,
    onSuggestionClick: (CitySuggestion) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!isLoading && suggestions.isEmpty()) {
        return
    }

    TravelCardSurface(modifier = modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(TravelTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(start = 2.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = loadingLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (suggestions.isEmpty()) {
                Text(
                    text = emptyLabel,
                    modifier = Modifier.padding(TravelTheme.spacing.md),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                suggestions.forEachIndexed { index, suggestion ->
                    if (index > 0) {
                        HorizontalDivider(color = TravelTheme.extendedColors.cardStroke)
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionClick(suggestion) }
                            .padding(TravelTheme.spacing.md),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xxs),
                    ) {
                        Text(
                            text = suggestion.primaryText,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (suggestion.secondaryText.isNotBlank()) {
                            Text(
                                text = suggestion.secondaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}
