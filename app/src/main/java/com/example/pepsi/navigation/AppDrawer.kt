package com.example.pepsi.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

private fun iconFor(screen: Screen): ImageVector = when (screen) {
    Screen.Overview -> Icons.Filled.Dashboard
    Screen.Users -> Icons.Filled.Group
    Screen.Depos -> Icons.Filled.Store
    Screen.AuditLogs -> Icons.Filled.History
    Screen.SystemHealth -> Icons.Filled.MonitorHeart
    Screen.Settings -> Icons.Filled.Settings
    else -> Icons.Filled.Dashboard
}

@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onDestinationClick: (Screen) -> Unit,
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(text = "Pepsi Depo", style = MaterialTheme.typography.titleLarge)
            Text(text = "System Administrator", style = MaterialTheme.typography.bodyMedium)
        }
        HorizontalDivider()
        Screen.topLevel.forEach { screen ->
            NavigationDrawerItem(
                icon = { Icon(imageVector = iconFor(screen), contentDescription = null) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = { onDestinationClick(screen) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}
