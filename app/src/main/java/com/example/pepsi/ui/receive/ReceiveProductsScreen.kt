package com.example.pepsi.ui.receive

import android.widget.Toast
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.RestockConfirmRequest
import com.example.pepsi.network.model.RestockRejectRequest
import com.example.pepsi.network.model.RestockResponse
import com.example.pepsi.network.model.RestockUpdateRequest
import com.example.pepsi.network.model.SupplyHistoryResponse
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.LoadMoreButton
import com.example.pepsi.ui.components.OnResume
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityStepperField
import com.example.pepsi.ui.components.loadPendingSupplies
import com.example.pepsi.util.formatApiDateTime
import kotlinx.coroutines.launch

private const val HISTORY_PAGE_SIZE = 20

@Composable
fun ReceiveProductsScreen(onMenuClick: () -> Unit) {
    val canConfirm = AppAccess.canCreate(PermissionModule.DEPOT_RESTOCK)
    var tab by remember { mutableIntStateOf(0) }
    // Bumped after a confirm/reject so the history tab reloads next time it is shown.
    var historyVersion by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = { PepsiTopBar(title = "Receive Products", onMenuClick = onMenuClick) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("To confirm") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("History") })
            }
            if (tab == 0) {
                PendingTab(canConfirm = canConfirm, onResponded = { historyVersion++ })
            } else {
                HistoryTab(version = historyVersion)
            }
        }
    }
}

@Composable
private fun PendingTab(canConfirm: Boolean, onResponded: () -> Unit) {
    var pending by remember { mutableStateOf<List<SupplyHistoryResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var confirming by remember { mutableStateOf<SupplyHistoryResponse?>(null) }
    var rejecting by remember { mutableStateOf<SupplyHistoryResponse?>(null) }

    suspend fun reload() {
        isLoading = true
        error = null
        loadPendingSupplies()
            .onSuccess { pending = it }
            .onFailure { error = it.message }
        isLoading = false
    }

    OnResume { reload() }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = "Pending Deliveries",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        item {
            ListStatus(
                isLoading = isLoading,
                error = error,
                isEmpty = !isLoading && error == null && pending.isEmpty(),
                emptyText = "No pending deliveries from the factory.",
            )
        }
        items(pending, key = { it.id }) { supply ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${supply.product_name ?: "Product"} ${supply.quantity_value.orEmpty()}".trim(),
                        fontWeight = FontWeight.Bold,
                    )
                    Text(text = "Sent: ${supply.amount} units", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Dispatched ${formatApiDateTime(supply.created_date)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    if (canConfirm) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { confirming = supply }) { Text("Confirm") }
                            OutlinedButton(onClick = { rejecting = supply }) { Text("Reject") }
                        }
                    }
                }
            }
        }
    }

    confirming?.let { supply ->
        ResponseDialog(
            title = "Confirm delivery",
            supply = supply,
            askReason = false,
            confirmLabel = "Confirm",
            onDismiss = { confirming = null },
            onSubmit = { received, _ ->
                RetrofitClient.depotApi.confirmRestock(
                    supply.id,
                    RestockConfirmRequest(received, AuthSession.user?.personnel_id ?: 0),
                )
            },
            onDone = {
                confirming = null
                pending = pending.filterNot { it.id == supply.id }
                onResponded()
                Toast.makeText(context, "Delivery confirmed and added to stock", Toast.LENGTH_SHORT).show()
                scope.launch { reload() }
            },
        )
    }

    rejecting?.let { supply ->
        ResponseDialog(
            title = "Reject delivery",
            supply = supply,
            askReason = true,
            confirmLabel = "Reject",
            onDismiss = { rejecting = null },
            onSubmit = { received, reason ->
                RetrofitClient.depotApi.rejectRestock(
                    supply.id,
                    RestockRejectRequest(reason, received, AuthSession.user?.personnel_id ?: 0),
                )
            },
            onDone = {
                rejecting = null
                pending = pending.filterNot { it.id == supply.id }
                onResponded()
                Toast.makeText(context, "Delivery rejected", Toast.LENGTH_SHORT).show()
                scope.launch { reload() }
            },
        )
    }
}

@Composable
private fun ResponseDialog(
    title: String,
    supply: SupplyHistoryResponse,
    askReason: Boolean,
    confirmLabel: String,
    onDismiss: () -> Unit,
    onSubmit: suspend (received: Int, reason: String) -> retrofit2.Response<RestockResponse>,
    onDone: () -> Unit,
) {
    var received by remember { mutableStateOf(if (askReason) "0" else supply.amount.toString()) }
    var reason by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val receivedValue = received.toIntOrNull()
    val valid = receivedValue != null && receivedValue >= 0 && (!askReason || reason.isNotBlank())

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${supply.product_name ?: "Product"} ${supply.quantity_value.orEmpty()} — ${supply.amount} sent",
                    style = MaterialTheme.typography.bodyMedium,
                )
                QuantityStepperField(quantity = received, onQuantityChange = { received = it }, label = "Quantity received")
                if (askReason) {
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = valid && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = onSubmit(receivedValue!!, reason.trim())
                            if (response.isSuccessful) {
                                onDone()
                            } else {
                                Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                        isSubmitting = false
                    }
                },
            ) { Text(if (isSubmitting) "Sending…" else confirmLabel) }
        },
        dismissButton = { TextButton(enabled = !isSubmitting, onClick = onDismiss) { Text("Cancel") } },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HistoryTab(version: Int) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val canEdit = AppAccess.canUpdate(PermissionModule.DEPOT_RESTOCK)
    val canDelete = AppAccess.canDelete(PermissionModule.DEPOT_RESTOCK)

    var status by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<RestockResponse>>(emptyList()) }
    var page by remember { mutableIntStateOf(1) }
    var totalPages by remember { mutableIntStateOf(1) }
    var isLoading by remember { mutableStateOf(true) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    var editing by remember { mutableStateOf<RestockResponse?>(null) }
    var deleting by remember { mutableStateOf<RestockResponse?>(null) }

    suspend fun fetch(pageNumber: Int) = RetrofitClient.depotApi.listRestock(
        depotId = AuthSession.depotId,
        status = status,
        page = pageNumber,
        pageSize = HISTORY_PAGE_SIZE,
    )

    LaunchedEffect(status, version, AuthSession.depotId) {
        isLoading = true
        error = null
        try {
            val response = fetch(1)
            val body = response.body()
            if (response.isSuccessful && body != null) {
                entries = body.items
                page = body.page
                totalPages = body.total_pages
            } else {
                entries = emptyList()
                error = response.readErrorMessage()
            }
        } catch (e: Exception) {
            entries = emptyList()
            error = "Network error: ${e.message}"
        }
        isLoading = false
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "All", "confirmed" to "Confirmed", "rejected" to "Rejected").forEach { (value, label) ->
                    FilterChip(selected = status == value, onClick = { status = value }, label = { Text(label) })
                }
            }
        }
        item {
            ListStatus(
                isLoading = isLoading,
                error = error,
                isEmpty = !isLoading && error == null && entries.isEmpty(),
                emptyText = "No restock entries yet.",
            )
        }
        items(entries, key = { it.id }) { entry ->
            val rejected = entry.status.equals("rejected", ignoreCase = true)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (rejected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${entry.product_name ?: "Product"} ${entry.quantity_value.orEmpty()}".trim(),
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = "${entry.quantity_delivered} units · ${entry.status.orEmpty().replaceFirstChar { it.uppercase() }}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = formatApiDateTime(entry.restock_date),
                            style = MaterialTheme.typography.bodySmall,
                        )
                        if (rejected && !entry.rejection_reason.isNullOrBlank()) {
                            Text(text = "Reason: ${entry.rejection_reason}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    if (canEdit) {
                        IconButton(onClick = { editing = entry }) { Icon(Icons.Default.Edit, contentDescription = "Edit entry") }
                    }
                    if (canDelete) {
                        IconButton(onClick = { deleting = entry }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete entry", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
        item {
            LoadMoreButton(
                visible = !isLoading && page < totalPages,
                isLoading = isLoadingMore,
                onClick = {
                    scope.launch {
                        isLoadingMore = true
                        try {
                            val response = fetch(page + 1)
                            val body = response.body()
                            if (response.isSuccessful && body != null) {
                                entries = entries + body.items
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
                },
            )
        }
    }

    editing?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onDismiss = { editing = null },
            onSaved = { updated ->
                entries = entries.map { if (it.id == updated.id) updated else it }
                editing = null
                Toast.makeText(context, "Entry updated", Toast.LENGTH_SHORT).show()
            },
        )
    }

    deleting?.let { entry ->
        AlertDialog(
            onDismissRequest = { deleting = null },
            title = { Text("Delete entry?") },
            text = { Text("This removes the ${entry.status.orEmpty()} restock of ${entry.quantity_delivered} × ${entry.product_name ?: "product"}.") },
            confirmButton = {
                Button(onClick = {
                    deleting = null
                    scope.launch {
                        try {
                            val response = RetrofitClient.depotApi.deleteRestockEntry(entry.id)
                            if (response.isSuccessful) {
                                entries = entries.filterNot { it.id == entry.id }
                                Toast.makeText(context, "Entry deleted", Toast.LENGTH_SHORT).show()
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
private fun EditEntryDialog(entry: RestockResponse, onDismiss: () -> Unit, onSaved: (RestockResponse) -> Unit) {
    var delivered by remember { mutableStateOf(entry.quantity_delivered.toString()) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val value = delivered.toIntOrNull()

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Edit Entry") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${entry.product_name ?: "Product"} ${entry.quantity_value.orEmpty()}".trim(),
                    style = MaterialTheme.typography.bodyMedium,
                )
                QuantityStepperField(quantity = delivered, onQuantityChange = { delivered = it }, label = "Quantity delivered")
            }
        },
        confirmButton = {
            Button(
                enabled = value != null && value >= 0 && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.depotApi.updateRestockEntry(entry.id, RestockUpdateRequest(value!!))
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
