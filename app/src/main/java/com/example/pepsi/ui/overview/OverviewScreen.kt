package com.example.pepsi.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.DeliveryStatus
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.data.sample.SampleData
import com.example.pepsi.data.state.DepoState
import com.example.pepsi.ui.components.KpiCard
import com.example.pepsi.ui.components.KpiData
import com.example.pepsi.ui.components.PepsiTopBar
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
    onProfileClick: () -> Unit,
    onViewStocks: () -> Unit,
    onSellProducts: () -> Unit,
    onReceiveProducts: () -> Unit,
) {
    val greeting = remember { greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
    val attendantFirstName = DepoState.profile.value.name.substringBefore(" ")

    val todaySales = DepoState.sales.filter { DepoState.matchesPeriod(it.date, RangePeriod.Today) }
    val todaySalesAmount = todaySales.sumOf { it.amount }
    val pendingDeliveries = DepoState.deliveries.filter { it.status == DeliveryStatus.Pending }
    val pendingUnits = pendingDeliveries.sumOf { delivery -> delivery.items.sumOf { it.quantitySent } }
    val totalStock = DepoState.stock.sumOf { it.quantity }

    val kpis = listOf(
        KpiData("Today's Sales", "UGX %,d".format(todaySalesAmount)),
        KpiData("Products Received (pending)", "$pendingUnits units"),
        KpiData("Total Stock Level", "$totalStock units"),
    )

    Scaffold(
        topBar = {
            PepsiTopBar(
                title = "Overview",
                onMenuClick = onMenuClick,
                showProfileAction = true,
                onProfileClick = onProfileClick,
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
                        text = "$greeting, $attendantFirstName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Here's what's happening at ${SampleData.profile.depoName} today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(kpis) { kpi ->
                KpiCard(data = kpi, modifier = Modifier.fillMaxWidth())
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
                    Button(onClick = onReceiveProducts, modifier = Modifier.fillMaxWidth()) {
                        Text("Receive Products (${pendingDeliveries.size} pending)")
                    }
                    Button(onClick = onSellProducts, modifier = Modifier.fillMaxWidth()) {
                        Text("Sell Products")
                    }
                    Button(onClick = onViewStocks, modifier = Modifier.fillMaxWidth()) {
                        Text("View Current Stock")
                    }
                }
            }
        }
    }
}
