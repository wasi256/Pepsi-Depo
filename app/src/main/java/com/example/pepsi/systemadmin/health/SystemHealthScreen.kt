package com.example.pepsi.systemadmin.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.ui.components.PepsiTopBar

private data class HealthMetric(val label: String, val percent: Int)

private val subMetrics = listOf(
    HealthMetric("Server uptime", 99),
    HealthMetric("API response", 97),
    HealthMetric("Database", 95),
    HealthMetric("Storage", 88),
)

@Composable
fun SystemHealthScreen(onMenuClick: () -> Unit) {
    val overall = AdminDataStore.systemHealthPercent.value

    Scaffold(
        topBar = { PepsiTopBar(title = "System Health", onMenuClick = onMenuClick) },
    ) { padding ->
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(padding),
    ) {
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "$overall%",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                    Text(text = "Overall system health", style = MaterialTheme.typography.bodyMedium)
                    LinearProgressIndicator(
                        progress = { overall / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    )
                }
            }
        }
        items(subMetrics) { metric ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = metric.label, style = MaterialTheme.typography.bodyLarge)
                        Text(text = "${metric.percent}%", style = MaterialTheme.typography.bodyLarge)
                    }
                    LinearProgressIndicator(
                        progress = { metric.percent / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    )
                }
            }
        }
    }
    }
}
