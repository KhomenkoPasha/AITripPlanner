package khom.pavlo.aitripplanner.domain.usecase

import khom.pavlo.aitripplanner.domain.model.AuthSession
import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import khom.pavlo.aitripplanner.domain.model.RegistrationResult
import khom.pavlo.aitripplanner.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class ObserveSavedSessionUseCase(
    private val repository: AuthRepository,
) {
    operator fun invoke(): Flow<AuthTokens?> = repository.observeSavedSession()
}

class RegisterUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): RegistrationResult = repository.register(name, email, password, confirmPassword)
}

class LoginUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(
        email: String,
        password: String,
    ): AuthSession = repository.login(email, password)
}

class LogoutUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

class GetCurrentUserUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthUser = repository.getCurrentUser()
}

class GetCachedUserUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthUser? = repository.getCachedUser()
}

class GetSavedSessionUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): AuthTokens? = repository.getSavedSession()
}

class HasAuthorizedDeviceUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke(): Boolean = repository.hasAuthorizedDevice()
}

class ClearSessionUseCase(
    private val repository: AuthRepository,
) {
    suspend operator fun invoke() {
        repository.clearSession()
    }
}
