package khom.pavlo.aitripplanner.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformPhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit,
    onError: (String) -> Unit,
): PlatformPhotoPickerLauncher {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        runCatching {
            uri?.toString()?.let(onPhotoPicked)
        }.onFailure { error ->
            onError(error.message ?: "Unable to open photo picker")
        }
    }

    return remember(launcher) {
        object : PlatformPhotoPickerLauncher {
            override fun launch() {
                launcher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
            }
        }
    }
}
