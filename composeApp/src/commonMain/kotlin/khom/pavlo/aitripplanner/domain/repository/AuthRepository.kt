package khom.pavlo.aitripplanner.domain.repository

import khom.pavlo.aitripplanner.domain.model.AuthSession
import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import khom.pavlo.aitripplanner.domain.model.RegistrationResult
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeSavedSession(): Flow<AuthTokens?>

    suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): RegistrationResult

    suspend fun login(
        email: String,
        password: String,
    ): AuthSession

    suspend fun refresh(): AuthTokens

    suspend fun logout()

    suspend fun getCurrentUser(): AuthUser

    suspend fun getCachedUser(): AuthUser?

    suspend fun getSavedSession(): AuthTokens?

    suspend fun hasAuthorizedDevice(): Boolean

    suspend fun clearSession()
}
