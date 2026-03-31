@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package khom.pavlo.aitripplanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.NSUUID
import platform.Foundation.pathExtension
import platform.PhotosUI.PHPickerConfiguration
import platform.PhotosUI.PHPickerFilter
import platform.PhotosUI.PHPickerResult
import platform.PhotosUI.PHPickerViewController
import platform.PhotosUI.PHPickerViewControllerDelegateProtocol
import platform.darwin.NSObject

@Composable
actual fun rememberPlatformPhotoPickerLauncher(
    onPhotoPicked: (String) -> Unit,
    onError: (String) -> Unit,
): PlatformPhotoPickerLauncher {
    val viewController = LocalUIViewController.current
    val currentOnPhotoPicked = rememberUpdatedState(onPhotoPicked)
    val currentOnError = rememberUpdatedState(onError)

    return remember(viewController) {
        IOSPlatformPhotoPickerLauncher(
            viewController = viewController,
            onPhotoPicked = { currentOnPhotoPicked.value(it) },
            onError = { currentOnError.value(it) },
        )
    }
}

private class IOSPlatformPhotoPickerLauncher(
    private val viewController: platform.UIKit.UIViewController,
    private val onPhotoPicked: (String) -> Unit,
    private val onError: (String) -> Unit,
) : PlatformPhotoPickerLauncher {
    private var delegate: IOSPhotoPickerDelegate? = null

    override fun launch() {
        val configuration = PHPickerConfiguration().apply {
            selectionLimit = 1
            filter = PHPickerFilter.imagesFilter()
        }
        val picker = PHPickerViewController(configuration)
        val pickerDelegate = IOSPhotoPickerDelegate(
            onPhotoPicked = onPhotoPicked,
            onError = onError,
        )
        delegate = pickerDelegate
        picker.delegate = pickerDelegate
        viewController.presentViewController(
            viewControllerToPresent = picker,
            animated = true,
            completion = null,
        )
    }
}

private class IOSPhotoPickerDelegate(
    private val onPhotoPicked: (String) -> Unit,
    private val onError: (String) -> Unit,
) : NSObject(), PHPickerViewControllerDelegateProtocol {
    override fun picker(
        picker: PHPickerViewController,
        didFinishPicking: List<*>,
    ) {
        picker.dismissViewControllerAnimated(true, completion = null)
        val result = didFinishPicking.firstOrNull() as? PHPickerResult ?: return
        val provider = result.itemProvider
        provider.loadFileRepresentationForTypeIdentifier("public.image") { url, error ->
            when {
                url == null -> onError(error?.localizedDescription ?: "Unable to import selected photo")
                else -> {
                    val temporaryUrl = copyToTemporaryLocation(url)
                    if (temporaryUrl != null) {
                        onPhotoPicked(temporaryUrl.absoluteString ?: temporaryUrl.path ?: "")
                    } else {
                        onError("Unable to prepare selected photo")
                    }
                }
            }
        }
    }

    private fun copyToTemporaryLocation(sourceUrl: NSURL): NSURL? {
        val tempDirectory = NSTemporaryDirectory()
        val extension = sourceUrl.pathExtension?.takeIf { it.isNotBlank() } ?: "jpg"
        val targetPath = "$tempDirectory/${NSUUID.UUID().UUIDString}.$extension"
        val targetUrl = NSURL.fileURLWithPath(targetPath)
        val fileManager = NSFileManager.defaultManager

        if (fileManager.fileExistsAtPath(targetPath)) {
            fileManager.removeItemAtURL(targetUrl, error = null)
        }

        return if (fileManager.copyItemAtURL(sourceUrl, targetUrl, error = null)) targetUrl else null
    }
}
