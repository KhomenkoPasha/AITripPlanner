package khom.pavlo.aitripplanner.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun InAppWebViewDialog(
    url: String,
    title: String,
    closeLabel: String,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TravelTheme.extendedColors.backgroundTop,
                            TravelTheme.extendedColors.backgroundBottom,
                        ),
                    ),
                ),
        ) {
            PlatformWebView(
                url = url,
                modifier = Modifier.fillMaxSize(),
            )

            Text(
                text = title,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = TravelTheme.spacing.lg),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )

            SoftIconActionButton(
                imageVector = Icons.Outlined.Close,
                contentDescription = closeLabel,
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(TravelTheme.spacing.lg),
            )
        }
    }
}
