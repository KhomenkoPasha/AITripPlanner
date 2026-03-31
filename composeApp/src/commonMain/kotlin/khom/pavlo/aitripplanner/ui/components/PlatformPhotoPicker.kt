package khom.pavlo.aitripplanner.ui.components

import androidx.compose.runtime.Composable

interface PlatformPhotoPickerLauncher {
    fun launch()
}

@Composable
expect fun rememberPlatformPhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit,
    onError: (String) -> Unit,
): PlatformPhotoPickerLauncher
