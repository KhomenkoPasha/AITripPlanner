package khom.pavlo.aitripplanner.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
internal fun authFieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.secondary,
    unfocusedBorderColor = TravelTheme.extendedColors.cardStroke,
    cursorColor = MaterialTheme.colorScheme.primary,
)

@Composable
internal fun AuthSuccessNotice(
    message: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = TravelTheme.extendedColors.successTint,
                shape = TravelTheme.corners.large,
            )
            .padding(TravelTheme.spacing.lg),
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
