package khom.pavlo.aitripplanner

import platform.UIKit.UIDevice

private const val PRODUCTION_BACKEND_URL = "https://ktor-travel-backend-production.up.railway.app"

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()
actual fun localBackendBaseUrl(): String = PRODUCTION_BACKEND_URL
