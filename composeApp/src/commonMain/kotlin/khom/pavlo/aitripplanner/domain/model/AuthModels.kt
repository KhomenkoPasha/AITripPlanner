package khom.pavlo.aitripplanner.domain.model

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

data class AuthUser(
    val id: String? = null,
    val name: String,
    val email: String,
)

data class AuthSession(
    val user: AuthUser,
    val tokens: AuthTokens,
)

data class RegistrationResult(
    val session: AuthSession? = null,
    val email: String,
)

enum class AuthStatus {
    AUTHENTICATED,
    OFFLINE,
    UNAUTHENTICATED,
    LOADING,
    REFRESHING,
    ERROR,
}

data class AuthState(
    val status: AuthStatus = AuthStatus.LOADING,
    val user: AuthUser? = null,
    val errorMessage: String? = null,
) {
    val isAuthenticated: Boolean
        get() = status == AuthStatus.AUTHENTICATED && user != null

    val isOfflineMode: Boolean
        get() = status == AuthStatus.OFFLINE

    val canUseApp: Boolean
        get() = isAuthenticated || isOfflineMode

    val isLoading: Boolean
        get() = status == AuthStatus.LOADING || status == AuthStatus.REFRESHING
}
