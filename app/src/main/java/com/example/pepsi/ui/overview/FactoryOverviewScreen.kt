package com.example.pepsi.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.ui.components.KpiCard
import com.example.pepsi.ui.components.KpiData
import com.example.pepsi.ui.components.PepsiTopBar
import java.util.Calendar

private fun greetingForHour(hour: Int): String = when (hour) {
    in 5..11 -> "Good Morning"
    in 12..16 -> "Good Afternoon"
    in 17..20 -> "Good Evening"
    else -> "Good Night"
}

private data class OverviewUiState(
    val totalProducts: Int? = null,
    val totalDepots: Int? = null,
    val totalStock: Int? = null,
    val pendingSupplies: Int? = null,
)

@Composable
fun FactoryOverviewScreen(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit,
    onViewProducts: () -> Unit,
    onViewDepos: () -> Unit,
) {
    val greeting = remember { greetingForHour(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }

    var uiState by remember { mutableStateOf(OverviewUiState()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        val productCount = FactoryRepository.fetchProductCount()
        val depotCount = FactoryRepository.fetchDepotCount()
        val stock = FactoryRepository.fetchStock()
        val supplies = FactoryRepository.fetchSupplyHistory(limit = 10)

        val firstFailure = listOf(productCount, depotCount, stock, supplies).firstOrNull { it.isFailure }
        if (firstFailure != null) {
            errorMessage = firstFailure.exceptionOrNull()?.message
        }
        uiState = OverviewUiState(
            totalProducts = productCount.getOrNull(),
            totalDepots = depotCount.getOrNull(),
            totalStock = stock.getOrNull()?.sumOf { it.availableQuantity },
            pendingSupplies = supplies.getOrNull()?.count { it.status == "pending" },
        )
        isLoading = false
    }

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
        if (isLoading && uiState.totalProducts == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
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
                    IconButton(onClick = { reloadKey++ }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            }

            errorMessage?.let {
                item {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            val kpis = listOf(
                KpiData("Total Products", uiState.totalProducts?.toString() ?: "—"),
                KpiData("Total Depos", uiState.totalDepots?.toString() ?: "—"),
                KpiData("Total Stock", uiState.totalStock?.toString() ?: "—"),
                KpiData("Pending Supplies", uiState.pendingSupplies?.toString() ?: "—"),
            )
            items(kpis.chunked(2)) { rowItems ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    rowItems.forEach { kpi ->
                        KpiCard(data = kpi, modifier = Modifier.weight(1f))
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
                    Button(onClick = onViewDepos, modifier = Modifier.fillMaxWidth()) {
                        Text("View Depos")
                    }
                    Button(onClick = onViewProducts, modifier = Modifier.fillMaxWidth()) {
                        Text("View Products & Stock")
                    }
                }
            }
        }
    }
}
