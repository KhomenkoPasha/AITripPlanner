package khom.pavlo.aitripplanner.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun DeleteTripConfirmationDialog(
    visible: Boolean,
    title: String,
    message: String,
    confirmLabel: String,
    cancelLabel: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    if (!visible) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = message) },
        confirmButton = {
            DeleteTripActionButton(
                text = confirmLabel,
                onClick = onConfirm,
            )
        },
        dismissButton = {
            EditTripActionButton(
                text = cancelLabel,
                onClick = onDismiss,
            )
        },
    )
}
