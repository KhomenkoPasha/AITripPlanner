@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package khom.pavlo.aitripplanner.core.platform

import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.NSMutableDictionary
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

private const val KEYCHAIN_SERVICE = "khom.pavlo.aitripplanner.auth"
private const val ACCESS_TOKEN_ACCOUNT = "accessToken"
private const val REFRESH_TOKEN_ACCOUNT = "refreshToken"
private const val USER_ID_ACCOUNT = "userId"
private const val USER_NAME_ACCOUNT = "userName"
private const val USER_EMAIL_ACCOUNT = "userEmail"
private const val LEGACY_DEVICE_AUTHORIZED_ACCOUNT = "deviceAuthorized"
private const val DEVICE_AUTHORIZATION_STATE_ACCOUNT = "deviceAuthorizationState"
private const val DEVICE_AUTHORIZATION_STATE_AUTHORIZED = "AUTHORIZED"
private const val DEVICE_AUTHORIZATION_STATE_REVOKED = "REVOKED"

actual class SecureTokenStorage {
    actual suspend fun readTokens(): AuthTokens? {
        val accessToken = readValue(ACCESS_TOKEN_ACCOUNT) ?: return null
        val refreshToken = readValue(REFRESH_TOKEN_ACCOUNT) ?: return null
        return AuthTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
        )
    }

    actual suspend fun saveTokens(tokens: AuthTokens) {
        saveValue(ACCESS_TOKEN_ACCOUNT, tokens.accessToken)
        saveValue(REFRESH_TOKEN_ACCOUNT, tokens.refreshToken)
    }

    actual suspend fun readUser(): AuthUser? {
        val name = readValue(USER_NAME_ACCOUNT) ?: return null
        val email = readValue(USER_EMAIL_ACCOUNT) ?: return null
        return AuthUser(
            id = readValue(USER_ID_ACCOUNT),
            name = name,
            email = email,
        )
    }

    actual suspend fun saveUser(user: AuthUser) {
        user.id?.let { saveValue(USER_ID_ACCOUNT, it) } ?: deleteValue(USER_ID_ACCOUNT)
        saveValue(USER_NAME_ACCOUNT, user.name)
        saveValue(USER_EMAIL_ACCOUNT, user.email)
    }

    actual suspend fun clearUser() {
        deleteValue(USER_ID_ACCOUNT)
        deleteValue(USER_NAME_ACCOUNT)
        deleteValue(USER_EMAIL_ACCOUNT)
    }

    actual suspend fun hasAuthorizedDevice(): Boolean = when (readValue(DEVICE_AUTHORIZATION_STATE_ACCOUNT)) {
        DEVICE_AUTHORIZATION_STATE_AUTHORIZED -> true
        DEVICE_AUTHORIZATION_STATE_REVOKED -> false
        else -> readValue(LEGACY_DEVICE_AUTHORIZED_ACCOUNT) == "true"
    }

    actual suspend fun markDeviceAuthorized() {
        saveValue(DEVICE_AUTHORIZATION_STATE_ACCOUNT, DEVICE_AUTHORIZATION_STATE_AUTHORIZED)
        deleteValue(LEGACY_DEVICE_AUTHORIZED_ACCOUNT)
    }

    actual suspend fun clearDeviceAuthorization() {
        saveValue(DEVICE_AUTHORIZATION_STATE_ACCOUNT, DEVICE_AUTHORIZATION_STATE_REVOKED)
        deleteValue(LEGACY_DEVICE_AUTHORIZED_ACCOUNT)
    }

    actual suspend fun clear() {
        deleteValue(ACCESS_TOKEN_ACCOUNT)
        deleteValue(REFRESH_TOKEN_ACCOUNT)
    }

    private fun saveValue(account: String, value: String) {
        deleteValue(account)
        val query = baseQuery(account).apply {
            setObject(value.toKeychainData(), forKey = kSecValueData)
        }

        val status = SecItemAdd(query, null)
        check(status == errSecSuccess) { "Unable to save secure session" }
    }

    private fun readValue(account: String): String? = memScoped {
        val query = baseQuery(account).apply {
            setObject(kCFBooleanTrue, forKey = kSecReturnData)
            setObject(kSecMatchLimitOne, forKey = kSecMatchLimit)
        }

        val result = alloc<CFTypeRefVar>()
        val status = SecItemCopyMatching(query, result.ptr)
        when (status) {
            errSecItemNotFound -> null
            errSecSuccess -> {
                val data = result.value as platform.Foundation.NSData
                NSString.create(data = data, encoding = NSUTF8StringEncoding)?.toString()
            }
            else -> error("Unable to read secure session")
        }
    }

    private fun deleteValue(account: String) {
        SecItemDelete(baseQuery(account))
    }

    private fun baseQuery(account: String): NSMutableDictionary = NSMutableDictionary().apply {
        setObject(kSecClassGenericPassword, forKey = kSecClass)
        setObject(KEYCHAIN_SERVICE, forKey = kSecAttrService)
        setObject(account, forKey = kSecAttrAccount)
    }
}

private fun String.toKeychainData() = NSString.create(string = this)
    .dataUsingEncoding(NSUTF8StringEncoding)
    ?: error("Unable to encode secure session")
