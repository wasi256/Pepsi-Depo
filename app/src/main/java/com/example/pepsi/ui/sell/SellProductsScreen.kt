package com.example.pepsi.ui.sell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.ChartEntry
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.data.sample.SampleData
import com.example.pepsi.data.state.DepoState
import com.example.pepsi.ui.components.EntryBarChart
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.RangePeriodSelector

@Composable
fun SellProductsScreen(onMenuClick: () -> Unit) {
    val quantities = remember { mutableStateOf(SampleData.products.associate { it.name to "" }) }
    var confirmation by remember { mutableStateOf<String?>(null) }
    var chartPeriod by remember { mutableStateOf(RangePeriod.Today) }

    val unitPrices = remember { SampleData.products.associate { it.name to it.unitPrice } }

    val filteredSales = DepoState.sales.filter { DepoState.matchesPeriod(it.date, chartPeriod) }
    val perProductEntries = SampleData.products.map { product ->
        ChartEntry(
            label = product.name.substringBefore(" "),
            value = filteredSales.filter { it.productName == product.name }.sumOf { it.quantity },
        )
    }
    val totalUnits = perProductEntries.sumOf { it.value }

    Scaffold(
        topBar = { PepsiTopBar(title = "Sell Products", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Record a Sale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            items(SampleData.products, key = { it.name }) { product ->
                val available = DepoState.stockQuantity(product.name)
                ProductQuantityRow(
                    productName = product.name,
                    available = available,
                    quantity = quantities.value[product.name] ?: "",
                    onQuantityChange = { value ->
                        val qty = value.filter(Char::isDigit)
                        quantities.value = quantities.value.toMutableMap().apply { this[product.name] = qty }
                    },
                )
            }

            item {
                Button(
                    onClick = {
                        val items = quantities.value.map { (name, qty) -> name to (qty.toIntOrNull() ?: 0) }
                        val created = DepoState.sellProducts(items, unitPrices)
                        if (created.isNotEmpty()) {
                            val totalAmount = created.sumOf { it.amount }
                            confirmation = "Sold ${created.sumOf { it.quantity }} units for UGX %,d.".format(totalAmount)
                            quantities.value = SampleData.products.associate { it.name to "" }
                        }
                    },
                    enabled = quantities.value.values.any { (it.toIntOrNull() ?: 0) > 0 },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Confirm Sold")
                }
            }

            confirmation?.let { message ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Text(text = message, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Text(
                    text = "Sales Rate by Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                RangePeriodSelector(selected = chartPeriod, onSelect = { chartPeriod = it })
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total units sold (${chartPeriod.label.lowercase()}): $totalUnits",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        EntryBarChart(data = perProductEntries, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductQuantityRow(
    productName: String,
    available: Int,
    quantity: String,
    onQuantityChange: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = productName, fontWeight = FontWeight.Bold)
            Text(
                text = "Available in stock: $available",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = onQuantityChange,
                    label = { Text("Quantity to Sell") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                )
                Column(modifier = Modifier.height(56.dp), verticalArrangement = Arrangement.SpaceBetween) {
                    IconButton(
                        onClick = {
                            val next = ((quantity.toIntOrNull() ?: 0) + 1).coerceAtMost(available)
                            onQuantityChange(next.toString())
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase quantity")
                    }
                    IconButton(
                        onClick = {
                            val next = ((quantity.toIntOrNull() ?: 0) - 1).coerceAtLeast(0)
                            onQuantityChange(next.toString())
                        },
                        modifier = Modifier.size(28.dp),
                    ) {
                        Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease quantity")
                    }
                }
            }
        }
    }
}
