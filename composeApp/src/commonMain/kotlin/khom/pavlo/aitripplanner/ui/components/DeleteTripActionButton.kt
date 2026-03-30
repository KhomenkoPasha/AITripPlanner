package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun DeleteTripActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    enabled: Boolean = true,
) {
    val destructiveColor = MaterialTheme.colorScheme.error.copy(alpha = 0.84f)

    OutlinedButton(
        modifier = modifier.defaultMinSize(minHeight = 50.dp),
        onClick = onClick,
        enabled = enabled && !isLoading,
        shape = TravelTheme.corners.medium,
        border = BorderStroke(
            width = 1.dp,
            color = destructiveColor.copy(alpha = 0.18f),
        ),
        contentPadding = PaddingValues(
            horizontal = 14.dp,
            vertical = 10.dp,
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.36f),
            contentColor = destructiveColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            disabledContentColor = destructiveColor.copy(alpha = 0.45f),
        ),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = destructiveColor,
                )
            } else {
                Icon(
                    modifier = Modifier.size(16.dp),
                    imageVector = Icons.Outlined.DeleteOutline,
                    contentDescription = null,
                    tint = destructiveColor,
                )
            }
            Text(
                text = text,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
                color = destructiveColor,
            )
        }
    }
}
