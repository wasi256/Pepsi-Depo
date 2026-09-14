package com.example.pepsi.ui.overview

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.example.pepsi.data.network.FactoryApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.FactoryStockDto
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.PepsiTopBar
import java.util.Calendar

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..20 -> "Good Evening"
    else -> "Good Night"
}

@Composable
fun FactoryOverviewScreen(
    onMenuClick: () -> Unit,
    onRecordProduction: () -> Unit,
    onRecordSupply: () -> Unit,
    onViewProductionRecords: () -> Unit,
    onViewSupplyRecords: () -> Unit,
) {
    val apiService = remember { RetrofitClient.createService(FactoryApiService::class.java) }
    val greeting = remember { greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }

    var stock by remember { mutableStateOf<List<FactoryStockDto>>(emptyList()) }
    var stockLoading by remember { mutableStateOf(true) }
    var stockError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            stock = apiService.getFactoryStock()
        } catch (e: Exception) {
            stockError = e.localizedMessage ?: "Failed to load factory stock."
        } finally {
            stockLoading = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Overview", onMenuClick = onMenuClick) },
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
                        text = greeting,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Here's what's happening at the factory today.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            item {
                Text(
                    text = "Factory Current Stock",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                ListStatus(
                    isLoading = stockLoading,
                    error = stockError,
                    isEmpty = !stockLoading && stockError == null && stock.isEmpty(),
                    emptyText = "No factory stock recorded yet.",
                )
            }
            if (!stockLoading && stockError == null && stock.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        stock.forEach { item ->
                            Card(modifier = Modifier.width(160.dp)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = item.productName, fontWeight = FontWeight.Bold)
                                    Text(
                                        text = "Available: ${item.availableQuantity}",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                        }
                    }
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
                    Button(onClick = onRecordProduction, modifier = Modifier.fillMaxWidth()) {
                        Text("Record Production")
                    }
                    Button(onClick = onRecordSupply, modifier = Modifier.fillMaxWidth()) {
                        Text("Record Supply")
                    }
                    Button(onClick = onViewProductionRecords, modifier = Modifier.fillMaxWidth()) {
                        Text("View Production Records")
                    }
                    Button(onClick = onViewSupplyRecords, modifier = Modifier.fillMaxWidth()) {
                        Text("View Supply Records")
                    }
                }
            }
        }
    }
}
