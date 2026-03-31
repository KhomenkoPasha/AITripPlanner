package khom.pavlo.aitripplanner

import android.content.Context
import khom.pavlo.aitripplanner.core.di.initKoin
import khom.pavlo.aitripplanner.core.platform.AndroidPlatformRuntime
import khom.pavlo.aitripplanner.core.platform.AndroidWorkManagerSyncScheduler
import khom.pavlo.aitripplanner.core.platform.PlatformPhotoStorageService
import khom.pavlo.aitripplanner.core.platform.PlatformThemeStore
import khom.pavlo.aitripplanner.sync.BackgroundSyncScheduler
import org.koin.dsl.module

fun initKoinAndroid(context: Context) {
    AndroidPlatformRuntime.install(context)
    initKoin(
        module {
            single<BackgroundSyncScheduler> { AndroidWorkManagerSyncScheduler() }
            single { PlatformPhotoStorageService() }
            single { PlatformThemeStore() }
        },
    )
}
