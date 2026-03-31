package khom.pavlo.aitripplanner.core.platform

import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import platform.Foundation.NSUserDefaults

private const val APP_THEME_KEY = "app_theme_mode"

actual class PlatformThemeStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val themeState = MutableStateFlow(readTheme())

    actual fun observeAppTheme(): Flow<AppThemeMode> = themeState

    actual fun getCurrentAppTheme(): AppThemeMode = themeState.value

    actual fun setAppTheme(theme: AppThemeMode) {
        if (theme == themeState.value) return

        defaults.setObject(theme.name, forKey = APP_THEME_KEY)
        themeState.value = theme
    }

    private fun readTheme(): AppThemeMode = defaults.stringForKey(APP_THEME_KEY)
        ?.let { stored -> AppThemeMode.entries.firstOrNull { it.name == stored } }
        ?: AppThemeMode.SYSTEM
}
