package khom.pavlo.aitripplanner.core.platform

import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow

expect class PlatformThemeStore() {
    fun observeAppTheme(): Flow<AppThemeMode>
    fun getCurrentAppTheme(): AppThemeMode
    fun setAppTheme(theme: AppThemeMode)
}
