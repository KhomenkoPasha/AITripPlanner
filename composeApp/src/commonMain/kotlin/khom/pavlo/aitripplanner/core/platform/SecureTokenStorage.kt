package khom.pavlo.aitripplanner.core.platform

import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser

expect class SecureTokenStorage() {
    suspend fun readTokens(): AuthTokens?
    suspend fun saveTokens(tokens: AuthTokens)
    suspend fun readUser(): AuthUser?
    suspend fun saveUser(user: AuthUser)
    suspend fun clearUser()
    suspend fun hasAuthorizedDevice(): Boolean
    suspend fun markDeviceAuthorized()
    suspend fun clearDeviceAuthorization()
    suspend fun clear()
}
