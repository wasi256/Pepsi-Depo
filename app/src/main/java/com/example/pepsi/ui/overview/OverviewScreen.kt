package com.example.pepsi.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.TrendPeriod
import com.example.pepsi.data.sample.SampleData
import com.example.pepsi.ui.components.KpiCard
import com.example.pepsi.ui.components.KpiData
import com.example.pepsi.ui.components.LineChart
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.TrendPeriodSelector
import java.util.Calendar

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..20 -> "Good Evening"
    else -> "Good Night"
}

@Composable
fun OverviewScreen(
    onMenuClick: () -> Unit,
    onViewCurrentStock: () -> Unit,
    onViewHistory: () -> Unit,
    onAdminClick: () -> Unit = {},
) {
    val kpis = listOf(
        KpiData("Total Distributions", SampleData.totalDistributions),
        KpiData("Total Sales", SampleData.totalSales),
        KpiData("Total Depos", SampleData.totalDepos),
        KpiData("Total Productions", SampleData.totalProductions),
    )
    var selectedPeriod by remember { mutableStateOf(TrendPeriod.Monthly) }
    val greeting = remember { greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    val managerFirstName = remember { SampleData.manager.name.substringBefore(" ") }

    Scaffold(
        topBar = {
            PepsiTopBar(
                title = "Overview",
                onMenuClick = onMenuClick,
                showActions = true,
                onAdminClick = onAdminClick,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column {
                    Text(
                        text = "$greeting, $managerFirstName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Here's what's happening across Pepsi Depo today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(kpis.chunked(2)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rowItems.forEach { kpi ->
                        KpiCard(data = kpi, modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                TrendPeriodSelector(
                    selected = selectedPeriod,
                    onSelect = { selectedPeriod = it },
                )
            }

            item {
                Text(
                    text = "Products Sold",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    LineChart(
                        data = SampleData.productsSoldTrends.getValue(selectedPeriod),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            item {
                Text(
                    text = "Products Manufactured",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    LineChart(
                        data = SampleData.productsManufacturedTrends.getValue(selectedPeriod),
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            item {
                Text(
                    text = "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = onViewCurrentStock, modifier = Modifier.fillMaxWidth()) {
                        Text("View Current Stock")
                    }
                    Button(onClick = onViewHistory, modifier = Modifier.fillMaxWidth()) {
                        Text("History of Supply and Production")
                    }
                }
            }
        }
    }
}
