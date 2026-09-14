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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.DeliveryStatus
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.data.state.DepoState
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.RangePeriodSelector

@Composable
fun ViewStocksScreen(onMenuClick: () -> Unit) {
    var selectedPeriod by remember { mutableStateOf(RangePeriod.Today) }

    val soldInPeriod = DepoState.sales
        .filter { DepoState.matchesPeriod(it.date, selectedPeriod) }
        .groupBy { it.productName }
        .mapValues { (_, records) -> records.sumOf { it.quantity } }

    val receivedInPeriod = DepoState.deliveries
        .filter { it.status == DeliveryStatus.Confirmed && DepoState.matchesPeriod(it.date, selectedPeriod) }
        .flatMap { it.items }
        .groupBy { it.productName }
        .mapValues { (_, items) -> items.sumOf { it.quantitySent } }

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
                RangePeriodSelector(selected = selectedPeriod, onSelect = { selectedPeriod = it })
            }
            item {
                Text(
                    text = "Current stock, with movement for ${selectedPeriod.label.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                StockTableHeader()
            }
            items(DepoState.stock, key = { it.productName }) { stockItem ->
                StockRow(
                    productName = stockItem.productName,
                    quantity = stockItem.quantity,
                    sold = soldInPeriod[stockItem.productName] ?: 0,
                    received = receivedInPeriod[stockItem.productName] ?: 0,
                )
            }
        }
    }
}

@Composable
private fun StockTableHeader() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(text = "Product", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
            Text(text = "Available", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "Sold", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            Text(text = "Received", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StockRow(productName: String, quantity: Int, sold: Int, received: Int) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column {
            Row(modifier = Modifier.padding(12.dp)) {
                Text(text = productName, modifier = Modifier.weight(2f))
                Text(text = "$quantity", modifier = Modifier.weight(1f))
                Text(text = "$sold", modifier = Modifier.weight(1f))
                Text(text = "$received", modifier = Modifier.weight(1f))
            }
            HorizontalDivider()
        }
    }
}
