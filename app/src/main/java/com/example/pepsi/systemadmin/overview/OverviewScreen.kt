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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.components.StatCard
import com.example.pepsi.common.components.TrendLineChart
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.theme.PepsiElectricBlue
import com.example.pepsi.theme.PepsiRed
import com.example.pepsi.ui.components.PepsiTopBar

private data class OverviewStat(val title: String, val value: String, val icon: ImageVector)

@Composable
fun OverviewScreen(onMenuClick: () -> Unit) {
    var depotCount by remember { mutableIntStateOf(0) }
    var depotCountLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.adminApi.listDepots()
            if (response.isSuccessful) {
                depotCount = response.body()?.total ?: 0
                depotCountLoaded = true
            }
        } catch (e: Exception) {
            // Leave the depot count blank; the rest of the overview still renders.
        }
    }

    val stats = listOf(
        OverviewStat("Registered Users", AdminDataStore.users.size.toString(), Icons.Filled.Group),
        OverviewStat("Registered Depos", if (depotCountLoaded) depotCount.toString() else "—", Icons.Filled.Store),
        OverviewStat("Audit Logs", AdminDataStore.auditLogs.size.toString(), Icons.Filled.History),
        OverviewStat("System Health", "${AdminDataStore.systemHealthPercent.value}%", Icons.Filled.MonitorHeart),
    )

    Scaffold(
        topBar = { PepsiTopBar(title = "Admin Overview", onMenuClick = onMenuClick) },
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
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
            lineColor = PepsiElectricBlue,
        )
        TrendLineChart(
            title = "System Health",
            currentValueLabel = "${AdminDataStore.systemHealthPercent.value}%",
            values = AdminDataStore.healthTrend(),
            lineColor = PepsiRed,
        )
    }
    }
}
