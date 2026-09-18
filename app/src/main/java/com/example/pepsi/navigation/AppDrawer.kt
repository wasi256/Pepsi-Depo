package com.example.pepsi.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.theme.PepsiTypography

/**
 * The navigation drawer. Each category (Admin / Factory / Depot) and each child screen
 * is only listed when the signed-in user's permissions cover it, so a factory manager
 * sees just Factory and a depot attendant just Depot.
 */
@Composable
fun AppDrawer(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    val sections = AppAccess.visibleSections().filter { AppAccess.visibleScreens(it).isNotEmpty() }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.75f),
    ) {
        Text(
            text = "Pepsi Depo",
            style = PepsiTypography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
        ) {
            sections.forEach { section ->
                val screens = AppAccess.visibleScreens(section)
                var expanded by remember(section) {
                    mutableStateOf(sections.size == 1 || screens.any { it.route == currentRoute })
                }
                DrawerCategory(
                    label = section.label,
                    expanded = expanded,
                    onToggle = { expanded = !expanded },
                    screens = screens,
                    currentRoute = currentRoute,
                    onItemClick = onItemClick,
                )
            }
        }

        HorizontalDivider()
        SignedInFooter()
    }
}

@Composable
private fun SignedInFooter() {
    val user = AuthSession.user
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
            text = user?.personnel_name ?: user?.username ?: "Signed in",
            style = MaterialTheme.typography.titleMedium,
        )
        user?.role_name?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    NavigationDrawerItem(
        label = { Text("Sign out") },
        icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
        selected = false,
        onClick = { AuthSession.signOut() },
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

@Composable
private fun DrawerCategory(
    label: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    screens: List<Screen>,
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    NavigationDrawerItem(
        label = { Text(text = label, fontWeight = FontWeight.Bold) },
        selected = false,
        icon = {
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = if (expanded) "Collapse $label" else "Expand $label",
            )
        },
        onClick = onToggle,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
    )
    if (expanded) {
        screens.forEach { screen ->
            NavigationDrawerItem(
                label = { Text(screen.label) },
                selected = screen.route == currentRoute,
                onClick = { onItemClick(screen) },
                modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 4.dp, bottom = 4.dp),
            )
        }
    }
}
