package khom.pavlo.aitripplanner.presentation

import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AuthException
import khom.pavlo.aitripplanner.domain.model.EmailAlreadyExistsAuthException
import khom.pavlo.aitripplanner.domain.model.InvalidCredentialsAuthException
import khom.pavlo.aitripplanner.domain.model.InvalidTokenAuthException
import khom.pavlo.aitripplanner.domain.model.NetworkAuthException
import khom.pavlo.aitripplanner.domain.model.RefreshFailedAuthException
import khom.pavlo.aitripplanner.domain.model.TimeoutAuthException
import khom.pavlo.aitripplanner.domain.model.ValidationAuthException

internal fun AppLanguage.emptyEmailError(): String = when (this) {
    AppLanguage.EN -> "Enter email"
    AppLanguage.RU -> "Введите email"
    AppLanguage.UK -> "Введіть email"
}

internal fun AppLanguage.invalidEmailError(): String = when (this) {
    AppLanguage.EN -> "Enter a valid email address"
    AppLanguage.RU -> "Введите корректный email"
    AppLanguage.UK -> "Некоректний email"
}

internal fun AppLanguage.emptyPasswordError(): String = when (this) {
    AppLanguage.EN -> "Enter password"
    AppLanguage.RU -> "Введите пароль"
    AppLanguage.UK -> "Введіть пароль"
}

internal fun AppLanguage.invalidPasswordError(): String = when (this) {
    AppLanguage.EN -> "Password must contain at least 6 characters"
    AppLanguage.RU -> "Пароль должен содержать минимум 6 символов"
    AppLanguage.UK -> "Пароль має містити щонайменше 6 символів"
}

internal fun AppLanguage.passwordLettersAndDigitsError(): String = when (this) {
    AppLanguage.EN -> "Password must contain Latin letters and digits"
    AppLanguage.RU -> "Пароль должен содержать латинские буквы и цифры"
    AppLanguage.UK -> "Пароль має містити латинські літери та цифри"
}

internal fun AppLanguage.passwordSpecialCharactersNotAllowedError(): String = when (this) {
    AppLanguage.EN -> "Special characters are not allowed in the password"
    AppLanguage.RU -> "Спецсимволы в пароле не разрешены"
    AppLanguage.UK -> "Спецсимволи в паролі не дозволені"
}

internal fun AppLanguage.emptyNameError(): String = when (this) {
    AppLanguage.EN -> "Enter your name"
    AppLanguage.RU -> "Введите имя"
    AppLanguage.UK -> "Введіть ім'я"
}

internal fun AppLanguage.emptyConfirmPasswordError(): String = when (this) {
    AppLanguage.EN -> "Confirm password"
    AppLanguage.RU -> "Подтвердите пароль"
    AppLanguage.UK -> "Підтвердіть пароль"
}

internal fun AppLanguage.passwordsDoNotMatchError(): String = when (this) {
    AppLanguage.EN -> "Passwords do not match"
    AppLanguage.RU -> "Пароли не совпадают"
    AppLanguage.UK -> "Паролі не співпадають"
}

internal fun AppLanguage.registrationSuccessMessage(): String = when (this) {
    AppLanguage.EN -> "Account created. Sign in to continue."
    AppLanguage.RU -> "Аккаунт создан. Войдите, чтобы продолжить."
    AppLanguage.UK -> "Акаунт створено. Увійдіть, щоб продовжити."
}

internal fun AppLanguage.sessionExpiredError(): String = when (this) {
    AppLanguage.EN -> "Your session has expired. Sign in again."
    AppLanguage.RU -> "Сессия истекла. Войдите снова."
    AppLanguage.UK -> "Сесію завершено. Увійдіть знову."
}

internal fun AppLanguage.restoreSessionError(): String = when (this) {
    AppLanguage.EN -> "Unable to restore the session right now"
    AppLanguage.RU -> "Сейчас не удалось восстановить сессию"
    AppLanguage.UK -> "Зараз не вдалося відновити сесію"
}

internal fun AppLanguage.authFallbackError(): String = when (this) {
    AppLanguage.EN -> "Something went wrong. Try again."
    AppLanguage.RU -> "Что-то пошло не так. Попробуйте снова."
    AppLanguage.UK -> "Щось пішло не так. Спробуйте ще раз."
}

internal fun Throwable.toAuthMessage(language: AppLanguage): String = when (this) {
    is InvalidCredentialsAuthException -> when (language) {
        AppLanguage.EN -> "Incorrect email or password"
        AppLanguage.RU -> "Неверный email или пароль"
        AppLanguage.UK -> "Неправильний email або пароль"
    }
    is EmailAlreadyExistsAuthException -> when (language) {
        AppLanguage.EN -> "An account with this email already exists"
        AppLanguage.RU -> "Аккаунт с таким email уже существует"
        AppLanguage.UK -> "Акаунт з таким email уже існує"
    }
    is ValidationAuthException -> when (language) {
        AppLanguage.EN -> "Check the registration data and try again"
        AppLanguage.RU -> "Проверьте данные регистрации и попробуйте снова"
        AppLanguage.UK -> "Перевірте дані реєстрації та спробуйте ще раз"
    }
    is InvalidTokenAuthException,
    is RefreshFailedAuthException -> language.sessionExpiredError()
    is TimeoutAuthException -> when (language) {
        AppLanguage.EN -> "The request timed out. Try again."
        AppLanguage.RU -> "Время ожидания истекло. Попробуйте снова."
        AppLanguage.UK -> "Час очікування вичерпано. Спробуйте ще раз."
    }
    is NetworkAuthException -> when (language) {
        AppLanguage.EN -> "No connection to the server. Check your internet and try again."
        AppLanguage.RU -> "Нет соединения с сервером. Проверьте интернет и попробуйте снова."
        AppLanguage.UK -> "Немає з'єднання із сервером. Перевірте інтернет і спробуйте ще раз."
    }
    is AuthException -> language.authFallbackError()
    else -> language.authFallbackError()
}
