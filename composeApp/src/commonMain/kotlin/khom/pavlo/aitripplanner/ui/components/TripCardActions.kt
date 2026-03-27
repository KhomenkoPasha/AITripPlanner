package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun TripCardActions(
    editLabel: String,
    deleteLabel: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
    isDeleting: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
    ) {
        EditTripActionButton(
            text = editLabel,
            onClick = onEditClick,
            enabled = !isDeleting,
        )
        DeleteTripActionButton(
            text = deleteLabel,
            onClick = onDeleteClick,
            isLoading = isDeleting,
        )
    }
}
