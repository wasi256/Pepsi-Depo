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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.navigation.Screen
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.CurrentSaleResponse
import com.example.pepsi.network.model.CurrentStockResponse
import com.example.pepsi.network.model.SupplyHistoryResponse
import com.example.pepsi.ui.components.KpiCard
import com.example.pepsi.ui.components.KpiData
import com.example.pepsi.ui.components.OnResume
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.formatMoney
import com.example.pepsi.ui.components.loadPendingSupplies
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
    val user = AuthSession.user
    val firstName = (user?.personnel_name ?: user?.username).orEmpty().substringBefore(" ")

    // null means "not available": still loading, or the user's role can't read it.
    var sales by remember { mutableStateOf<List<CurrentSaleResponse>?>(null) }
    var stock by remember { mutableStateOf<List<CurrentStockResponse>?>(null) }
    var pendingSupplies by remember { mutableStateOf<List<SupplyHistoryResponse>?>(null) }

    val depotId = AuthSession.depotId
    OnResume(key = depotId) {
        sales = runCatching {
            RetrofitClient.depotApi.listCurrentSales(depotId).takeIf { it.isSuccessful }?.body()
        }.getOrNull()
        stock = runCatching {
            RetrofitClient.depotApi.listCurrentStock(depotId).takeIf { it.isSuccessful }?.body()
        }.getOrNull()
        pendingSupplies = loadPendingSupplies().getOrNull()
    }

    val depotName = stock?.firstNotNullOfOrNull { it.depot_name } ?: sales?.firstNotNullOfOrNull { it.depot_name }
    val pendingUnits = pendingSupplies?.sumOf { it.amount }

    val kpis = listOf(
        KpiData("Today's Sales", sales?.let { formatMoney(it.sumOf { sale -> sale.sold_amount }) } ?: "—"),
        KpiData("Products Received (pending)", pendingUnits?.let { "$it units" } ?: "—"),
        KpiData("Total Stock Level", stock?.let { "${it.sumOf { item -> item.current_amount }} units" } ?: "—"),
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
                        text = if (firstName.isBlank()) greeting else "$greeting, $firstName",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Here's what's happening at ${depotName ?: "your depot"} today.",
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
                    if (AppAccess.canAccessRoute(Screen.ReceiveProducts.route)) {
                        Button(onClick = onReceiveProducts, modifier = Modifier.fillMaxWidth()) {
                            Text(pendingSupplies?.let { "Receive Products (${it.size} pending)" } ?: "Receive Products")
                        }
                    }
                    if (AppAccess.canAccessRoute(Screen.SellProducts.route)) {
                        Button(onClick = onSellProducts, modifier = Modifier.fillMaxWidth()) {
                            Text("Sell Products")
                        }
                    }
                    Button(onClick = onViewStocks, modifier = Modifier.fillMaxWidth()) {
                        Text("View Current Stock")
                    }
                }
            }
        }
    }
}
