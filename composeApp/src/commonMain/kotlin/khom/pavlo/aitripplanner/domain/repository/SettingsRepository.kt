package khom.pavlo.aitripplanner.domain.repository

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeAppLanguage(): Flow<AppLanguage>
    suspend fun getCurrentLanguage(): AppLanguage
    suspend fun setAppLanguage(language: AppLanguage)
}
