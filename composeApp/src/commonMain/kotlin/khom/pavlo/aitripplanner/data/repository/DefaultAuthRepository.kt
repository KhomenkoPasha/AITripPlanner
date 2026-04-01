package khom.pavlo.aitripplanner.data.repository

import khom.pavlo.aitripplanner.core.auth.AuthSessionManager
import khom.pavlo.aitripplanner.data.remote.AuthRemoteDataSource
import khom.pavlo.aitripplanner.data.remote.AuthRemoteResult
import khom.pavlo.aitripplanner.domain.model.AuthSession
import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import khom.pavlo.aitripplanner.domain.model.RegistrationResult
import khom.pavlo.aitripplanner.domain.model.UnexpectedAuthException
import khom.pavlo.aitripplanner.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class DefaultAuthRepository(
    private val remoteDataSource: AuthRemoteDataSource,
    private val sessionManager: AuthSessionManager,
) : AuthRepository {
    override fun observeSavedSession(): Flow<AuthTokens?> = sessionManager.tokens

    override suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): RegistrationResult {
        val result = remoteDataSource.register(name, email, password, confirmPassword)
        val tokens = result.tokens
        if (tokens == null) {
            return RegistrationResult(email = email.trim())
        }

        sessionManager.saveSession(tokens)
        val user = result.user ?: remoteDataSource.getCurrentUser()
        sessionManager.saveUser(user)
        return RegistrationResult(
            session = AuthSession(user = user, tokens = tokens),
            email = email.trim(),
        )
    }

    override suspend fun login(
        email: String,
        password: String,
    ): AuthSession {
        val result = remoteDataSource.login(email, password)
        return authenticate(
            result = result,
            fallbackEmail = email,
            fallbackPassword = password,
        )
    }

    override suspend fun refresh(): AuthTokens {
        val current = sessionManager.getSavedSession() ?: throw UnexpectedAuthException()
        return sessionManager.refreshIfNeeded(current.accessToken) { refreshToken ->
            remoteDataSource.refresh(refreshToken)
        } ?: throw UnexpectedAuthException()
    }

    override suspend fun logout() {
        val current = sessionManager.getSavedSession()
        runCatching {
            remoteDataSource.logout(current?.refreshToken)
        }
        sessionManager.clearSession(revokeDeviceAccess = true)
    }

    override suspend fun getCurrentUser(): AuthUser {
        val user = remoteDataSource.getCurrentUser()
        sessionManager.saveUser(user)
        return user
    }

    override suspend fun getCachedUser(): AuthUser? = sessionManager.getCachedUser()

    override suspend fun getSavedSession(): AuthTokens? = sessionManager.getSavedSession()

    override suspend fun hasAuthorizedDevice(): Boolean = sessionManager.hasAuthorizedDevice()

    override suspend fun clearSession() {
        sessionManager.clearSession()
    }

    private suspend fun authenticate(
        result: AuthRemoteResult,
        fallbackEmail: String,
        fallbackPassword: String,
    ): AuthSession {
        val tokens = result.tokens
            ?: remoteDataSource.login(fallbackEmail, fallbackPassword).tokens
            ?: throw UnexpectedAuthException()

        sessionManager.saveSession(tokens)
        val user = result.user ?: remoteDataSource.getCurrentUser()
        sessionManager.saveUser(user)
        return AuthSession(user = user, tokens = tokens)
    }
}
