package com.example.pepsi.ui.saleshistory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.data.model.SaleRecord
import com.example.pepsi.data.state.DepoState
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.RangePeriodSelector

@Composable
fun SalesHistoryScreen(onMenuClick: () -> Unit) {
    var selectedPeriod by remember { mutableStateOf(RangePeriod.Today) }
    var expandedIndex by remember { mutableStateOf<Int?>(null) }

    val filtered = DepoState.sales.filter { DepoState.matchesPeriod(it.date, selectedPeriod) }
    val totalUnits = filtered.sumOf { it.quantity }
    val totalAmount = filtered.sumOf { it.amount }

    Scaffold(
        topBar = { PepsiTopBar(title = "Sales History", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RangePeriodSelector(
                    selected = selectedPeriod,
                    onSelect = {
                        selectedPeriod = it
                        expandedIndex = null
                    },
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "${selectedPeriod.label} Summary",
                            fontWeight = FontWeight.Bold,
                        )
                        Text(text = "Units sold: $totalUnits")
                        Text(text = "Amount sold: UGX %,d".format(totalAmount))
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    Text(
                        text = "No sales recorded for ${selectedPeriod.label.lowercase()}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            itemsIndexed(filtered) { index, sale ->
                SaleHistoryRow(
                    sale = sale,
                    expanded = expandedIndex == index,
                    onClick = { expandedIndex = if (expandedIndex == index) null else index },
                )
            }
        }
    }
}

@Composable
private fun SaleHistoryRow(sale: SaleRecord, expanded: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = sale.productName,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(text = "Qty: ${sale.quantity}")
            }
            Text(
                text = "Amount: UGX %,d".format(sale.amount),
                style = MaterialTheme.typography.bodyMedium,
            )
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Date: ${sale.date}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Unit price: UGX %,d".format(sale.amount / sale.quantity.coerceAtLeast(1)),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
