package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.SettingsBrightness
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun ThemeSelector(
    selectedTheme: AppThemeMode,
    label: String,
    systemLabel: String,
    lightLabel: String,
    darkLabel: String,
    onThemeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        FilledTonalIconButton(
            onClick = { expanded = true },
            modifier = Modifier.clip(TravelTheme.corners.medium),
            shape = TravelTheme.corners.medium,
        ) {
            Icon(
                imageVector = selectedTheme.icon(),
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AppThemeMode.entries.forEach { theme ->
                DropdownMenuItem(
                    text = { Text(text = theme.label(systemLabel, lightLabel, darkLabel)) },
                    leadingIcon = {
                        Icon(
                            imageVector = theme.icon(),
                            contentDescription = null,
                        )
                    },
                    onClick = {
                        expanded = false
                        onThemeSelected(theme)
                    },
                )
            }
        }
    }
}

private fun AppThemeMode.label(
    systemLabel: String,
    lightLabel: String,
    darkLabel: String,
): String = when (this) {
    AppThemeMode.SYSTEM -> systemLabel
    AppThemeMode.LIGHT -> lightLabel
    AppThemeMode.DARK -> darkLabel
}

@Composable
private fun AppThemeMode.icon() = when (this) {
    AppThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
    AppThemeMode.LIGHT -> Icons.Outlined.LightMode
    AppThemeMode.DARK -> Icons.Outlined.DarkMode
}
