package khom.pavlo.aitripplanner.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.JsonConvertException
import io.ktor.utils.io.errors.IOException
import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import khom.pavlo.aitripplanner.domain.model.EmailAlreadyExistsAuthException
import khom.pavlo.aitripplanner.domain.model.InvalidCredentialsAuthException
import khom.pavlo.aitripplanner.domain.model.InvalidTokenAuthException
import khom.pavlo.aitripplanner.domain.model.NetworkAuthException
import khom.pavlo.aitripplanner.domain.model.RefreshFailedAuthException
import khom.pavlo.aitripplanner.domain.model.TimeoutAuthException
import khom.pavlo.aitripplanner.domain.model.UnexpectedAuthException
import khom.pavlo.aitripplanner.domain.model.ValidationAuthException
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

interface AuthRemoteDataSource {
    suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): AuthRemoteResult

    suspend fun login(
        email: String,
        password: String,
    ): AuthRemoteResult

    suspend fun refresh(refreshToken: String): AuthTokens
    suspend fun logout(refreshToken: String?)
    suspend fun getCurrentUser(): AuthUser
}

data class AuthRemoteResult(
    val tokens: AuthTokens? = null,
    val user: AuthUser? = null,
)

class KtorAuthRemoteDataSource(
    private val client: HttpClient,
    private val config: TravelPlannerConfig,
) : AuthRemoteDataSource {
    override suspend fun register(
        name: String,
        email: String,
        password: String,
        confirmPassword: String,
    ): AuthRemoteResult = executePayloadRequest(AuthRequestContext.REGISTER) {
        client.post("${config.baseUrl}/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(
                AuthRegisterRequest(
                    name = name.trim(),
                    email = email.trim(),
                    password = password,
                    confirmPassword = confirmPassword,
                ),
            )
        }
    }

    override suspend fun login(
        email: String,
        password: String,
    ): AuthRemoteResult = executePayloadRequest(AuthRequestContext.LOGIN) {
        client.post("${config.baseUrl}/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                AuthLoginRequest(
                    email = email.trim(),
                    password = password,
                ),
            )
        }
    }

    override suspend fun refresh(refreshToken: String): AuthTokens = executeTokensRequest(AuthRequestContext.REFRESH) {
        client.post("${config.baseUrl}/auth/refresh") {
            contentType(ContentType.Application.Json)
            setBody(AuthRefreshRequest(refreshToken = refreshToken))
        }
    }

    override suspend fun logout(refreshToken: String?) {
        executeUnitRequest(AuthRequestContext.LOGOUT) {
            client.post("${config.baseUrl}/auth/logout") {
                contentType(ContentType.Application.Json)
                if (!refreshToken.isNullOrBlank()) {
                    setBody(AuthRefreshRequest(refreshToken = refreshToken))
                }
            }
        }
    }

    override suspend fun getCurrentUser(): AuthUser = executeUserRequest(AuthRequestContext.ME) {
        client.get("${config.baseUrl}/auth/me")
    }
}

internal enum class AuthRequestContext {
    LOGIN,
    REGISTER,
    REFRESH,
    LOGOUT,
    ME,
}

@Serializable
internal data class AuthRegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
)

@Serializable
internal data class AuthLoginRequest(
    val email: String,
    val password: String,
)

@Serializable
internal data class AuthRefreshRequest(
    val refreshToken: String,
)

internal suspend fun executePayloadRequest(
    context: AuthRequestContext,
    block: suspend () -> io.ktor.client.statement.HttpResponse,
): AuthRemoteResult = executeAuthRequest(context, block) { response ->
    val body = response.body<JsonObject>()
    parseAuthRemoteResult(body)
}

internal suspend fun executeTokensRequest(
    context: AuthRequestContext,
    block: suspend () -> io.ktor.client.statement.HttpResponse,
): AuthTokens = executeAuthRequest(context, block) { response ->
    val body = response.body<JsonObject>()
    parseAuthTokens(body)
}

private suspend fun executeUserRequest(
    context: AuthRequestContext,
    block: suspend () -> io.ktor.client.statement.HttpResponse,
): AuthUser = executeAuthRequest(context, block) { response ->
    val body = response.body<JsonObject>()
    parseAuthUser(body)
}

private suspend fun executeUnitRequest(
    context: AuthRequestContext,
    block: suspend () -> io.ktor.client.statement.HttpResponse,
) {
    executeAuthRequest(context, block) { }
}

private suspend fun <T> executeAuthRequest(
    context: AuthRequestContext,
    block: suspend () -> io.ktor.client.statement.HttpResponse,
    onSuccess: suspend (io.ktor.client.statement.HttpResponse) -> T,
): T = try {
    val response = block()
    if (!response.status.isSuccess()) {
        throw mapAuthHttpException(response, context)
    }
    onSuccess(response)
} catch (error: Throwable) {
    throw error.toAuthException(context)
}

internal fun parseAuthRemoteResult(body: JsonObject): AuthRemoteResult = AuthRemoteResult(
    tokens = body.extractAuthTokensOrNull(),
    user = body.extractAuthUserOrNull(),
)

internal fun parseAuthTokens(body: JsonObject): AuthTokens = body.extractAuthTokensOrNull()
    ?: throw UnexpectedAuthException()

internal fun parseAuthUser(body: JsonObject): AuthUser = body.extractAuthUserOrNull()
    ?: throw UnexpectedAuthException()

internal suspend fun mapAuthHttpException(
    response: io.ktor.client.statement.HttpResponse,
    context: AuthRequestContext,
): Throwable {
    val payload = runCatching { response.body<JsonObject>() }.getOrNull()
    val errorCode = payload
        ?.candidateContainers()
        ?.firstNotNullOfOrNull { container ->
            container.readString("code", "errorCode", "error")
        }
        ?.lowercase()
        .orEmpty()

    return when {
        context == AuthRequestContext.REGISTER &&
            (response.status == HttpStatusCode.Conflict ||
                errorCode.contains("exists") ||
                errorCode.contains("already")) -> EmailAlreadyExistsAuthException()

        context == AuthRequestContext.REGISTER &&
            (response.status == HttpStatusCode.BadRequest ||
                response.status.value == 422 ||
                errorCode.contains("validation") ||
                errorCode.contains("invalid")) -> ValidationAuthException()

        context == AuthRequestContext.LOGIN &&
            (response.status == HttpStatusCode.BadRequest ||
                response.status == HttpStatusCode.Unauthorized ||
                errorCode.contains("credential") ||
                errorCode.contains("password")) -> InvalidCredentialsAuthException()

        context == AuthRequestContext.REFRESH &&
            response.status == HttpStatusCode.Unauthorized -> RefreshFailedAuthException()

        (context == AuthRequestContext.ME || context == AuthRequestContext.LOGOUT) &&
            response.status == HttpStatusCode.Unauthorized -> InvalidTokenAuthException()

        response.status == HttpStatusCode.RequestTimeout ||
            response.status == HttpStatusCode.GatewayTimeout -> TimeoutAuthException()

        else -> UnexpectedAuthException()
    }
}

internal fun Throwable.toAuthException(context: AuthRequestContext): Throwable = when (this) {
    is CancellationException -> throw this
    is InvalidCredentialsAuthException -> this
    is EmailAlreadyExistsAuthException -> this
    is ValidationAuthException -> this
    is InvalidTokenAuthException -> this
    is RefreshFailedAuthException -> this
    is TimeoutAuthException -> this
    is NetworkAuthException -> this
    is UnexpectedAuthException -> this
    is HttpRequestTimeoutException,
    is SocketTimeoutException -> TimeoutAuthException(this)
    is IOException -> NetworkAuthException(this)
    is JsonConvertException -> UnexpectedAuthException(this)
    else -> when (context) {
        AuthRequestContext.REFRESH -> RefreshFailedAuthException()
        else -> UnexpectedAuthException(this)
    }
}

private fun JsonObject.extractAuthTokensOrNull(): AuthTokens? {
    val container = candidateContainers().firstNotNullOfOrNull { current ->
        val accessToken = current.readString("accessToken", "access_token")
        val refreshToken = current.readString("refreshToken", "refresh_token")
        if (accessToken != null && refreshToken != null) {
            AuthTokens(accessToken = accessToken, refreshToken = refreshToken)
        } else {
            null
        }
    }

    return container
}

private fun JsonObject.extractAuthUserOrNull(): AuthUser? = candidateContainers()
    .firstNotNullOfOrNull { current ->
        current.asAuthUserOrNull()
    }

private fun JsonObject.candidateContainers(): List<JsonObject> = buildList {
    add(this@candidateContainers)
    listOf("data", "session", "tokens", "result", "user").forEach { key ->
        this@candidateContainers[key]
            ?.jsonObjectOrNull()
            ?.let(::add)
    }
    this@candidateContainers["data"]
        ?.jsonObjectOrNull()
        ?.get("user")
        ?.jsonObjectOrNull()
        ?.let(::add)
}

private fun JsonObject.asAuthUserOrNull(): AuthUser? {
    val directUser = this["user"]?.jsonObjectOrNull()?.asAuthUserOrNull()
    if (directUser != null) return directUser

    val email = readString("email") ?: return null
    val name = readString("name", "fullName", "username")
        ?: email.substringBefore('@')
    return AuthUser(
        id = readString("id", "_id", "userId"),
        name = name,
        email = email,
    )
}

private fun JsonObject.readString(vararg keys: String): String? = keys.firstNotNullOfOrNull { key ->
    val primitive = this[key] as? JsonPrimitive ?: return@firstNotNullOfOrNull null
    primitive.contentOrNull()?.takeIf { it.isNotBlank() }
}

private fun kotlinx.serialization.json.JsonElement.jsonObjectOrNull(): JsonObject? = runCatching {
    jsonObject
}.getOrNull()

private fun JsonPrimitive.contentOrNull(): String? = runCatching {
    content
}.getOrNull()
