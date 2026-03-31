package khom.pavlo.aitripplanner.domain.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK,
}

fun AppThemeMode.resolveDarkTheme(systemDarkTheme: Boolean): Boolean = when (this) {
    AppThemeMode.SYSTEM -> systemDarkTheme
    AppThemeMode.LIGHT -> false
    AppThemeMode.DARK -> true
}
