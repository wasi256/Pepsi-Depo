package com.example.pepsi.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pepsi.R

private fun iconFor(screen: Screen): ImageVector = when (screen) {
    Screen.Overview -> Icons.Filled.Dashboard
    Screen.Users -> Icons.Filled.Group
    Screen.Depos -> Icons.Filled.Store
    Screen.Products -> Icons.Filled.Inventory2
    Screen.AuditLogs -> Icons.Filled.History
    Screen.SystemHealth -> Icons.Filled.MonitorHeart
    Screen.Settings -> Icons.Filled.Settings
    else -> Icons.Filled.Dashboard
}

/**
 * Compact drawer (roughly a quarter of the screen width) so the destinations
 * behind it stay visible rather than being fully covered. Items are stacked
 * icon-over-label, like a navigation rail, so labels stay legible at that width.
 */
@Composable
fun AppDrawerContent(
    currentRoute: String?,
    onDestinationClick: (Screen) -> Unit,
) {
    ModalDrawerSheet(modifier = Modifier.fillMaxWidth(0.28f)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.pepsi_logo),
                contentDescription = "Pepsi Depo",
                modifier = Modifier.size(44.dp),
            )
            Text(
                text = "Pepsi Depo",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        HorizontalDivider()
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Screen.topLevel.forEach { screen ->
                CompactDrawerItem(
                    icon = iconFor(screen),
                    label = screen.title,
                    selected = currentRoute == screen.route,
                    onClick = { onDestinationClick(screen) },
                )
            }
        }
    }
}

@Composable
private fun CompactDrawerItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val contentColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = contentColor)
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            textAlign = TextAlign.Center,
            maxLines = 2,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
