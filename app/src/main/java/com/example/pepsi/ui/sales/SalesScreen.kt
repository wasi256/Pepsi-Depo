package com.example.pepsi.ui.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.Sale
import com.example.pepsi.data.sample.FactorySampleData
import com.example.pepsi.ui.components.HourlySalesBarChart
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun SalesScreen(onMenuClick: () -> Unit) {
    Scaffold(
        topBar = { PepsiTopBar(title = "Sales", onMenuClick = onMenuClick) },
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
                    text = "Units Sold by Time of Day",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    HourlySalesBarChart(
                        data = FactorySampleData.hourlySales,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }
            item {
                Text(
                    text = "Sales Records",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            items(FactorySampleData.sales) { sale ->
                SaleRow(sale)
            }
        }
    }
}

@Composable
private fun SaleRow(sale: Sale) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = sale.productName, fontWeight = FontWeight.Bold)
            Text(text = "Quantity: ${sale.quantity}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Depo: ${sale.depoName}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Date: ${sale.date}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
