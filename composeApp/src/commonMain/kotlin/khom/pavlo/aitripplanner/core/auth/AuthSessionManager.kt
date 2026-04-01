package khom.pavlo.aitripplanner.core.auth

import khom.pavlo.aitripplanner.core.platform.SecureTokenStorage
import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AuthSessionManager(
    private val secureTokenStorage: SecureTokenStorage,
) {
    private val mutex = Mutex()
    private val mutableTokens = MutableStateFlow<AuthTokens?>(null)

    val tokens: StateFlow<AuthTokens?> = mutableTokens.asStateFlow()

    fun peekAccessToken(): String? = mutableTokens.value?.accessToken

    suspend fun getSavedSession(): AuthTokens? = mutex.withLock {
        readTokensLocked()
    }

    suspend fun saveSession(tokens: AuthTokens) = mutex.withLock {
        secureTokenStorage.saveTokens(tokens)
        secureTokenStorage.markDeviceAuthorized()
        mutableTokens.value = tokens
    }

    suspend fun getCachedUser(): AuthUser? = mutex.withLock {
        secureTokenStorage.readUser()
    }

    suspend fun saveUser(user: AuthUser) = mutex.withLock {
        secureTokenStorage.saveUser(user)
        secureTokenStorage.markDeviceAuthorized()
    }

    suspend fun hasAuthorizedDevice(): Boolean = mutex.withLock {
        secureTokenStorage.hasAuthorizedDevice()
    }

    suspend fun clearSession(revokeDeviceAccess: Boolean = false) = mutex.withLock {
        secureTokenStorage.clear()
        if (revokeDeviceAccess) {
            secureTokenStorage.clearUser()
            secureTokenStorage.clearDeviceAuthorization()
        }
        mutableTokens.value = null
    }

    suspend fun refreshIfNeeded(
        tokenUsed: String?,
        refreshAction: suspend (refreshToken: String) -> AuthTokens,
    ): AuthTokens? = mutex.withLock {
        val current = readTokensLocked() ?: return null
        if (tokenUsed != null && current.accessToken != tokenUsed) {
            return current
        }

        val refreshed = refreshAction(current.refreshToken)
        secureTokenStorage.saveTokens(refreshed)
        mutableTokens.value = refreshed
        refreshed
    }

    private suspend fun readTokensLocked(): AuthTokens? {
        val inMemory = mutableTokens.value
        if (inMemory != null) return inMemory

        val stored = runCatching { secureTokenStorage.readTokens() }.getOrNull()
        if (stored != null) {
            secureTokenStorage.markDeviceAuthorized()
        }
        mutableTokens.value = stored
        return stored
    }
}
