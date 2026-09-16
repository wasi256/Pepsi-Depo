package com.example.pepsi.ui.workers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.pepsi.data.network.model.PersonnelRead
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun WorkersScreen(onMenuClick: () -> Unit) {
    WorkersContent(onMenuClick = onMenuClick)
}

@Composable
private fun WorkersContent(onMenuClick: () -> Unit) {
    var nameFilter by remember { mutableStateOf("") }
    var showFilter by remember { mutableStateOf(false) }
    var attendants by remember { mutableStateOf<List<PersonnelRead>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        FactoryRepository.fetchDepoAttendants()
            .onSuccess { attendants = it }
            .onFailure { errorMessage = it.message }
        isLoading = false
    }

    val filteredWorkers = remember(nameFilter, attendants) {
        if (nameFilter.isBlank()) {
            attendants
        } else {
            attendants.filter { it.name.contains(nameFilter, ignoreCase = true) }
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Depo Attendants",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { reloadKey++ }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showFilter = !showFilter }) {
                        Icon(Icons.Filled.Search, contentDescription = "Filter by name")
                    }
                }
            }
            if (showFilter) {
                item {
                    OutlinedTextField(
                        value = nameFilter,
                        onValueChange = { nameFilter = it },
                        label = { Text("Filter by name") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (errorMessage != null) {
                item {
                    Text(
                        text = errorMessage.orEmpty(),
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            } else if (filteredWorkers.isEmpty()) {
                item {
                    Text(
                        text = "No depo attendants found.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun WorkerRow(worker: PersonnelRead) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = worker.name, fontWeight = FontWeight.Bold)
            Text(text = "Tel: ${worker.contact}", style = MaterialTheme.typography.bodyMedium)
            worker.email?.let {
                Text(text = "Email: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Role: Depo Attendant", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
