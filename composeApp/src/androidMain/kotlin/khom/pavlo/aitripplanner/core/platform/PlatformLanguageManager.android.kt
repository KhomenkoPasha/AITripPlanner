package khom.pavlo.aitripplanner.core.platform

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import khom.pavlo.aitripplanner.domain.model.AppLanguage

actual object PlatformLanguageManager {
    actual fun apply(language: AppLanguage) {
        AppCompatDelegate.setApplicationLocales(
            LocaleListCompat.forLanguageTags(language.toLanguageTag()),
        )
    }
}

private fun AppLanguage.toLanguageTag(): String = when (this) {
    AppLanguage.EN -> "en"
    AppLanguage.RU -> "ru"
    AppLanguage.UK -> "uk"
}
