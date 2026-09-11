package com.example.pepsi.systemadmin.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.components.StatCard
import com.example.pepsi.common.data.AdminDataStore

private data class OverviewStat(val title: String, val value: String, val icon: ImageVector)

@Composable
fun OverviewScreen() {
    val stats = listOf(
        OverviewStat("Registered Users", AdminDataStore.users.size.toString(), Icons.Filled.Group),
        OverviewStat("Registered Depos", AdminDataStore.depos.size.toString(), Icons.Filled.Store),
        OverviewStat("Audit Logs", AdminDataStore.auditLogs.size.toString(), Icons.Filled.History),
        OverviewStat("System Health", "${AdminDataStore.systemHealthPercent.value}%", Icons.Filled.MonitorHeart),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Overview",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(stats) { stat ->
                StatCard(
                    title = stat.title,
                    value = stat.value,
                    icon = stat.icon,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
