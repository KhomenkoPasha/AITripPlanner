package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun PlaceDescriptionSection(
    title: String,
    text: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    TravelCardSurface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                TextButton(
                    onClick = onActionClick,
                    contentPadding = PaddingValues(0.dp),
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textDecoration = TextDecoration.Underline,
                    )
                }
            }
        }
    }
}
