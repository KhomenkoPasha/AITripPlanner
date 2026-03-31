package khom.pavlo.aitripplanner.domain.usecase

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppLanguageUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppLanguage> = repository.observeAppLanguage()
}

class GetCurrentAppLanguageUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(): AppLanguage = repository.getCurrentLanguage()
}

class SetAppLanguageUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(language: AppLanguage) {
        repository.setAppLanguage(language)
    }
}

class ObserveAppThemeUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): Flow<AppThemeMode> = repository.observeAppTheme()
}

class GetCurrentAppThemeUseCase(
    private val repository: SettingsRepository,
) {
    operator fun invoke(): AppThemeMode = repository.getCurrentTheme()
}

class SetAppThemeUseCase(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(theme: AppThemeMode) {
        repository.setAppTheme(theme)
    }
}
