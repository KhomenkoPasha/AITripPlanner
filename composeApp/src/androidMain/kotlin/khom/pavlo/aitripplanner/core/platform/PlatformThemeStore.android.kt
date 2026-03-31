package khom.pavlo.aitripplanner.core.platform

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatDelegate
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

private const val THEME_PREFS_NAME = "travel_planner_preferences"
private const val APP_THEME_KEY = "app_theme_mode"

actual class PlatformThemeStore {
    private val mainHandler = Handler(Looper.getMainLooper())
    private val preferences = AndroidPlatformRuntime.appContext.getSharedPreferences(
        THEME_PREFS_NAME,
        Context.MODE_PRIVATE,
    )
    private val themeState = MutableStateFlow(readTheme())

    init {
        applyThemeMode(themeState.value)
    }

    actual fun observeAppTheme(): Flow<AppThemeMode> = themeState

    actual fun getCurrentAppTheme(): AppThemeMode = themeState.value

    actual fun setAppTheme(theme: AppThemeMode) {
        if (theme == themeState.value) return

        preferences.edit()
            .putString(APP_THEME_KEY, theme.name)
            .apply()
        themeState.value = theme
        applyThemeMode(theme)
    }

    private fun readTheme(): AppThemeMode = preferences
        .getString(APP_THEME_KEY, null)
        ?.let { stored -> AppThemeMode.entries.firstOrNull { it.name == stored } }
        ?: AppThemeMode.SYSTEM

    private fun applyThemeMode(theme: AppThemeMode) {
        val nightMode = when (theme) {
            AppThemeMode.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppThemeMode.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppThemeMode.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

        if (Looper.myLooper() == Looper.getMainLooper()) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        } else {
            mainHandler.post {
                AppCompatDelegate.setDefaultNightMode(nightMode)
            }
        }
    }
}
