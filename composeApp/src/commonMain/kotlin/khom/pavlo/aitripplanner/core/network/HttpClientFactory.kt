package khom.pavlo.aitripplanner.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import khom.pavlo.aitripplanner.core.auth.AuthSessionManager
import khom.pavlo.aitripplanner.data.remote.AuthRefreshRequest
import khom.pavlo.aitripplanner.data.remote.AuthRequestContext
import khom.pavlo.aitripplanner.data.remote.TravelPlannerConfig
import khom.pavlo.aitripplanner.data.remote.executeTokensRequest
import khom.pavlo.aitripplanner.data.remote.toAuthException
import khom.pavlo.aitripplanner.domain.model.InvalidTokenAuthException
import khom.pavlo.aitripplanner.domain.model.RefreshFailedAuthException
import kotlinx.serialization.json.Json

class HttpClientFactory(
    private val json: Json,
    private val config: TravelPlannerConfig,
    private val authSessionManager: AuthSessionManager,
) {
    fun create(): HttpClient = HttpClient {
        buildBaseConfig()
        install(Auth) {
            bearer {
                cacheTokens = false
                loadTokens {
                    authSessionManager.getSavedSession()?.toBearerTokens()
                }
                sendWithoutRequest { request ->
                    request.url.encodedPath.shouldUseAuthorization()
                }
                refreshTokens {
                    val refreshedTokens = try {
                        authSessionManager.refreshIfNeeded(oldTokens?.accessToken) { refreshToken ->
                            executeTokensRequest(AuthRequestContext.REFRESH) {
                                client.post("${config.baseUrl}/auth/refresh") {
                                    contentType(ContentType.Application.Json)
                                    setBody(AuthRefreshRequest(refreshToken))
                                }
                            }
                        }
                    } catch (error: Throwable) {
                        val mapped = error.toAuthException(AuthRequestContext.REFRESH)
                        if (mapped is RefreshFailedAuthException || mapped is InvalidTokenAuthException) {
                            authSessionManager.clearSession()
                        }
                        null
                    } ?: return@refreshTokens null

                    refreshedTokens.toBearerTokens()
                }
            }
        }
    }

    private fun HttpClientConfig<*>.buildBaseConfig() {
        install(HttpTimeout) {
            val timeoutMillis = 5 * 60 * 1000L
            requestTimeoutMillis = timeoutMillis
            connectTimeoutMillis = timeoutMillis
            socketTimeoutMillis = timeoutMillis
        }
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = object : Logger {
                override fun log(message: String) = println(message)
            }
            level = LogLevel.INFO
        }
    }
}

private fun String.shouldUseAuthorization(): Boolean = !startsWith("/auth/login") &&
    !startsWith("/auth/register") &&
    !startsWith("/auth/refresh")

private fun khom.pavlo.aitripplanner.domain.model.AuthTokens.toBearerTokens(): BearerTokens = BearerTokens(
    accessToken = accessToken,
    refreshToken = refreshToken,
)
