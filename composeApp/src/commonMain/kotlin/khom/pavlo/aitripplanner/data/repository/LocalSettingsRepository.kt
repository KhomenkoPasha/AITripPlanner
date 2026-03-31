package khom.pavlo.aitripplanner.data.repository

import khom.pavlo.aitripplanner.core.platform.PlatformThemeStore
import khom.pavlo.aitripplanner.data.local.TripLocalDataSource
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class LocalSettingsRepository(
    private val localDataSource: TripLocalDataSource,
    private val platformThemeStore: PlatformThemeStore,
) : SettingsRepository {
    override fun observeAppLanguage(): Flow<AppLanguage> = localDataSource.observeAppLanguage()

    override suspend fun getCurrentLanguage(): AppLanguage = localDataSource.getCurrentLanguage()

    override suspend fun setAppLanguage(language: AppLanguage) {
        localDataSource.setAppLanguage(language)
    }

    override fun observeAppTheme(): Flow<AppThemeMode> = platformThemeStore.observeAppTheme()

    override fun getCurrentTheme(): AppThemeMode = platformThemeStore.getCurrentAppTheme()

    override suspend fun setAppTheme(theme: AppThemeMode) {
        platformThemeStore.setAppTheme(theme)
    }
}
