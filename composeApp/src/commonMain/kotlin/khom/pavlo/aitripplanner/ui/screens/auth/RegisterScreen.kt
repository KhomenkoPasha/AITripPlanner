package khom.pavlo.aitripplanner.ui.screens.auth

import aitripplanner.composeapp.generated.resources.Res
import aitripplanner.composeapp.generated.resources.auth_confirm_password_label
import aitripplanner.composeapp.generated.resources.auth_email_label
import aitripplanner.composeapp.generated.resources.auth_hide_password
import aitripplanner.composeapp.generated.resources.auth_login_prompt
import aitripplanner.composeapp.generated.resources.auth_name_label
import aitripplanner.composeapp.generated.resources.auth_password_label
import aitripplanner.composeapp.generated.resources.auth_register_action
import aitripplanner.composeapp.generated.resources.auth_register_subtitle
import aitripplanner.composeapp.generated.resources.auth_register_title
import aitripplanner.composeapp.generated.resources.auth_show_password
import aitripplanner.composeapp.generated.resources.auth_switch_to_login
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
import androidx.compose.runtime.remember
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
fun RegisterScreen(
    name: String,
    email: String,
    password: String,
    confirmPassword: String,
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeMode,
    nameError: String?,
    emailError: String?,
    passwordError: String?,
    confirmPasswordError: String?,
    errorMessage: String?,
    isLoading: Boolean,
    isPasswordVisible: Boolean,
    isConfirmPasswordVisible: Boolean,
    isSubmitEnabled: Boolean,
    offlineActionLabel: String,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onToggleConfirmPasswordVisibility: () -> Unit,
    onSubmit: () -> Unit,
    onOpenLogin: () -> Unit,
    onContinueOffline: () -> Unit,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = stringResource(Res.string.auth_register_title)
    val subtitle = stringResource(Res.string.auth_register_subtitle)
    val nameLabel = stringResource(Res.string.auth_name_label)
    val emailLabel = stringResource(Res.string.auth_email_label)
    val passwordLabel = stringResource(Res.string.auth_password_label)
    val confirmPasswordLabel = stringResource(Res.string.auth_confirm_password_label)
    val actionLabel = stringResource(Res.string.auth_register_action)
    val promptLabel = stringResource(Res.string.auth_login_prompt)
    val switchLabel = stringResource(Res.string.auth_switch_to_login)
    val showPasswordLabel = stringResource(Res.string.auth_show_password)
    val hidePasswordLabel = stringResource(Res.string.auth_hide_password)
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val emailRequester = remember { FocusRequester() }
    val passwordRequester = remember { FocusRequester() }
    val confirmPasswordRequester = remember { FocusRequester() }

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
                            value = name,
                            onValueChange = onNameChange,
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(nameLabel) },
                            singleLine = true,
                            enabled = !isLoading,
                            isError = nameError != null,
                            shape = TravelTheme.corners.medium,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { emailRequester.requestFocus() },
                            ),
                            supportingText = nameError?.let { message -> { Text(message) } },
                            colors = authFieldColors(),
                        )
                        OutlinedTextField(
                            value = email,
                            onValueChange = onEmailChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(emailRequester),
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
                                imeAction = ImeAction.Next,
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { confirmPasswordRequester.requestFocus() },
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
                        OutlinedTextField(
                            value = confirmPassword,
                            onValueChange = onConfirmPasswordChange,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(confirmPasswordRequester),
                            label = { Text(confirmPasswordLabel) },
                            singleLine = true,
                            enabled = !isLoading,
                            isError = confirmPasswordError != null,
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
                            visualTransformation = if (isConfirmPasswordVisible) {
                                VisualTransformation.None
                            } else {
                                PasswordVisualTransformation()
                            },
                            trailingIcon = {
                                IconButton(
                                    onClick = onToggleConfirmPasswordVisibility,
                                    enabled = !isLoading,
                                ) {
                                    Icon(
                                        imageVector = if (isConfirmPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                        contentDescription = if (isConfirmPasswordVisible) hidePasswordLabel else showPasswordLabel,
                                    )
                                }
                            },
                            supportingText = confirmPasswordError?.let { message -> { Text(message) } },
                            colors = authFieldColors(),
                        )
                        PrimaryActionButton(
                            text = actionLabel,
                            onClick = {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                onSubmit()
                            },
                            enabled = isSubmitEnabled,
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
                                onClick = onOpenLogin,
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
