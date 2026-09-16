package com.example.pepsi.ui.depos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.example.pepsi.data.network.model.DepotRead
import com.example.pepsi.data.network.model.SupplyResponse
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun DeposScreen(onMenuClick: () -> Unit) {
    var selectedDepotId by remember { mutableStateOf<Int?>(null) }
    var depots by remember { mutableStateOf<List<DepotRead>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        FactoryRepository.fetchDepots()
            .onSuccess { depots = it }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Depos",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Tap a depo to view what's been supplied to it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = { reloadKey++ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                }
            }

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            if (isLoading && depots.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (depots.isEmpty()) {
                Text(
                    text = "No depos found.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            depots.forEach { depot ->
                val isSelected = depot.id == selectedDepotId
                DepoRow(
                    depot = depot,
                    isSelected = isSelected,
                    onClick = {
                        selectedDepotId = if (isSelected) null else depot.id
                    },
                )
                if (isSelected) {
                    SuppliedToDepotSection(depot = depot)
                }
            }
        }
    }
}

@Composable
private fun DepoRow(depot: DepotRead, isSelected: Boolean, onClick: () -> Unit) {
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
            Text(text = depot.name, fontWeight = FontWeight.Bold)
            Text(text = "Location: ${depot.location}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SuppliedToDepotSection(depot: DepotRead) {
    var supplies by remember(depot.id) { mutableStateOf<List<SupplyResponse>>(emptyList()) }
    var isLoading by remember(depot.id) { mutableStateOf(true) }
    var errorMessage by remember(depot.id) { mutableStateOf<String?>(null) }

    LaunchedEffect(depot.id) {
        isLoading = true
        errorMessage = null
        FactoryRepository.fetchSupplyHistory(depotId = depot.id)
            .onSuccess { supplies = it }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Text(
            text = "Supplied to ${depot.name}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(16.dp),
        )
    }
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Product", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = "Amount", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(text = "Status", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                supplies.isEmpty() -> {
                    Text(
                        text = "Nothing supplied to this depo yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )
                }
                else -> {
                    supplies.forEach { supply ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Text(text = supply.productName, modifier = Modifier.weight(1f))
                            Text(
                                text = "${supply.amount} × ${supply.quantityValue}",
                                modifier = Modifier.weight(1f),
                            )
                            Text(text = supply.status, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}
