@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package khom.pavlo.aitripplanner.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import platform.Foundation.NSURL
import platform.Foundation.NSURLRequest
import platform.WebKit.WKWebView

@Composable
actual fun PlatformWebView(
    url: String,
    modifier: Modifier,
) {
    val webView = remember { WKWebView() }

    UIKitView(
        modifier = modifier,
        factory = { webView },
        update = { currentWebView ->
            val targetUrl = NSURL.URLWithString(url) ?: return@UIKitView
            if (currentWebView.URL?.absoluteString != url) {
                currentWebView.loadRequest(NSURLRequest.requestWithURL(targetUrl))
            }
        },
    )
}
