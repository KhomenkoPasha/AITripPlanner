package khom.pavlo.aitripplanner.core.platform

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import platform.Foundation.NSUserDefaults

actual object PlatformLanguageManager {
    actual fun apply(language: AppLanguage) {
        NSUserDefaults.standardUserDefaults.setObject(
            listOf(language.toLanguageTag()),
            forKey = "AppleLanguages",
        )
    }
}

private fun AppLanguage.toLanguageTag(): String = when (this) {
    AppLanguage.EN -> "en"
    AppLanguage.RU -> "ru"
    AppLanguage.UK -> "uk"
}
