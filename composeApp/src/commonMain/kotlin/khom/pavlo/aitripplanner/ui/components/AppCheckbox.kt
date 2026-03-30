package khom.pavlo.aitripplanner.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    contentDescription: String,
    checkedStateLabel: String,
    uncheckedStateLabel: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val mutedAccent = TravelTheme.extendedColors.mutedAccent
    val cardStroke = TravelTheme.extendedColors.cardStroke
    val surface = MaterialTheme.colorScheme.surface
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val secondary = MaterialTheme.colorScheme.secondary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant

    val containerColor = animateColorAsState(targetValue = when {
        !enabled && checked -> mutedAccent.copy(alpha = 0.55f)
        !enabled -> surfaceVariant.copy(alpha = 0.45f)
        checked -> mutedAccent
        else -> surface.copy(alpha = 0.72f)
    }, label = "checkbox_container")
    val borderColor = animateColorAsState(targetValue = when {
        !enabled -> cardStroke.copy(alpha = 0.65f)
        checked -> secondary.copy(alpha = 0.65f)
        else -> cardStroke
    }, label = "checkbox_border")
    val iconTint = animateColorAsState(
        targetValue = if (enabled) secondary else onSurfaceVariant.copy(alpha = 0.55f),
        label = "checkbox_icon",
    )
    val boxScale = animateFloatAsState(
        targetValue = if (checked) 1f else 0.94f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.72f),
        label = "checkbox_scale",
    )

    Box(
        modifier = modifier
            .size(44.dp)
            .semantics {
                this.contentDescription = contentDescription
                this.stateDescription = if (checked) checkedStateLabel else uncheckedStateLabel
            }
            .toggleable(
                value = checked,
                enabled = enabled,
                role = Role.Checkbox,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onValueChange = { onCheckedChange?.invoke(it) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(boxScale.value)
                .size(26.dp)
                .background(
                    color = containerColor.value,
                    shape = TravelTheme.corners.small,
                )
                .border(
                    width = 1.dp,
                    color = borderColor.value,
                    shape = TravelTheme.corners.small,
                )
                .alpha(if (enabled) 1f else 0.76f),
            contentAlignment = Alignment.Center,
        ) {
            AnimatedVisibility(
                visible = checked,
                enter = fadeIn() + scaleIn(initialScale = 0.65f),
                exit = fadeOut() + scaleOut(targetScale = 0.7f),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = iconTint.value,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}
