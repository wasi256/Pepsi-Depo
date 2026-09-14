package com.example.pepsi.ui.supply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.network.FactoryApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.SupplyResponse
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.util.formatApiDateTime
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10

@Composable
fun SupplyRecordsScreen(onMenuClick: () -> Unit) {
    val apiService = remember { RetrofitClient.createService(FactoryApiService::class.java) }
    val scope = rememberCoroutineScope()

    var records by remember { mutableStateOf<List<SupplyResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var canLoadMore by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val page = apiService.getSupplyHistory(skip = 0, limit = PAGE_SIZE)
            records = page
            canLoadMore = page.size == PAGE_SIZE
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Failed to load supply records."
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Supply Records", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                ListStatus(
                    isLoading = isLoading,
                    error = error,
                    isEmpty = !isLoading && error == null && records.isEmpty(),
                    emptyText = "No supply records yet.",
                )
            }

            items(records.size) { index ->
                val record = records[index]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = record.productName, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Amount: ${record.amount} (${record.quantityValue})",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Status: ${record.status.replaceFirstChar(Char::uppercase)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = when (record.status) {
                                "received" -> MaterialTheme.colorScheme.primary
                                "rejected" -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            },
                        )
                        record.rejectionReason?.let {
                            Text(text = "Reason: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "Recorded: ${formatApiDateTime(record.createdDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (!isLoading && error == null && canLoadMore) {
                item {
                    Button(
                        onClick = {
                            isLoadingMore = true
                            scope.launch {
                                try {
                                    val page = apiService.getSupplyHistory(skip = records.size, limit = PAGE_SIZE)
                                    records = records + page
                                    canLoadMore = page.size == PAGE_SIZE
                                } catch (e: Exception) {
                                    error = e.localizedMessage ?: "Failed to load more records."
                                } finally {
                                    isLoadingMore = false
                                }
                            }
                        },
                        enabled = !isLoadingMore,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isLoadingMore) "Loading…" else "Load More")
                    }
                }
            }
        }
    }
}
