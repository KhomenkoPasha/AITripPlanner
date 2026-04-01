package khom.pavlo.aitripplanner.presentation.auth

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AuthState
import khom.pavlo.aitripplanner.domain.model.AuthStatus
import khom.pavlo.aitripplanner.domain.model.InvalidTokenAuthException
import khom.pavlo.aitripplanner.domain.model.NetworkAuthException
import khom.pavlo.aitripplanner.domain.model.RefreshFailedAuthException
import khom.pavlo.aitripplanner.domain.model.TimeoutAuthException
import khom.pavlo.aitripplanner.domain.usecase.ClearSessionUseCase
import khom.pavlo.aitripplanner.domain.usecase.GetCachedUserUseCase
import khom.pavlo.aitripplanner.domain.usecase.GetCurrentUserUseCase
import khom.pavlo.aitripplanner.domain.usecase.HasAuthorizedDeviceUseCase
import khom.pavlo.aitripplanner.domain.usecase.GetSavedSessionUseCase
import khom.pavlo.aitripplanner.domain.usecase.LoginUseCase
import khom.pavlo.aitripplanner.domain.usecase.LogoutUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveAppLanguageUseCase
import khom.pavlo.aitripplanner.domain.usecase.ObserveSavedSessionUseCase
import khom.pavlo.aitripplanner.domain.usecase.RegisterUseCase
import khom.pavlo.aitripplanner.presentation.authFallbackError
import khom.pavlo.aitripplanner.presentation.emptyConfirmPasswordError
import khom.pavlo.aitripplanner.presentation.emptyEmailError
import khom.pavlo.aitripplanner.presentation.emptyNameError
import khom.pavlo.aitripplanner.presentation.emptyPasswordError
import khom.pavlo.aitripplanner.presentation.invalidEmailError
import khom.pavlo.aitripplanner.presentation.invalidPasswordError
import khom.pavlo.aitripplanner.presentation.passwordLettersAndDigitsError
import khom.pavlo.aitripplanner.presentation.passwordSpecialCharactersNotAllowedError
import khom.pavlo.aitripplanner.presentation.passwordsDoNotMatchError
import khom.pavlo.aitripplanner.presentation.registrationSuccessMessage
import khom.pavlo.aitripplanner.presentation.restoreSessionError
import khom.pavlo.aitripplanner.presentation.sessionExpiredError
import khom.pavlo.aitripplanner.presentation.toAuthMessage
import khom.pavlo.aitripplanner.presentation.base.Presenter
import khom.pavlo.aitripplanner.ui.navigation.AuthRoute
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val SESSION_RESTORE_TIMEOUT_MS = 12_000L

data class LoginFieldErrors(
    val email: String? = null,
    val password: String? = null,
)

data class RegisterFieldErrors(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val confirmPassword: String? = null,
)

data class AuthUiState(
    val route: AuthRoute = AuthRoute.LOGIN,
    val authState: AuthState = AuthState(),
    val isStartupResolved: Boolean = false,
    val loginEmail: String = "",
    val loginPassword: String = "",
    val loginFieldErrors: LoginFieldErrors = LoginFieldErrors(),
    val registerName: String = "",
    val registerEmail: String = "",
    val registerPassword: String = "",
    val registerConfirmPassword: String = "",
    val registerFieldErrors: RegisterFieldErrors = RegisterFieldErrors(),
    val loginPasswordVisible: Boolean = false,
    val registerPasswordVisible: Boolean = false,
    val registerConfirmPasswordVisible: Boolean = false,
    val isSubmitting: Boolean = false,
    val isLogoutInProgress: Boolean = false,
    val submitError: String? = null,
    val registrationSuccess: String? = null,
) {
    val isRegisterFormClearlyValid: Boolean
        get() = registerName.trim().isNotEmpty() &&
            registerEmail.trim().isValidEmail() &&
            registerPassword.matchesRegistrationPolicy() &&
            registerConfirmPassword.isNotBlank() &&
            registerPassword == registerConfirmPassword
}

class AuthViewModel(
    private val observeAppLanguage: ObserveAppLanguageUseCase,
    private val observeSavedSession: ObserveSavedSessionUseCase,
    private val getCachedUser: GetCachedUserUseCase,
    private val getSavedSession: GetSavedSessionUseCase,
    private val hasAuthorizedDevice: HasAuthorizedDeviceUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val login: LoginUseCase,
    private val register: RegisterUseCase,
    private val logout: LogoutUseCase,
    private val clearSession: ClearSessionUseCase,
) : Presenter() {
    private val mutableState = MutableStateFlow(AuthUiState())
    private var currentLanguage: AppLanguage = AppLanguage.EN
    private var hasResolvedStartup = false

    val state: StateFlow<AuthUiState> = mutableState.asStateFlow()

    init {
        scope.launch {
            observeAppLanguage().collect { language ->
                currentLanguage = language
            }
        }
        scope.launch {
            observeSavedSession().collect { tokens ->
                if (!hasResolvedStartup) return@collect
                if (tokens == null && state.value.authState.isAuthenticated) {
                    if (hasAuthorizedDevice()) {
                        continueOffline(state.value.authState.user)
                    } else {
                        mutableState.update {
                            it.copy(
                                route = AuthRoute.LOGIN,
                                authState = AuthState(
                                    status = AuthStatus.UNAUTHENTICATED,
                                    errorMessage = currentLanguage.sessionExpiredError(),
                                ),
                                isSubmitting = false,
                                isLogoutInProgress = false,
                                loginPassword = "",
                                registerPassword = "",
                                registerConfirmPassword = "",
                            )
                        }
                    }
                }
            }
        }
        restoreSession()
    }

    fun onLoginEmailChange(value: String) = mutableState.update {
        it.copy(
            loginEmail = value,
            loginFieldErrors = it.loginFieldErrors.copy(email = null),
            submitError = null,
            registrationSuccess = if (it.route == AuthRoute.LOGIN) it.registrationSuccess else null,
            authState = it.authState.clearTransientError(),
        )
    }

    fun onLoginPasswordChange(value: String) = mutableState.update {
        it.copy(
            loginPassword = value,
            loginFieldErrors = it.loginFieldErrors.copy(password = null),
            submitError = null,
            authState = it.authState.clearTransientError(),
        )
    }

    fun onRegisterNameChange(value: String) = mutableState.update {
        it.copy(
            registerName = value,
            registerFieldErrors = it.registerFieldErrors.copy(name = null),
            submitError = null,
            registrationSuccess = null,
            authState = it.authState.clearTransientError(),
        )
    }

    fun onRegisterEmailChange(value: String) = mutableState.update {
        it.copy(
            registerEmail = value,
            registerFieldErrors = it.registerFieldErrors.copy(email = null),
            submitError = null,
            registrationSuccess = null,
            authState = it.authState.clearTransientError(),
        )
    }

    fun onRegisterPasswordChange(value: String) = mutableState.update {
        it.copy(
            registerPassword = value,
            registerFieldErrors = it.registerFieldErrors.copy(password = null),
            submitError = null,
            registrationSuccess = null,
            authState = it.authState.clearTransientError(),
        )
    }

    fun onRegisterConfirmPasswordChange(value: String) = mutableState.update {
        it.copy(
            registerConfirmPassword = value,
            registerFieldErrors = it.registerFieldErrors.copy(confirmPassword = null),
            submitError = null,
            registrationSuccess = null,
            authState = it.authState.clearTransientError(),
        )
    }

    fun toggleLoginPasswordVisibility() {
        mutableState.update { it.copy(loginPasswordVisible = !it.loginPasswordVisible) }
    }

    fun toggleRegisterPasswordVisibility() {
        mutableState.update { it.copy(registerPasswordVisible = !it.registerPasswordVisible) }
    }

    fun toggleRegisterConfirmPasswordVisibility() {
        mutableState.update { it.copy(registerConfirmPasswordVisible = !it.registerConfirmPasswordVisible) }
    }

    fun openLogin() {
        mutableState.update {
            it.copy(
                route = AuthRoute.LOGIN,
                submitError = null,
                registerFieldErrors = RegisterFieldErrors(),
                registerPassword = "",
                registerConfirmPassword = "",
                authState = it.authState.clearTransientError(),
            )
        }
    }

    fun openRegister() {
        mutableState.update {
            it.copy(
                route = AuthRoute.REGISTER,
                submitError = null,
                registrationSuccess = null,
                loginFieldErrors = LoginFieldErrors(),
                loginPassword = "",
                authState = it.authState.clearTransientError(),
            )
        }
    }

    fun onLoginSubmit() {
        val snapshot = state.value
        val fieldErrors = snapshot.validateLogin(currentLanguage)
        if (!fieldErrors.isEmpty()) {
            mutableState.update {
                it.copy(
                    loginFieldErrors = fieldErrors,
                    submitError = null,
                )
            }
            return
        }

        val email = snapshot.loginEmail.trim()
        val password = snapshot.loginPassword
        scope.launch {
            mutableState.update {
                it.copy(
                    isSubmitting = true,
                    submitError = null,
                    registrationSuccess = null,
                    loginFieldErrors = LoginFieldErrors(),
                    authState = it.authState.clearTransientError(),
                )
            }
            runCatching {
                login(email, password)
            }.onSuccess { session ->
                mutableState.update {
                    it.copy(
                        authState = AuthState(
                            status = AuthStatus.AUTHENTICATED,
                            user = session.user,
                        ),
                        isStartupResolved = true,
                        isSubmitting = false,
                        submitError = null,
                        loginPassword = "",
                        registerPassword = "",
                        registerConfirmPassword = "",
                    )
                }
            }.onFailure { error ->
                mutableState.update {
                    it.copy(
                        authState = AuthState(
                            status = AuthStatus.ERROR,
                            errorMessage = error.toAuthMessage(currentLanguage),
                        ),
                        submitError = error.toAuthMessage(currentLanguage),
                        isSubmitting = false,
                    )
                }
            }
        }
    }

    fun onRegisterSubmit() {
        val snapshot = state.value
        val fieldErrors = snapshot.validateRegister(currentLanguage)
        if (!fieldErrors.isEmpty()) {
            mutableState.update {
                it.copy(
                    registerFieldErrors = fieldErrors,
                    submitError = null,
                    registrationSuccess = null,
                )
            }
            return
        }

        val name = snapshot.registerName.trim()
        val email = snapshot.registerEmail.trim()
        val password = snapshot.registerPassword
        val confirmPassword = snapshot.registerConfirmPassword

        scope.launch {
            mutableState.update {
                it.copy(
                    isSubmitting = true,
                    submitError = null,
                    registrationSuccess = null,
                    registerFieldErrors = RegisterFieldErrors(),
                    authState = it.authState.clearTransientError(),
                )
            }
            runCatching {
                register(name, email, password, confirmPassword)
            }.onSuccess { result ->
                val session = result.session
                if (session != null) {
                    mutableState.update {
                        it.copy(
                            authState = AuthState(
                                status = AuthStatus.AUTHENTICATED,
                                user = session.user,
                            ),
                            isStartupResolved = true,
                            isSubmitting = false,
                            submitError = null,
                            registerPassword = "",
                            registerConfirmPassword = "",
                            loginPassword = "",
                        )
                    }
                } else {
                    mutableState.update {
                        it.copy(
                            route = AuthRoute.LOGIN,
                            authState = AuthState(status = AuthStatus.UNAUTHENTICATED),
                            loginEmail = result.email,
                            loginPassword = "",
                            registerName = "",
                            registerEmail = result.email,
                            registerPassword = "",
                            registerConfirmPassword = "",
                            registerFieldErrors = RegisterFieldErrors(),
                            submitError = null,
                            registrationSuccess = currentLanguage.registrationSuccessMessage(),
                            isSubmitting = false,
                        )
                    }
                }
            }.onFailure { error ->
                val message = error.toAuthMessage(currentLanguage)
                mutableState.update {
                    it.copy(
                        authState = AuthState(
                            status = AuthStatus.ERROR,
                            errorMessage = message,
                        ),
                        submitError = message,
                        registrationSuccess = null,
                        isSubmitting = false,
                    )
                }
            }
        }
    }

    fun logout() {
        scope.launch {
            mutableState.update {
                it.copy(
                    isLogoutInProgress = true,
                    submitError = null,
                    authState = it.authState.copy(errorMessage = null),
                )
            }
            runCatching { logout.invoke() }
            mutableState.update {
                    it.copy(
                        route = AuthRoute.LOGIN,
                        authState = AuthState(status = AuthStatus.UNAUTHENTICATED),
                        isStartupResolved = true,
                        isLogoutInProgress = false,
                        submitError = null,
                        loginPassword = "",
                    registerPassword = "",
                    registerConfirmPassword = "",
                )
            }
        }
    }

    fun continueOffline(user: khom.pavlo.aitripplanner.domain.model.AuthUser? = state.value.authState.user) {
        hasResolvedStartup = true
        mutableState.update {
            it.copy(
                route = AuthRoute.LOGIN,
                authState = AuthState(
                    status = AuthStatus.OFFLINE,
                    user = user,
                ),
                isStartupResolved = true,
                isSubmitting = false,
                isLogoutInProgress = false,
                submitError = null,
                registrationSuccess = null,
                loginPassword = "",
                registerPassword = "",
                registerConfirmPassword = "",
            )
        }
    }

    fun exitOfflineMode() {
        mutableState.update {
            it.copy(
                route = AuthRoute.LOGIN,
                authState = AuthState(status = AuthStatus.UNAUTHENTICATED),
                isStartupResolved = true,
                isSubmitting = false,
                isLogoutInProgress = false,
                submitError = null,
                registrationSuccess = null,
                loginPassword = "",
                registerPassword = "",
                registerConfirmPassword = "",
            )
        }
    }

    fun handleBackPress(): Boolean {
        val route = state.value.route
        return if (route == AuthRoute.REGISTER) {
            openLogin()
            true
        } else {
            false
        }
    }

    private fun restoreSession() {
        scope.launch {
            val authorizedDevice = hasAuthorizedDevice()
            val cachedUser = if (authorizedDevice) getCachedUser() else null

            if (authorizedDevice) {
                hasResolvedStartup = true
                mutableState.update {
                    it.copy(
                        authState = AuthState(
                            status = AuthStatus.OFFLINE,
                            user = cachedUser,
                        ),
                        isStartupResolved = true,
                        submitError = null,
                        registrationSuccess = null,
                    )
                }
            } else {
                mutableState.update {
                    it.copy(authState = AuthState(status = AuthStatus.LOADING))
                }
            }

            val savedSession = withTimeoutOrNull(SESSION_RESTORE_TIMEOUT_MS) {
                getSavedSession()
            }
            if (savedSession == null) {
                if (!authorizedDevice) {
                    hasResolvedStartup = true
                    mutableState.update {
                        it.copy(
                            authState = AuthState(status = AuthStatus.UNAUTHENTICATED),
                            isStartupResolved = true,
                        )
                    }
                }
                return@launch
            }

            if (!authorizedDevice) {
                mutableState.update {
                    it.copy(authState = AuthState(status = AuthStatus.REFRESHING))
                }
            }

            val result = runCatching {
                withTimeoutOrNull(SESSION_RESTORE_TIMEOUT_MS) {
                    getCurrentUser()
                } ?: throw TimeoutAuthException()
            }

            result.onSuccess { user ->
                hasResolvedStartup = true
                mutableState.update {
                    it.copy(
                        authState = AuthState(
                            status = AuthStatus.AUTHENTICATED,
                            user = user,
                        ),
                        isStartupResolved = true,
                    )
                }
            }.onFailure { error ->
                hasResolvedStartup = true
                when (error) {
                    is InvalidTokenAuthException,
                    is RefreshFailedAuthException -> {
                        clearSession()
                        mutableState.update {
                            it.copy(
                                route = AuthRoute.LOGIN,
                                authState = AuthState(
                                    status = if (authorizedDevice) AuthStatus.OFFLINE else AuthStatus.UNAUTHENTICATED,
                                    user = if (authorizedDevice) getCachedUser() ?: cachedUser else null,
                                    errorMessage = if (authorizedDevice) null else currentLanguage.sessionExpiredError(),
                                ),
                                isStartupResolved = true,
                            )
                        }
                    }

                    is NetworkAuthException,
                    is TimeoutAuthException -> {
                        if (authorizedDevice) {
                            continueOffline(getCachedUser() ?: state.value.authState.user ?: cachedUser)
                        } else {
                            mutableState.update {
                                it.copy(
                                    route = AuthRoute.LOGIN,
                                    authState = AuthState(status = AuthStatus.UNAUTHENTICATED),
                                    isStartupResolved = true,
                                    submitError = null,
                                )
                            }
                        }
                    }

                    else -> {
                        mutableState.update {
                            it.copy(
                                route = AuthRoute.LOGIN,
                                authState = AuthState(
                                    status = AuthStatus.ERROR,
                                    errorMessage = error.toAuthMessage(currentLanguage)
                                        .ifBlank { currentLanguage.restoreSessionError() },
                                ),
                                isStartupResolved = true,
                                submitError = error.toAuthMessage(currentLanguage)
                                    .ifBlank { currentLanguage.restoreSessionError() },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun AuthUiState.validateLogin(language: AppLanguage): LoginFieldErrors = LoginFieldErrors(
    email = when {
        loginEmail.trim().isEmpty() -> language.emptyEmailError()
        !loginEmail.trim().isValidEmail() -> language.invalidEmailError()
        else -> null
    },
    password = when {
        loginPassword.isEmpty() -> language.emptyPasswordError()
        else -> null
    },
)

private fun AuthUiState.validateRegister(language: AppLanguage): RegisterFieldErrors {
    val passwordValidation = language.validateRegistrationPassword(registerPassword)
    val confirmPasswordError = when {
        registerConfirmPassword.isEmpty() -> language.emptyConfirmPasswordError()
        registerPassword != registerConfirmPassword -> language.passwordsDoNotMatchError()
        else -> null
    }

    return RegisterFieldErrors(
        name = if (registerName.trim().isEmpty()) language.emptyNameError() else null,
        email = when {
            registerEmail.trim().isEmpty() -> language.emptyEmailError()
            !registerEmail.trim().isValidEmail() -> language.invalidEmailError()
            else -> null
        },
        password = passwordValidation,
        confirmPassword = confirmPasswordError,
    )
}

private fun AppLanguage.validateRegistrationPassword(password: String): String? = when {
    password.isEmpty() -> emptyPasswordError()
    password.length < 6 -> invalidPasswordError()
    password.any { !it.isLetterOrDigit() } -> passwordSpecialCharactersNotAllowedError()
    password.any { it.isLetter() && !it.isLatinLetter() } -> passwordLettersAndDigitsError()
    password.none { it.isLatinLetter() } || password.none { it.isDigit() } -> passwordLettersAndDigitsError()
    else -> null
}

private fun RegisterFieldErrors.isEmpty(): Boolean = name == null &&
    email == null &&
    password == null &&
    confirmPassword == null

private fun LoginFieldErrors.isEmpty(): Boolean = email == null && password == null

private fun AuthState.clearTransientError(): AuthState = if (status == AuthStatus.ERROR) {
    copy(status = AuthStatus.UNAUTHENTICATED, errorMessage = null)
} else {
    copy(errorMessage = null)
}

private fun String.isValidEmail(): Boolean = length >= 5 &&
    contains('@') &&
    substringAfter('@', "").contains('.')

private fun String.matchesRegistrationPolicy(): Boolean = length >= 6 &&
    all { it.isLetterOrDigit() } &&
    any { it.isDigit() } &&
    any { it.isLatinLetter() } &&
    none { it.isLetter() && !it.isLatinLetter() }

private fun Char.isLatinLetter(): Boolean = this in 'a'..'z' || this in 'A'..'Z'
