package khom.pavlo.aitripplanner

import android.app.Application
import khom.pavlo.aitripplanner.core.platform.PlatformThemeStore
import org.koin.core.context.GlobalContext
import org.osmdroid.config.Configuration
import java.io.File

class TravelPlannerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))
        Configuration.getInstance().userAgentValue = packageName
        val osmdroidBasePath = File(filesDir, "osmdroid")
        Configuration.getInstance().osmdroidBasePath = osmdroidBasePath
        Configuration.getInstance().osmdroidTileCache = File(osmdroidBasePath, "tiles")
        initKoinAndroid(this)
        GlobalContext.get().get<PlatformThemeStore>()
    }
}
