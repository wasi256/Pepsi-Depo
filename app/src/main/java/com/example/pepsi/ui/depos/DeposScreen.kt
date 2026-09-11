package com.example.pepsi.ui.depos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.pepsi.data.model.Depo
import com.example.pepsi.data.model.StockLevel
import com.example.pepsi.data.sample.SampleData
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun DeposScreen(onMenuClick: () -> Unit) {
    var selectedDepoName by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { PepsiTopBar(title = "Depos", onMenuClick = onMenuClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Depos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "Tap a depo to view its current stock",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            SampleData.depos.forEach { depo ->
                val isSelected = depo.name == selectedDepoName
                DepoRow(
                    depo = depo,
                    isSelected = isSelected,
                    onClick = {
                        selectedDepoName = if (isSelected) null else depo.name
                    },
                )
                if (isSelected) {
                    CurrentStockSection(
                        depoName = depo.name,
                        stock = SampleData.depoStock[depo.name].orEmpty(),
                    )
                }
            }
        }
    }
}

@Composable
private fun DepoRow(depo: Depo, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = if (isSelected) {
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = depo.name, fontWeight = FontWeight.Bold)
            Text(text = "Location: ${depo.location}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Attendant: ${depo.attendantName}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun CurrentStockSection(depoName: String, stock: List<StockLevel>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            text = "Current Stock — $depoName",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
        )
    }
    StockTable(stock)
}

@Composable
private fun StockTable(stock: List<StockLevel>) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Product",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "Stock Level",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            if (stock.isEmpty()) {
                Text(
                    text = "No stock recorded for this depo.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            }
            stock.forEach { item ->
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(text = item.productName, modifier = Modifier.weight(1f))
                    Text(text = item.stockLevel.toString(), modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
