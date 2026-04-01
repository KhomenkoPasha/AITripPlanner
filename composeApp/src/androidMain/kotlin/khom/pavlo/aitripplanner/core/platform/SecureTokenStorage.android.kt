package khom.pavlo.aitripplanner.core.platform

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import khom.pavlo.aitripplanner.domain.model.AuthTokens
import khom.pavlo.aitripplanner.domain.model.AuthUser
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private const val SESSION_PREFS_NAME = "travel_planner_secure_session"
private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
private const val KEY_ALIAS = "travel_planner_auth_tokens"
private const val ACCESS_TOKEN_KEY = "access_token_ciphertext"
private const val REFRESH_TOKEN_KEY = "refresh_token_ciphertext"
private const val USER_ID_KEY = "user_id"
private const val USER_NAME_KEY = "user_name"
private const val USER_EMAIL_KEY = "user_email"
private const val LEGACY_DEVICE_AUTHORIZED_KEY = "device_authorized"
private const val DEVICE_AUTHORIZATION_STATE_KEY = "device_authorization_state"
private const val DEVICE_AUTHORIZATION_STATE_AUTHORIZED = "AUTHORIZED"
private const val DEVICE_AUTHORIZATION_STATE_REVOKED = "REVOKED"
private const val AES_TRANSFORMATION = "AES/GCM/NoPadding"

actual class SecureTokenStorage {
    private val preferences = AndroidPlatformRuntime.appContext.getSharedPreferences(
        SESSION_PREFS_NAME,
        Context.MODE_PRIVATE,
    )

    actual suspend fun readTokens(): AuthTokens? {
        val encryptedAccessToken = preferences.getString(ACCESS_TOKEN_KEY, null) ?: return null
        val encryptedRefreshToken = preferences.getString(REFRESH_TOKEN_KEY, null) ?: return null

        return AuthTokens(
            accessToken = decrypt(encryptedAccessToken),
            refreshToken = decrypt(encryptedRefreshToken),
        )
    }

    actual suspend fun saveTokens(tokens: AuthTokens) {
        preferences.edit()
            .putString(ACCESS_TOKEN_KEY, encrypt(tokens.accessToken))
            .putString(REFRESH_TOKEN_KEY, encrypt(tokens.refreshToken))
            .apply()
    }

    actual suspend fun readUser(): AuthUser? {
        val name = preferences.getString(USER_NAME_KEY, null) ?: return null
        val email = preferences.getString(USER_EMAIL_KEY, null) ?: return null
        return AuthUser(
            id = preferences.getString(USER_ID_KEY, null),
            name = name,
            email = email,
        )
    }

    actual suspend fun saveUser(user: AuthUser) {
        preferences.edit()
            .putString(USER_ID_KEY, user.id)
            .putString(USER_NAME_KEY, user.name)
            .putString(USER_EMAIL_KEY, user.email)
            .apply()
    }

    actual suspend fun clearUser() {
        preferences.edit()
            .remove(USER_ID_KEY)
            .remove(USER_NAME_KEY)
            .remove(USER_EMAIL_KEY)
            .apply()
    }

    actual suspend fun hasAuthorizedDevice(): Boolean {
        val state = preferences.getString(DEVICE_AUTHORIZATION_STATE_KEY, null)
        return when (state) {
            DEVICE_AUTHORIZATION_STATE_AUTHORIZED -> true
            DEVICE_AUTHORIZATION_STATE_REVOKED -> false
            else -> preferences.getBoolean(LEGACY_DEVICE_AUTHORIZED_KEY, false)
        }
    }

    actual suspend fun markDeviceAuthorized() {
        preferences.edit()
            .putString(DEVICE_AUTHORIZATION_STATE_KEY, DEVICE_AUTHORIZATION_STATE_AUTHORIZED)
            .remove(LEGACY_DEVICE_AUTHORIZED_KEY)
            .apply()
    }

    actual suspend fun clearDeviceAuthorization() {
        preferences.edit()
            .putString(DEVICE_AUTHORIZATION_STATE_KEY, DEVICE_AUTHORIZATION_STATE_REVOKED)
            .remove(LEGACY_DEVICE_AUTHORIZED_KEY)
            .apply()
    }

    actual suspend fun clear() {
        preferences.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateSecretKey())
        val iv = Base64.encodeToString(cipher.iv, Base64.NO_WRAP)
        val ciphertext = Base64.encodeToString(cipher.doFinal(value.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP)
        return "$iv:$ciphertext"
    }

    private fun decrypt(value: String): String {
        val parts = value.split(':', limit = 2)
        require(parts.size == 2) { "Invalid encrypted token payload" }

        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateSecretKey(),
            GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)),
        )
        return cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)).decodeToString()
    }

    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
        val existing = keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        if (existing != null) return existing

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE_PROVIDER)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build(),
        )
        return generator.generateKey()
    }
}
