package com.example.pepsi.ui.workers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.Worker
import com.example.pepsi.data.model.WorkerRole
import com.example.pepsi.data.sample.FactorySampleData
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun WorkersScreen(onMenuClick: () -> Unit) {
    var roleFilter by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }

    val filteredWorkers = remember(roleFilter) {
        if (roleFilter.isBlank()) {
            FactorySampleData.workers
        } else {
            FactorySampleData.workers.filter {
                it.role.displayName().contains(roleFilter, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Workers", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Row {
                    Text(
                        text = "Users",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(Icons.Filled.Search, contentDescription = "Filter by role")
                    }
                }
            }
            if (showFilter) {
                item {
                    OutlinedTextField(
                        value = roleFilter,
                        onValueChange = { roleFilter = it },
                        label = { Text("Filter by role") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
            items(filteredWorkers) { worker ->
                WorkerRow(worker)
            }
        }
    }
}

@Composable
private fun WorkerRow(worker: Worker) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = worker.name, fontWeight = FontWeight.Bold)
            Text(text = "Tel: ${worker.telephone}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Role: ${worker.role.displayName()}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private fun WorkerRole.displayName(): String = when (this) {
    WorkerRole.SystemAdmin -> "System Admin"
    WorkerRole.Factory -> "Factory"
    WorkerRole.DepoAttendant -> "Depo Attendant"
}
