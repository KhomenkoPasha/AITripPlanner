package khom.pavlo.aitripplanner.ui.screens.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import khom.pavlo.aitripplanner.domain.model.AppLanguage
import khom.pavlo.aitripplanner.domain.model.AppThemeMode
import khom.pavlo.aitripplanner.ui.components.EmptyStateView
import khom.pavlo.aitripplanner.ui.components.LanguageSelector
import khom.pavlo.aitripplanner.ui.components.PrimaryActionButton
import khom.pavlo.aitripplanner.ui.components.SavedTripCard
import khom.pavlo.aitripplanner.ui.components.SectionHeader
import khom.pavlo.aitripplanner.ui.components.ThemeSelector
import khom.pavlo.aitripplanner.ui.components.TravelAppScaffold
import khom.pavlo.aitripplanner.ui.components.TravelCardSurface
import khom.pavlo.aitripplanner.ui.model.TripOverviewUiModel
import khom.pavlo.aitripplanner.ui.preview.PreviewTrips
import khom.pavlo.aitripplanner.ui.strings.AppStrings
import khom.pavlo.aitripplanner.ui.strings.appStrings
import khom.pavlo.aitripplanner.ui.theme.AppTheme
import khom.pavlo.aitripplanner.ui.theme.TravelTheme

@Composable
fun ProfileScreen(
    userName: String,
    userEmail: String,
    selectedLanguage: AppLanguage,
    selectedTheme: AppThemeMode,
    preferenceLabels: List<String>,
    favoriteTrips: List<TripOverviewUiModel>,
    strings: AppStrings,
    onLanguageSelected: (AppLanguage) -> Unit,
    onThemeSelected: (AppThemeMode) -> Unit,
    onTripClick: (String) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
    bottomBar: @Composable (() -> Unit)? = null,
) {
    TravelAppScaffold(
        modifier = modifier,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = TravelTheme.spacing.lg,
                        vertical = TravelTheme.spacing.md,
                    ),
                verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
            ) {
                Text(
                    text = strings.profileTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(TravelTheme.spacing.xs)) {
                        LanguageSelector(
                            selectedLanguage = selectedLanguage,
                            label = strings.languageLabel,
                            onLanguageSelected = onLanguageSelected,
                        )
                        ThemeSelector(
                            selectedTheme = selectedTheme,
                            label = strings.themeLabel,
                            systemLabel = strings.themeSystemLabel,
                            lightLabel = strings.themeLightLabel,
                            darkLabel = strings.themeDarkLabel,
                            onThemeSelected = onThemeSelected,
                        )
                    }
                }
            }
        },
        bottomBar = bottomBar,
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(TravelTheme.spacing.lg),
            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.lg),
        ) {
            item {
                TravelCardSurface {
                    Column(
                        modifier = Modifier.padding(TravelTheme.spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                    ) {
                        Text(
                            text = userName,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = userEmail,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
            item {
                TravelCardSurface {
                    Column(
                        modifier = Modifier.padding(TravelTheme.spacing.xl),
                        verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.md),
                    ) {
                        ProfileInfoRow(
                            label = strings.profileNameLabel,
                            value = userName,
                        )
                        ProfileInfoRow(
                            label = strings.profileEmailLabel,
                            value = userEmail,
                        )
                        ProfileInfoRow(
                            label = strings.languageLabel,
                            value = selectedLanguage.displayLabel(selectedLanguage),
                        )
                        ProfileInfoRow(
                            label = strings.themeLabel,
                            value = selectedTheme.displayLabel(strings),
                        )
                    }
                }
            }
            item {
                SectionHeader(
                    title = strings.profilePreferencesTitle,
                    subtitle = strings.preferencesTitle,
                )
            }
            if (preferenceLabels.isEmpty()) {
                item {
                    EmptyStateView(
                        title = strings.profilePreferencesTitle,
                        subtitle = strings.profilePreferencesEmpty,
                    )
                }
            } else {
                item {
                    TravelCardSurface {
                        Column(
                            modifier = Modifier.padding(TravelTheme.spacing.xl),
                            verticalArrangement = Arrangement.spacedBy(TravelTheme.spacing.sm),
                        ) {
                            preferenceLabels.forEach { label ->
                                Text(
                                    text = "• $label",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
            item {
                SectionHeader(
                    title = strings.profileFavoritesTitle,
                    subtitle = strings.savedTripsSubtitle,
                )
            }
            if (favoriteTrips.isEmpty()) {
                item {
                    EmptyStateView(
                        title = strings.profileFavoritesEmptyTitle,
                        subtitle = strings.profileFavoritesEmptySubtitle,
                    )
                }
            } else {
                items(favoriteTrips, key = { it.id }) { trip ->
                    SavedTripCard(
                        trip = trip,
                        onClick = { onTripClick(trip.id) },
                    )
                }
            }
            item {
                PrimaryActionButton(
                    text = strings.logoutAction,
                    onClick = onLogoutClick,
                )
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

private fun AppThemeMode.displayLabel(strings: AppStrings): String = when (this) {
    AppThemeMode.SYSTEM -> strings.themeSystemLabel
    AppThemeMode.LIGHT -> strings.themeLightLabel
    AppThemeMode.DARK -> strings.themeDarkLabel
}

private fun AppLanguage.displayLabel(uiLanguage: AppLanguage): String = when (this) {
    AppLanguage.EN -> when (uiLanguage) {
        AppLanguage.EN -> "English"
        AppLanguage.RU -> "Английский"
        AppLanguage.UK -> "Англійська"
    }

    AppLanguage.RU -> when (uiLanguage) {
        AppLanguage.EN -> "Russian"
        AppLanguage.RU -> "Русский"
        AppLanguage.UK -> "Російська"
    }

    AppLanguage.UK -> when (uiLanguage) {
        AppLanguage.EN -> "Ukrainian"
        AppLanguage.RU -> "Украинский"
        AppLanguage.UK -> "Українська"
    }
}

@Preview
@Composable
private fun ProfileScreenPreview() {
    AppTheme {
        ProfileScreen(
            userName = "Pavlo Khom",
            userEmail = "pavlo@example.com",
            selectedLanguage = AppLanguage.RU,
            selectedTheme = AppThemeMode.SYSTEM,
            preferenceLabels = listOf("Музеи", "Relaxed", "Budget"),
            favoriteTrips = listOf(PreviewTrips.romeOverview),
            strings = appStrings(),
            onLanguageSelected = {},
            onThemeSelected = {},
            onTripClick = {},
            onLogoutClick = {},
        )
    }
}
