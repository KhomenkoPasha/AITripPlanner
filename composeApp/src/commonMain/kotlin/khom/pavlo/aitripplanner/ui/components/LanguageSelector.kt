package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.domain.model.AppLanguage

@Composable
fun LanguageSelector(
    selectedLanguage: AppLanguage,
    label: String,
    onLanguageSelected: (AppLanguage) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    Row(modifier = modifier, horizontalArrangement = Arrangement.End) {
        FilledTonalButton(onClick = { expanded = true }) {
            Icon(
                imageVector = Icons.Outlined.Language,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Text(text = selectedLanguage.codeLabel())
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            AppLanguage.entries.forEach { language ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(text = language.codeLabel())
                            Text(text = language.displayName())
                        }
                    },
                    onClick = {
                        expanded = false
                        onLanguageSelected(language)
                    },
                )
            }
        }
    }
}

private fun AppLanguage.codeLabel(): String = when (this) {
    AppLanguage.EN -> "EN"
    AppLanguage.RU -> "RU"
    AppLanguage.UK -> "UK"
}

private fun AppLanguage.displayName(): String = when (this) {
    AppLanguage.EN -> "English"
    AppLanguage.RU -> "Русский"
    AppLanguage.UK -> "Українська"
}
