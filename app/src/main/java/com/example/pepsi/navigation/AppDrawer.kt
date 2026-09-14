package com.example.pepsi.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import com.example.pepsi.theme.PepsiTypography

@Composable
fun AppDrawer(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    var depotExpanded by remember { mutableStateOf(depotDrawerScreens.any { it.route == currentRoute }) }
    var factoryExpanded by remember { mutableStateOf(factoryDrawerScreens.any { it.route == currentRoute }) }

    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.75f),
    ) {
        Text(
            text = "Pepsi Depo",
            style = PepsiTypography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()

        DrawerCategory(
            label = "Depot",
            expanded = depotExpanded,
            onToggle = { depotExpanded = !depotExpanded },
            screens = depotDrawerScreens,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
        )
        DrawerCategory(
            label = "Factory",
            expanded = factoryExpanded,
            onToggle = { factoryExpanded = !factoryExpanded },
            screens = factoryDrawerScreens,
            currentRoute = currentRoute,
            onItemClick = onItemClick,
        )
    }
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
