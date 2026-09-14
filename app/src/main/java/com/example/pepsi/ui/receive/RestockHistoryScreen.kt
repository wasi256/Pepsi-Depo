package com.example.pepsi.ui.receive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.network.DepotApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.RestockResponse
import com.example.pepsi.data.state.DepotSession
import com.example.pepsi.ui.components.DepotDropdown
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.util.formatApiDateTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private val statusOptions = listOf("All", "confirmed", "rejected")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestockHistoryScreen(onMenuClick: () -> Unit) {
    val depotApi = remember { RetrofitClient.createService(DepotApiService::class.java) }
    val scope = rememberCoroutineScope()

    val depots by DepotSession.depots
    val selectedDepot by DepotSession.selectedDepot
    val depotsLoading by DepotSession.isLoading

    var statusFilter by remember { mutableStateOf("All") }
    var statusExpanded by remember { mutableStateOf(false) }
    var productNameQuery by remember { mutableStateOf("") }
    var quantityQuery by remember { mutableStateOf("") }
    var fromDateMillis by remember { mutableStateOf<Long?>(null) }
    var toDateMillis by remember { mutableStateOf<Long?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }

    var entries by remember { mutableStateOf<List<RestockResponse>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun runQuery(targetPage: Int) {
        isLoading = true
        error = null
        scope.launch {
            try {
                val result = depotApi.getRestockHistory(
                    depotId = selectedDepot?.id,
                    status = statusFilter.takeIf { it != "All" },
                    productName = productNameQuery.trim().ifBlank { null },
                    quantity = quantityQuery.trim().ifBlank { null },
                    dateFrom = fromDateMillis?.let { apiDateFormat.format(it) },
                    dateTo = toDateMillis?.let { apiDateFormat.format(it) },
                    page = targetPage,
                    pageSize = 10,
                )
                entries = result.items
                page = result.page
                totalPages = result.totalPages.coerceAtLeast(1)
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Failed to load restock history."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        DepotSession.ensureLoaded(depotApi)
        runQuery(1)
    }

    if (showFromPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = fromDateMillis)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = { fromDateMillis = state.selectedDateMillis; showFromPicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }
    if (showToPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = toDateMillis)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = { toDateMillis = state.selectedDateMillis; showToPicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } },
        ) { DatePicker(state = state) }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Restock History", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    DepotDropdown(
                        depots = depots,
                        selectedDepot = selectedDepot,
                        onDepotSelected = DepotSession::select,
                        isLoading = depotsLoading,
                    )

                    OutlinedTextField(
                        value = productNameQuery,
                        onValueChange = { productNameQuery = it },
                        label = { Text("Search by product name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { runQuery(1) }),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = quantityQuery,
                        onValueChange = { quantityQuery = it },
                        label = { Text("Quantity (e.g. 320 ml)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { runQuery(1) }),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    ExposedDropdownMenuBox(
                        expanded = statusExpanded,
                        onExpandedChange = { statusExpanded = it },
                    ) {
                        OutlinedTextField(
                            value = statusFilter,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Status") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        )
                        ExposedDropdownMenu(
                            expanded = statusExpanded,
                            onDismissRequest = { statusExpanded = false },
                        ) {
                            statusOptions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.replaceFirstChar(Char::uppercase)) },
                                    onClick = { statusFilter = option; statusExpanded = false },
                                )
                            }
                        }
                    }

                    Text(
                        text = "Date range",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(onClick = { showFromPicker = true }, modifier = Modifier.weight(1f)) {
                            Text(fromDateMillis?.let { displayDateFormat.format(it) } ?: "From")
                        }
                        OutlinedButton(onClick = { showToPicker = true }, modifier = Modifier.weight(1f)) {
                            Text(toDateMillis?.let { displayDateFormat.format(it) } ?: "To")
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { runQuery(1) }, modifier = Modifier.weight(1f)) {
                            Text("Apply Filters")
                        }
                        OutlinedButton(
                            onClick = {
                                statusFilter = "All"
                                productNameQuery = ""
                                quantityQuery = ""
                                fromDateMillis = null
                                toDateMillis = null
                                runQuery(1)
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Clear")
                        }
                    }
                }
            }

            item {
                ListStatus(
                    isLoading = isLoading,
                    error = error,
                    isEmpty = !isLoading && error == null && entries.isEmpty(),
                    emptyText = "No restock entries match these filters.",
                )
            }

            items(entries, key = { it.id }) { entry ->
                RestockHistoryCard(entry)
            }

            if (!isLoading && error == null && entries.isNotEmpty()) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = { runQuery(page - 1) },
                            enabled = page > 1,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Previous")
                        }
                        Text(
                            text = "Page $page of $totalPages",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                        OutlinedButton(
                            onClick = { runQuery(page + 1) },
                            enabled = page < totalPages,
                            modifier = Modifier.weight(1f),
                        ) {
                            Text("Next")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RestockHistoryCard(entry: RestockResponse) {
    val statusLabel = entry.status.replaceFirstChar(Char::uppercase)
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${entry.productName} — $statusLabel", fontWeight = FontWeight.Bold)
            Text(
                text = "Quantity: ${entry.quantityDelivered} (${entry.quantityValue})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(text = "Depot: ${entry.depotName}", style = MaterialTheme.typography.bodyMedium)
            entry.rejectionReason?.let {
                Text(text = "Reason: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Date: ${formatApiDateTime(entry.restockDate)}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
