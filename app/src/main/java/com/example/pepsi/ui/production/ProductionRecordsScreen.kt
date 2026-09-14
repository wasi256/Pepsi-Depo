package com.example.pepsi.ui.production

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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.pepsi.data.network.FactoryApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.ProductionResponse
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.util.formatApiDateTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private const val PAGE_SIZE = 10
private const val MAX_RANGE_DAYS = 31

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}
private val displayDateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun millisToApiDate(millis: Long): String = apiDateFormat.format(millis)
private fun millisToDisplayDate(millis: Long): String = displayDateFormat.format(millis)

private fun daysBetweenInclusive(fromMillis: Long, toMillis: Long): List<Long> {
    val days = mutableListOf<Long>()
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = fromMillis }
    while (calendar.timeInMillis <= toMillis) {
        days.add(calendar.timeInMillis)
        calendar.add(Calendar.DAY_OF_MONTH, 1)
    }
    return days
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionRecordsScreen(onMenuClick: () -> Unit) {
    val apiService = remember { RetrofitClient.createService(FactoryApiService::class.java) }
    val scope = rememberCoroutineScope()

    var productNameQuery by remember { mutableStateOf("") }
    var quantityQuery by remember { mutableStateOf("") }
    var fromDateMillis by remember { mutableStateOf<Long?>(null) }
    var toDateMillis by remember { mutableStateOf<Long?>(null) }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var rangeError by remember { mutableStateOf<String?>(null) }

    var records by remember { mutableStateOf<List<ProductionResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var canLoadMore by remember { mutableStateOf(false) }
    var isRangeMode by remember { mutableStateOf(false) }

    suspend fun runSingleQuery(skip: Int): List<ProductionResponse> {
        return apiService.getProductionHistory(
            skip = skip,
            limit = PAGE_SIZE,
            date = fromDateMillis?.let { millisToApiDate(it) }.takeIf { toDateMillis == null },
            productName = productNameQuery.trim().ifBlank { null },
            quantity = quantityQuery.toIntOrNull(),
        )
    }

    suspend fun runRangeQuery(fromMillis: Long, toMillis: Long): List<ProductionResponse> {
        val merged = mutableListOf<ProductionResponse>()
        for (dayMillis in daysBetweenInclusive(fromMillis, toMillis)) {
            val page = apiService.getProductionHistory(
                skip = 0,
                limit = PAGE_SIZE,
                date = millisToApiDate(dayMillis),
                productName = productNameQuery.trim().ifBlank { null },
                quantity = quantityQuery.toIntOrNull(),
            )
            merged += page
        }
        return merged.sortedByDescending { it.productionDate ?: it.createdDate }
    }

    fun applyFilters() {
        val from = fromDateMillis
        val to = toDateMillis
        rangeError = null

        if (from != null && to != null && from > to) {
            rangeError = "The 'From' date must be before the 'To' date."
            return
        }
        if (from != null && to != null) {
            val spanDays = ((to - from) / (24 * 60 * 60 * 1000)) + 1
            if (spanDays > MAX_RANGE_DAYS) {
                rangeError = "Please pick a range of $MAX_RANGE_DAYS days or fewer."
                return
            }
        }

        isLoading = true
        error = null
        scope.launch {
            try {
                if (from != null && to != null) {
                    isRangeMode = true
                    records = runRangeQuery(from, to)
                    canLoadMore = false
                } else {
                    isRangeMode = false
                    val page = runSingleQuery(skip = 0)
                    records = page
                    canLoadMore = page.size == PAGE_SIZE
                }
            } catch (e: Exception) {
                error = e.localizedMessage ?: "Failed to load production records."
            } finally {
                isLoading = false
            }
        }
    }

    fun clearFilters() {
        productNameQuery = ""
        quantityQuery = ""
        fromDateMillis = null
        toDateMillis = null
        rangeError = null
        applyFilters()
    }

    LaunchedEffect(Unit) {
        try {
            val page = runSingleQuery(skip = 0)
            records = page
            canLoadMore = page.size == PAGE_SIZE
        } catch (e: Exception) {
            error = e.localizedMessage ?: "Failed to load production records."
        } finally {
            isLoading = false
        }
    }

    if (showFromPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = fromDateMillis)
        DatePickerDialog(
            onDismissRequest = { showFromPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fromDateMillis = state.selectedDateMillis
                    showFromPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showFromPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }
    if (showToPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = toDateMillis)
        DatePickerDialog(
            onDismissRequest = { showToPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    toDateMillis = state.selectedDateMillis
                    showToPicker = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showToPicker = false }) { Text("Cancel") } },
        ) {
            DatePicker(state = state)
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Production Records", onMenuClick = onMenuClick) },
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
                    OutlinedTextField(
                        value = productNameQuery,
                        onValueChange = { productNameQuery = it },
                        label = { Text("Search by product name") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (productNameQuery.isNotEmpty()) {
                                IconButton(onClick = { productNameQuery = ""; applyFilters() }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { applyFilters() }),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    OutlinedTextField(
                        value = quantityQuery,
                        onValueChange = { quantityQuery = it.filter(Char::isDigit) },
                        label = { Text("Quantity produced") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Search,
                        ),
                        keyboardActions = KeyboardActions(onSearch = { applyFilters() }),
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Text(
                        text = "Date range",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        OutlinedButton(
                            onClick = { showFromPicker = true },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(fromDateMillis?.let { millisToDisplayDate(it) } ?: "From")
                        }
                        OutlinedButton(
                            onClick = { showToPicker = true },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(toDateMillis?.let { millisToDisplayDate(it) } ?: "To")
                        }
                    }
                    rangeError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (isRangeMode) {
                        Text(
                            text = "Showing up to $PAGE_SIZE records per day across the selected range.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { applyFilters() }, modifier = Modifier.weight(1f)) {
                            Text("Apply Filters")
                        }
                        OutlinedButton(onClick = { clearFilters() }, modifier = Modifier.weight(1f)) {
                            Text("Clear")
                        }
                    }
                }
            }

            item {
                ListStatus(
                    isLoading = isLoading,
                    error = error,
                    isEmpty = !isLoading && error == null && records.isEmpty(),
                    emptyText = "No production records match these filters.",
                )
            }

            items(records.size) { index ->
                val record = records[index]
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = record.productName, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Quantity Produced: ${record.quantityProduced}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        record.productionDate?.let {
                            Text(text = "Produced: ${formatApiDateTime(it)}", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(
                            text = "Recorded: ${formatApiDateTime(record.createdDate)}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            if (!isLoading && error == null && canLoadMore && !isRangeMode) {
                item {
                    Button(
                        onClick = {
                            isLoadingMore = true
                            scope.launch {
                                try {
                                    val page = runSingleQuery(skip = records.size)
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
