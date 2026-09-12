package com.example.pepsi.navigation

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.theme.PepsiTypography

@Composable
fun AppDrawer(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
) {
    ModalDrawerSheet(
        modifier = Modifier.fillMaxWidth(0.75f),
    ) {
        Text(
            text = "Pepsi Depo",
            style = PepsiTypography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )
        HorizontalDivider()
        drawerScreens.forEach { screen ->
            NavigationDrawerItem(
                label = { Text(screen.label) },
                selected = screen.route == currentRoute,
                onClick = { onItemClick(screen) },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            )
        }
    }
}
