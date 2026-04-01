package khom.pavlo.aitripplanner.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import khom.pavlo.aitripplanner.ui.navigation.BottomTab
import khom.pavlo.aitripplanner.ui.strings.AppStrings

@Composable
fun BottomNavigationBar(
    selectedTab: BottomTab,
    strings: AppStrings,
    onTabSelected: (BottomTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
    ) {
        BottomTab.entries.forEach { tab ->
            val label = when (tab) {
                BottomTab.CREATE_NEW_TRIP -> strings.createTripTab
                BottomTab.MY_TRIPS -> strings.myTripsTab
                BottomTab.PROFILE -> strings.profileTab
            }
            val icon = when (tab) {
                BottomTab.CREATE_NEW_TRIP -> Icons.Outlined.AddCircleOutline
                BottomTab.MY_TRIPS -> Icons.Outlined.BookmarkBorder
                BottomTab.PROFILE -> Icons.Outlined.PersonOutline
            }
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                icon = { Icon(imageVector = icon, contentDescription = label) },
                label = {
                    Text(
                        text = label,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}
