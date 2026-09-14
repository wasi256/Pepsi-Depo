package com.example.pepsi.ui.receive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.network.DepotApiService
import com.example.pepsi.data.network.FactoryApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.RestockConfirmRequest
import com.example.pepsi.data.network.model.RestockRejectRequest
import com.example.pepsi.data.network.model.RestockResponse
import com.example.pepsi.data.network.model.SupplyResponse
import com.example.pepsi.data.state.DepotSession
import com.example.pepsi.ui.components.DepotDropdown
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityStepperField
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

private const val MAX_PENDING_PAGES = 10
private const val PENDING_PAGE_SIZE = 10

private suspend fun fetchPendingSupplies(apiService: FactoryApiService): List<SupplyResponse> {
    val pending = mutableListOf<SupplyResponse>()
    var skip = 0
    repeat(MAX_PENDING_PAGES) {
        val page = apiService.getSupplyHistory(skip = skip, limit = PENDING_PAGE_SIZE)
        pending += page.filter { it.status == "pending" }
        if (page.size < PENDING_PAGE_SIZE) return pending
        skip += PENDING_PAGE_SIZE
    }
    return pending
}

@Composable
fun ReceiveProductsScreen(onMenuClick: () -> Unit) {
    val factoryApi = remember { RetrofitClient.createService(FactoryApiService::class.java) }
    val depotApi = remember { RetrofitClient.createService(DepotApiService::class.java) }
    val scope = rememberCoroutineScope()

    val depots by DepotSession.depots
    val selectedDepot by DepotSession.selectedDepot
    val depotsLoading by DepotSession.isLoading
    val depotsError by DepotSession.error

    val pending = remember { mutableStateListOf<SupplyResponse>() }
    var pendingLoading by remember { mutableStateOf(true) }
    var pendingError by remember { mutableStateOf<String?>(null) }

    val recentlyActioned = remember { mutableStateListOf<RestockResponse>() }

    LaunchedEffect(Unit) {
        DepotSession.ensureLoaded(depotApi)
        try {
            pending.clear()
            pending.addAll(fetchPendingSupplies(factoryApi))
        } catch (e: Exception) {
            pendingError = e.localizedMessage ?: "Failed to load pending deliveries."
        } finally {
            pendingLoading = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Receive Products", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                DepotDropdown(
                    depots = depots,
                    selectedDepot = selectedDepot,
                    onDepotSelected = DepotSession::select,
                    isLoading = depotsLoading,
                )
            }
            depotsError?.let { error ->
                item {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }

            item {
                Text(
                    text = "Pending Deliveries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            item {
                ListStatus(
                    isLoading = pendingLoading,
                    error = pendingError,
                    isEmpty = !pendingLoading && pendingError == null && pending.isEmpty(),
                    emptyText = "No pending deliveries from the factory.",
                )
            }

            items(pending, key = { it.id }) { supply ->
                PendingSupplyCard(
                    supply = supply,
                    depotApi = depotApi,
                    scope = scope,
                    onActioned = { response ->
                        pending.remove(supply)
                        recentlyActioned.add(0, response)
                    },
                )
            }

            if (recentlyActioned.isNotEmpty()) {
                item { HorizontalDivider() }
                item {
                    Text(
                        text = "Recently Responded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(recentlyActioned, key = { it.id }) { restock ->
                    RespondedRestockCard(restock)
                }
            }
        }
    }
}

private enum class ReceiveAction { None, Confirming, Rejecting }

@Composable
private fun PendingSupplyCard(
    supply: SupplyResponse,
    depotApi: DepotApiService,
    scope: CoroutineScope,
    onActioned: (RestockResponse) -> Unit,
) {
    var action by remember(supply.id) { mutableStateOf(ReceiveAction.None) }
    var isSubmitting by remember(supply.id) { mutableStateOf(false) }
    var submitError by remember(supply.id) { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = supply.productName, fontWeight = FontWeight.Bold)
            Text(
                text = "Amount sent: ${supply.amount} (${supply.quantityValue})",
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(text = "Recorded: ${supply.createdDate}", style = MaterialTheme.typography.bodyMedium)

            if (action == ReceiveAction.None) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { action = ReceiveAction.Confirming }, modifier = Modifier.weight(1f)) {
                        Text("Confirm")
                    }
                    OutlinedButton(onClick = { action = ReceiveAction.Rejecting }, modifier = Modifier.weight(1f)) {
                        Text("Reject")
                    }
                }
            }

            if (action == ReceiveAction.Confirming) {
                ConfirmSection(
                    defaultQuantity = supply.amount,
                    isSubmitting = isSubmitting,
                    error = submitError,
                    onCancel = { action = ReceiveAction.None },
                    onSend = { quantityReceived ->
                        val depotId = DepotSession.selectedDepot.value?.id ?: return@ConfirmSection
                        isSubmitting = true
                        submitError = null
                        scope.launch {
                            try {
                                val response = depotApi.confirmRestock(
                                    supply.id,
                                    RestockConfirmRequest(depotId = depotId, quantityReceived = quantityReceived),
                                )
                                onActioned(response)
                            } catch (e: Exception) {
                                submitError = e.localizedMessage ?: "Failed to confirm delivery."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                )
            }

            if (action == ReceiveAction.Rejecting) {
                RejectSection(
                    defaultQuantity = supply.amount,
                    isSubmitting = isSubmitting,
                    error = submitError,
                    onCancel = { action = ReceiveAction.None },
                    onSend = { quantityReceived, reason ->
                        val depotId = DepotSession.selectedDepot.value?.id ?: return@RejectSection
                        isSubmitting = true
                        submitError = null
                        scope.launch {
                            try {
                                val response = depotApi.rejectRestock(
                                    supply.id,
                                    RestockRejectRequest(
                                        depotId = depotId,
                                        reason = reason,
                                        quantityReceived = quantityReceived,
                                    ),
                                )
                                onActioned(response)
                            } catch (e: Exception) {
                                submitError = e.localizedMessage ?: "Failed to reject delivery."
                            } finally {
                                isSubmitting = false
                            }
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfirmSection(
    defaultQuantity: Int,
    isSubmitting: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onSend: (quantityReceived: Int) -> Unit,
) {
    var quantity by remember { mutableStateOf(defaultQuantity.toString()) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(text = "Confirm Delivery", fontWeight = FontWeight.Bold)
        QuantityStepperField(
            quantity = quantity,
            onQuantityChange = { quantity = it },
            label = "Quantity Received",
        )
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { quantity.toIntOrNull()?.let { if (it > 0) onSend(it) } },
                enabled = !isSubmitting && quantity.toIntOrNull()?.let { it > 0 } == true,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (isSubmitting) "Sending…" else "Send")
            }
            OutlinedButton(onClick = onCancel, enabled = !isSubmitting, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun RejectSection(
    defaultQuantity: Int,
    isSubmitting: Boolean,
    error: String?,
    onCancel: () -> Unit,
    onSend: (quantityReceived: Int, reason: String) -> Unit,
) {
    var quantity by remember { mutableStateOf(defaultQuantity.toString()) }
    var reason by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(text = "Reject Delivery", fontWeight = FontWeight.Bold)
        QuantityStepperField(
            quantity = quantity,
            onQuantityChange = { quantity = it },
            label = "Quantity Actually Received",
        )
        OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Reason for rejection") },
            modifier = Modifier.fillMaxWidth(),
        )
        error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = {
                    val qty = quantity.toIntOrNull() ?: 0
                    if (reason.trim().length >= 3) onSend(qty, reason.trim())
                },
                enabled = !isSubmitting && reason.trim().length >= 3,
                modifier = Modifier.weight(1f),
            ) {
                Text(if (isSubmitting) "Sending…" else "Send")
            }
            OutlinedButton(onClick = onCancel, enabled = !isSubmitting, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun RespondedRestockCard(restock: RestockResponse) {
    val statusLabel = restock.status.replaceFirstChar(Char::uppercase)
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${restock.productName} — $statusLabel", fontWeight = FontWeight.Bold)
            Text(
                text = "Quantity received: ${restock.quantityDelivered} (${restock.quantityValue})",
                style = MaterialTheme.typography.bodyMedium,
            )
            restock.rejectionReason?.let {
                Text(text = "Reason: $it", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
