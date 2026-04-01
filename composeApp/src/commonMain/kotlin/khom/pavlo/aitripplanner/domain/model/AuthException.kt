package khom.pavlo.aitripplanner.domain.model

sealed class AuthException(
    message: String? = null,
    cause: Throwable? = null,
) : Exception(message, cause)

class InvalidCredentialsAuthException : AuthException()

class EmailAlreadyExistsAuthException : AuthException()

class ValidationAuthException : AuthException()

class InvalidTokenAuthException : AuthException()

class RefreshFailedAuthException : AuthException()

class NetworkAuthException(cause: Throwable? = null) : AuthException(cause = cause)

class TimeoutAuthException(cause: Throwable? = null) : AuthException(cause = cause)

class UnexpectedAuthException(cause: Throwable? = null) : AuthException(cause = cause)
