package com.example.pepsi.ui.stocks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.network.DepotApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.DepotStockDto
import com.example.pepsi.data.state.DepotSession
import com.example.pepsi.ui.components.DepotDropdown
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.util.formatApiDateTime

@Composable
fun ViewStocksScreen(onMenuClick: () -> Unit) {
    val depotApi = remember { RetrofitClient.createService(DepotApiService::class.java) }

    val depots by DepotSession.depots
    val selectedDepot by DepotSession.selectedDepot
    val depotsLoading by DepotSession.isLoading

    var stock by remember { mutableStateOf<List<DepotStockDto>>(emptyList()) }
    var stockLoading by remember { mutableStateOf(true) }
    var stockError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        DepotSession.ensureLoaded(depotApi)
    }

    LaunchedEffect(selectedDepot) {
        stockLoading = true
        stockError = null
        try {
            stock = depotApi.getDepotStock(depotId = selectedDepot?.id)
        } catch (e: Exception) {
            stockError = e.localizedMessage ?: "Failed to load stock."
        } finally {
            stockLoading = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "View Stocks", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                DepotDropdown(
                    depots = depots,
                    selectedDepot = selectedDepot,
                    onDepotSelected = DepotSession::select,
                    isLoading = depotsLoading,
                )
            }
            item {
                ListStatus(
                    isLoading = stockLoading,
                    error = stockError,
                    isEmpty = !stockLoading && stockError == null && stock.isEmpty(),
                    emptyText = "No stock recorded for this depot.",
                )
            }
            if (!stockLoading && stockError == null && stock.isNotEmpty()) {
                item { StockTableHeader() }
                items(stock, key = { it.id }) { item ->
                    StockRow(item)
                }
            }
        }
    }
}

@Composable
private fun StockTableHeader() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(text = "Product", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
            Text(text = "Size", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "Available", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StockRow(item: DepotStockDto) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.padding(12.dp)) {
                Text(text = item.productName, modifier = Modifier.weight(2f))
                Text(text = item.quantityValue, modifier = Modifier.weight(1f))
                Text(text = "${item.currentAmount}", modifier = Modifier.weight(1f))
            }
            Text(
                text = "Updated: ${formatApiDateTime(item.updatedAt)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 8.dp),
            )
            HorizontalDivider()
        }
    }
}
