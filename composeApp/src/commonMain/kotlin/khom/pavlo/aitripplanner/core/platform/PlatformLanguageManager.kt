package khom.pavlo.aitripplanner.core.platform

import khom.pavlo.aitripplanner.domain.model.AppLanguage

expect object PlatformLanguageManager {
    fun apply(language: AppLanguage)
}
