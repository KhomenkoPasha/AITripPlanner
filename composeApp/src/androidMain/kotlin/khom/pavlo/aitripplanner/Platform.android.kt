package khom.pavlo.aitripplanner

import android.os.Build

private const val PRODUCTION_BACKEND_URL = "https://ktor-travel-backend-production.up.railway.app"

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()
actual fun localBackendBaseUrl(): String = PRODUCTION_BACKEND_URL
