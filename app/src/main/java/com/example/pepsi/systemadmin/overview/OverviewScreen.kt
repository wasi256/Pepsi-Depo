package com.example.pepsi.systemadmin.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.pepsi.common.components.TrendLineChart
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.theme.PepsiClassicBlue
import com.example.pepsi.theme.PepsiDarkBlue
import com.example.pepsi.theme.PepsiRed

private data class OverviewStat(val title: String, val value: String, val icon: ImageVector)

@Composable
fun OverviewScreen() {
    val stats = listOf(
        OverviewStat("Registered Users", AdminDataStore.users.size.toString(), Icons.Filled.Group),
        OverviewStat("Registered Depos", AdminDataStore.depos.size.toString(), Icons.Filled.Store),
        OverviewStat("Audit Logs", AdminDataStore.auditLogs.size.toString(), Icons.Filled.History),
        OverviewStat("System Health", "${AdminDataStore.systemHealthPercent.value}%", Icons.Filled.MonitorHeart),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = "Overview", style = MaterialTheme.typography.titleLarge)

        stats.chunked(2).forEach { rowStats ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                rowStats.forEach { stat ->
                    StatCard(
                        title = stat.title,
                        value = stat.value,
                        icon = stat.icon,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }

        Text(
            text = "Trends",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        TrendLineChart(
            title = "Registered Users",
            currentValueLabel = AdminDataStore.users.size.toString(),
            values = AdminDataStore.usersTrend(),
            lineColor = PepsiClassicBlue,
        )
        TrendLineChart(
            title = "Registered Depos",
            currentValueLabel = AdminDataStore.depos.size.toString(),
            values = AdminDataStore.deposTrend(),
            lineColor = PepsiDarkBlue,
        )
        TrendLineChart(
            title = "System Health",
            currentValueLabel = "${AdminDataStore.systemHealthPercent.value}%",
            values = AdminDataStore.healthTrend(),
            lineColor = PepsiRed,
        )
    }
}
