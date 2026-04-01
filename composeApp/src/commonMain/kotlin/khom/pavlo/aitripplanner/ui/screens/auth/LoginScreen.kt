package khom.pavlo.aitripplanner.ui.screens.auth

import aitripplanner.composeapp.generated.resources.Res
import aitripplanner.composeapp.generated.resources.auth_email_label
import aitripplanner.composeapp.generated.resources.auth_hide_password
import aitripplanner.composeapp.generated.resources.auth_login_action
import aitripplanner.composeapp.generated.resources.auth_login_prompt
import aitripplanner.composeapp.generated.resources.auth_login_subtitle
import aitripplanner.composeapp.generated.resources.auth_login_title
import aitripplanner.composeapp.generated.resources.auth_password_label
import aitripplanner.composeapp.generated.resources.auth_show_password
import aitripplanner.composeapp.generated.resources.auth_switch_to_register
import aitripplanner.composeapp.generated.resources.language_label
import aitripplanner.composeapp.generated.resources.theme_dark_label
import aitripplanner.composeapp.generated.resources.theme_label
import aitripplanner.composeapp.generated.resources.theme_light_label
import aitripplanner.composeapp.generated.resources.theme_system_label
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.ui.components.ErrorStateView
import khom.pavlo.aitripplanner.ui.components.LanguageSelector
import khom.pavlo.aitripplanner.ui.components.PrimaryActionButton
import khom.pavlo.aitripplanner.ui.components.ThemeSelector
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.TravelCardSurface
import khom.pavlo.aitripplanner.ui.theme.TravelTheme
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreen(
    email: String,
    password: String,
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeMode,
    emailError: String?,
    passwordError: String?,
    errorMessage: String?,
    successMessage: String?,
    isLoading: Boolean,
    isPasswordVisible: Boolean,
    offlineActionLabel: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onOpenRegister: () -> Unit,
    onContinueOffline: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(Res.string.auth_login_title)
    val subtitle = stringResource(Res.string.auth_login_subtitle)
    val emailLabel = stringResource(Res.string.auth_email_label)
    val passwordLabel = stringResource(Res.string.auth_password_label)
    val actionLabel = stringResource(Res.string.auth_login_action)
    val promptLabel = stringResource(Res.string.auth_login_prompt)
    val switchLabel = stringResource(Res.string.auth_switch_to_register)
    val showPasswordLabel = stringResource(Res.string.auth_show_password)
    val hidePasswordLabel = stringResource(Res.string.auth_hide_password)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val passwordRequester = androidx.compose.runtime.remember { FocusRequester() }

    TravelAppScaffold(
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.lg,
                        vertical = TravelTheme.spacing.md,
                    ),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs)) {
                    LanguageSelector(
                        selectedLanguage = selectedLanguage,
                        label = stringResource(Res.string.language_label),
                        onLanguageSelected = onLanguageSelected,
                    )
                    ThemeSelector(
                        selectedTheme = selectedTheme,
                        label = stringResource(Res.string.theme_label),
                        systemLabel = stringResource(Res.string.theme_system_label),
                        lightLabel = stringResource(Res.string.theme_light_label),
                        darkLabel = stringResource(Res.string.theme_dark_label),
                        onThemeSelected = onThemeSelected,
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(
                horizontal = TravelTheme.spacing.lg,
                vertical = TravelTheme.spacing.lg,
            ),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
        ) {
            item {
                TravelCardSurface {
                    Column(
                        modifier = Modifier.padding(TravelTheme.spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            successMessage?.let { message ->
                item { AuthSuccessNotice(message = message) }
            }
            if (errorMessage != null) {
                item {
                    ErrorStateView(
                        title = title,
                        subtitle = errorMessage,
                    )
                }
            }
            item {
                TravelCardSurface {
                    Column(
                        modifier = Modifier.padding(TravelTheme.spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                    ) {
                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(emailLabel) },
                            singleLine = true,
                            enabled = !isLoading,
                            isError = emailError != null,
                            shape = TravelTheme.corners.medium,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { passwordRequester.requestFocus() },
                            ),
                            supportingText = emailError?.let { message -> { Text(message) } },
                            colors = authFieldColors(),
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = onPasswordChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(passwordRequester),
                            label = { Text(passwordLabel) },
                            singleLine = true,
                            enabled = !isLoading,
                            isError = passwordError != null,
                            shape = TravelTheme.corners.medium,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done,
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    keyboardController?.hide()
                                    focusManager.clearFocus()
                                    onSubmit()
                                },
                            ),
                            visualTransformation = if (isPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = onTogglePasswordVisibility,
                                    enabled = !isLoading,
                                ) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (isPasswordVisible) hidePasswordLabel else showPasswordLabel,
                                    )
                                }
                            },
                            supportingText = passwordError?.let { message -> { Text(message) } },
                            colors = authFieldColors(),
                        )
                        PrimaryActionButton(
                            text = actionLabel,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSubmit()
                            },
                            enabled = email.isNotBlank() && password.isNotBlank() && !isLoading,
                            isLoading = isLoading,
                        )
                        TextButton(
                            onClick = onContinueOffline,
                            enabled = !isLoading,
                            modifier = Modifier.align(Alignment.End),
                        ) {
                            Text(text = offlineActionLabel)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = promptLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            TextButton(
                                onClick = onOpenRegister,
                                enabled = !isLoading,
                            ) {
                                Text(text = switchLabel)
                            }
                        }
                    }
                }
            }
        }
    }
}
