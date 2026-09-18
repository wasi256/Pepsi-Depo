package com.example.pepsi.ui.saleshistory

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.auth.PermissionModule
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.SaleResponse
import com.example.pepsi.network.model.SaleUpdateRequest
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.LoadMoreButton
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.RangePeriodSelector
import com.example.pepsi.ui.components.dateFrom
import com.example.pepsi.ui.components.formatMoney
import com.example.pepsi.ui.components.todayString
import com.example.pepsi.util.formatApiDateTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 20

@Composable
fun SalesHistoryScreen(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val canEdit = AppAccess.canUpdate(PermissionModule.DEPOT_SALES)
    val canDelete = AppAccess.canDelete(PermissionModule.DEPOT_SALES)

    var selectedPeriod by remember { mutableStateOf(RangePeriod.Today) }
    var search by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Int?>(null) }

    var sales by remember { mutableStateOf<List<SaleResponse>>(emptyList()) }
    var total by remember { mutableIntStateOf(0) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var reloadTick by remember { mutableIntStateOf(0) }

    var editing by remember { mutableStateOf<SaleResponse?>(null) }
    var deleting by remember { mutableStateOf<SaleResponse?>(null) }

    suspend fun fetch(pageNumber: Int) = RetrofitClient.depotApi.listSales(
        depotId = AuthSession.depotId,
        productName = search.trim().ifBlank { null },
        dateFrom = selectedPeriod.dateFrom(),
        dateTo = todayString(),
        page = pageNumber,
        pageSize = PAGE_SIZE,
    )

    LaunchedEffect(selectedPeriod, search, reloadTick, AuthSession.depotId) {
        if (search.isNotBlank()) delay(350) // debounce typing
        isLoading = true
        error = null
        try {
            val response = fetch(1)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                sales = body.items
                total = body.total
                page = body.page
                totalPages = body.total_pages
            } else {
                sales = emptyList()
                total = 0
                error = response.readErrorMessage()
            }
        } catch (e: Exception) {
            sales = emptyList()
            error = "Network error: ${e.message}"
        }
        isLoading = false
    }

    fun loadMore() {
        scope.launch {
            isLoadingMore = true
            try {
                val response = fetch(page + 1)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    sales = sales + body.items
                    page = body.page
                    totalPages = body.total_pages
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            isLoadingMore = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Sales History", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                RangePeriodSelector(
                    selected = selectedPeriod,
                    onSelect = {
                        selectedPeriod = it
                        expandedId = null
                    },
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
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "${selectedPeriod.label} Summary", fontWeight = FontWeight.Bold)
                        Text(text = "Sales recorded: $total")
                        Text(text = "Units sold: ${sales.sumOf { it.quantity_sold }}")
                        Text(text = "Amount sold: ${formatMoney(sales.sumOf { it.amount_sold })}")
                        if (sales.size < total) {
                            Text(
                                text = "Totals cover the ${sales.size} sales loaded so far.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }
            }
            item {
                ListStatus(
                    isLoading = isLoading,
                    error = error,
                    isEmpty = !isLoading && error == null && sales.isEmpty(),
                    emptyText = "No sales recorded for ${selectedPeriod.label.lowercase()}.",
                )
            }
            items(sales, key = { it.id }) { sale ->
                SaleHistoryRow(
                    sale = sale,
                    expanded = expandedId == sale.id,
                    onClick = { expandedId = if (expandedId == sale.id) null else sale.id },
                    onEdit = if (canEdit) ({ editing = sale }) else null,
                    onDelete = if (canDelete) ({ deleting = sale }) else null,
                )
            }
            item {
                LoadMoreButton(
                    visible = !isLoading && page < totalPages,
                    isLoading = isLoadingMore,
                    onClick = ::loadMore,
                )
            }
        }
    }

    editing?.let { sale ->
        EditSaleDialog(
            sale = sale,
            onDismiss = { editing = null },
            onSaved = { updated ->
                sales = sales.map { if (it.id == updated.id) updated else it }
                editing = null
                Toast.makeText(context, "Sale updated", Toast.LENGTH_SHORT).show()
            },
        )
    }

    deleting?.let { sale ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete sale?") },
            text = { Text("This removes the sale of ${sale.quantity_sold} × ${sale.product_name ?: "product"} (${formatMoney(sale.amount_sold)}).") },
            confirmButton = {
                Button(onClick = {
                    deleting = null
                    scope.launch {
                        try {
                            val response = RetrofitClient.depotApi.deleteSale(sale.id)
                            if (response.isSuccessful) {
                                sales = sales.filterNot { it.id == sale.id }
                                total = (total - 1).coerceAtLeast(0)
                                Toast.makeText(context, "Sale deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { deleting = null }) { Text("Cancel") } },
        )
    }
}

@Composable
private fun SaleHistoryRow(
    sale: SaleResponse,
    expanded: Boolean,
    onClick: () -> Unit,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${sale.product_name ?: "Product"} ${sale.quantity_value.orEmpty()}".trim(),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Qty ${sale.quantity_sold} · ${formatMoney(sale.amount_sold)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, contentDescription = "Edit sale") }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete sale", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = "Date: ${sale.sale_date ?: "—"} ${sale.sale_time.orEmpty()}".trim(), style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Unit price: ${formatMoney(sale.amount_sold / sale.quantity_sold.coerceAtLeast(1))}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                sale.depot_name?.let { Text(text = "Depot: $it", style = MaterialTheme.typography.bodyMedium) }
            }
        }
    }
}

@Composable
private fun EditSaleDialog(sale: SaleResponse, onDismiss: () -> Unit, onSaved: (SaleResponse) -> Unit) {
    var quantity by remember { mutableStateOf(sale.quantity_sold.toString()) }
    var amount by remember { mutableStateOf(sale.amount_sold.let { if (it % 1.0 == 0.0) it.toLong().toString() else it.toString() }) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val quantityValue = quantity.toIntOrNull()
    val amountValue = amount.toDoubleOrNull()
    val valid = quantityValue != null && quantityValue > 0 && amountValue != null && amountValue >= 0

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Edit Sale") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "${sale.product_name ?: "Product"} ${sale.quantity_value.orEmpty()}".trim(), style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { input -> quantity = input.filter { it.isDigit() } },
                    label = { Text("Quantity sold") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> amount = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Amount sold") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = valid && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.depotApi.updateSale(
                                sale.id,
                                SaleUpdateRequest(quantity_sold = quantityValue!!, amount_sold = amountValue!!),
                            )
                            val body = response.body()
                            if (response.isSuccessful && body != null) {
                                onSaved(body)
                            } else {
                                Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        isSubmitting = false
                    }
                },
            ) { Text(if (isSubmitting) "Saving…" else "Save") }
        },
        dismissButton = { TextButton(enabled = !isSubmitting, onClick = onDismiss) { Text("Cancel") } },
    )
}
