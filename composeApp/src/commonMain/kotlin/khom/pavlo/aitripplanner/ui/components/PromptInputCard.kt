package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PromptInputCard(
    city: String,
    tripTitle: String,
    summary: String,
    heroNote: String,
    cityLabel: String,
    tripTitleLabel: String,
    summaryLabel: String,
    heroNoteLabel: String,
    helperText: String,
    actionLabel: String,
    onCityChange: (String) -> Unit,
    onTripTitleChange: (String) -> Unit,
    onSummaryChange: (String) -> Unit,
    onHeroNoteChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    errorMessage: String? = null,
) {
    TravelCardSurface(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.xl),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
        ) {
            OutlinedTextField(
                value = city,
                onValueChange = onCityChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = cityLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = tripTitle,
                onValueChange = onTripTitleChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = tripTitleLabel) },
                singleLine = true,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = summary,
                onValueChange = onSummaryChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = summaryLabel) },
                minLines = 4,
                maxLines = 6,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            OutlinedTextField(
                value = heroNote,
                onValueChange = onHeroNoteChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = heroNoteLabel) },
                minLines = 2,
                maxLines = 3,
                enabled = enabled,
                shape = TravelTheme.corners.medium,
                colors = travelFieldColors(),
            )
            Text(
                text = errorMessage ?: helperText,
                style = MaterialTheme.typography.bodyMedium,
                color = if (errorMessage != null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            PrimaryActionButton(
                text = actionLabel,
                onClick = onSubmit,
                enabled = enabled && city.isNotBlank() && tripTitle.isNotBlank() && summary.isNotBlank(),
                isLoading = isLoading,
            )
        }
    }
}

@Composable
private fun travelFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = TravelTheme.extendedColors.cardStroke,
    cursorColor = MaterialTheme.colorScheme.primary,
)
