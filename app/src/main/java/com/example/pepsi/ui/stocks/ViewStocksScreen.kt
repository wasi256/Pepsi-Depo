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
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.CurrentStockResponse
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.OnResume
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.util.formatApiDateTime

@Composable
fun ViewStocksScreen(onMenuClick: () -> Unit) {
    var stock by remember { mutableStateOf<List<CurrentStockResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }

    OnResume(key = AuthSession.depotId) {
        isLoading = true
        error = null
        try {
            val response = RetrofitClient.depotApi.listCurrentStock(AuthSession.depotId)
            if (response.isSuccessful) stock = response.body().orEmpty() else error = response.readErrorMessage()
        } catch (e: Exception) {
            error = "Network error: ${e.message}"
        }
        isLoading = false
    }

    val visible = stock.filter {
        search.isBlank() || it.product_name.orEmpty().contains(search.trim(), ignoreCase = true)
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
                Text(
                    text = "Current stock at ${stock.firstNotNullOfOrNull { it.depot_name } ?: "your depot"}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${stock.size} products · ${stock.sumOf { it.current_amount }} units in total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            item {
                OutlinedTextField(
                    value = search,
                    onValueChange = { search = it },
                    label = { Text("Search product") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            item {
                ListStatus(
                    isLoading = isLoading,
                    error = error,
                    isEmpty = !isLoading && error == null && visible.isEmpty(),
                    emptyText = if (stock.isEmpty()) "No stock recorded for this depot yet." else "No product matches your search.",
                )
            }
            items(visible, key = { it.id }) { item ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.product_name ?: "Product #${item.product_id}"} ${item.quantity_value.orEmpty()}".trim(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                text = "Updated ${formatApiDateTime(item.updated_at)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Text(
                            text = "${item.current_amount}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (item.current_amount <= 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}
