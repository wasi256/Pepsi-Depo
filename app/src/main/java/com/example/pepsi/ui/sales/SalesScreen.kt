package com.example.pepsi.ui.sales

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.network.model.DepotRead
import com.example.pepsi.data.network.model.ProductRead
import com.example.pepsi.data.network.model.QuantityRead
import com.example.pepsi.data.network.model.SupplyResponse
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityStepperField
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

private fun formatDisplayDate(iso: String?): String {
    if (iso == null) return "—"
    return iso.substringBefore("T")
}

private data class SalesUiState(
    val products: List<ProductRead> = emptyList(),
    val quantities: List<QuantityRead> = emptyList(),
    val depots: List<DepotRead> = emptyList(),
    val supplyHistory: List<SupplyResponse> = emptyList(),
)

@Composable
fun SalesScreen(onMenuClick: () -> Unit) {
    var uiState by remember { mutableStateOf(SalesUiState()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var reloadKey by remember { mutableStateOf(0) }

    LaunchedEffect(reloadKey) {
        isLoading = true
        errorMessage = null
        coroutineScope {
            val productsDeferred = async { FactoryRepository.fetchProducts() }
            val quantitiesDeferred = async { FactoryRepository.fetchQuantities() }
            val depotsDeferred = async { FactoryRepository.fetchDepots() }
            val supplyDeferred = async { FactoryRepository.fetchSupplyHistory() }

            val results = awaitAll(productsDeferred, quantitiesDeferred, depotsDeferred, supplyDeferred)
            val firstFailure = results.firstOrNull { it.isFailure }
            if (firstFailure != null) {
                errorMessage = firstFailure.exceptionOrNull()?.message
            }
            uiState = SalesUiState(
                products = productsDeferred.await().getOrDefault(emptyList()),
                quantities = quantitiesDeferred.await().getOrDefault(emptyList()),
                depots = depotsDeferred.await().getOrDefault(emptyList()),
                supplyHistory = supplyDeferred.await().getOrDefault(emptyList()),
            )
        }
        isLoading = false
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Sales", onMenuClick = onMenuClick) },
    ) { padding ->
        if (isLoading && uiState.products.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            errorMessage?.let {
                item {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            item {
                CreateSupplySection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    products = uiState.products,
                    quantities = uiState.quantities,
                    depots = uiState.depots,
                    onSupplied = { reloadKey++ },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = "Supply History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { reloadKey++ }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            }
            if (uiState.supplyHistory.isEmpty()) {
                item {
                    Text(
                        text = "No supplies recorded yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            items(uiState.supplyHistory) { supply ->
                SupplyRow(
                    supply = supply,
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onDeleted = { reloadKey++ },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateSupplySection(
    modifier: Modifier = Modifier,
    products: List<ProductRead>,
    quantities: List<QuantityRead>,
    depots: List<DepotRead>,
    onSupplied: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var depotExpanded by remember { mutableStateOf(false) }
    var selectedDepot by remember { mutableStateOf<DepotRead?>(null) }
    var productExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductRead?>(null) }
    var quantityExpanded by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableStateOf<QuantityRead?>(null) }
    var amount by remember { mutableStateOf("") }
    var supplierId by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Create Supply",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "No sign-in yet, so enter your personnel ID manually for now.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        ExposedDropdownMenuBox(expanded = depotExpanded, onExpandedChange = { depotExpanded = it }) {
            OutlinedTextField(
                value = selectedDepot?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Depo Name") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = depotExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = depotExpanded, onDismissRequest = { depotExpanded = false }) {
                depots.forEach { depot ->
                    DropdownMenuItem(
                        text = { Text("${depot.name} — ${depot.location}") },
                        onClick = { selectedDepot = depot; depotExpanded = false },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(expanded = productExpanded, onExpandedChange = { productExpanded = it }) {
            OutlinedTextField(
                value = selectedProduct?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Product Name") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = productExpanded, onDismissRequest = { productExpanded = false }) {
                products.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(product.name) },
                        onClick = { selectedProduct = product; productExpanded = false },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(expanded = quantityExpanded, onExpandedChange = { quantityExpanded = it }) {
            OutlinedTextField(
                value = selectedQuantity?.quantity ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Quantity (unit size)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quantityExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(expanded = quantityExpanded, onDismissRequest = { quantityExpanded = false }) {
                quantities.forEach { quantity ->
                    DropdownMenuItem(
                        text = { Text(quantity.quantity) },
                        onClick = { selectedQuantity = quantity; quantityExpanded = false },
                    )
                }
            }
        }

        QuantityStepperField(
            quantity = amount,
            onQuantityChange = { amount = it },
            label = "Amount",
        )

        OutlinedTextField(
            value = supplierId,
            onValueChange = { supplierId = it.filter(Char::isDigit) },
            label = { Text("Supplier (personnel) ID") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                val depot = selectedDepot ?: return@Button
                val product = selectedProduct ?: return@Button
                val quantity = selectedQuantity ?: return@Button
                val amountValue = amount.toIntOrNull() ?: return@Button
                val supplierIdValue = supplierId.toIntOrNull() ?: return@Button
                if (amountValue <= 0) return@Button
                isSubmitting = true
                errorMessage = null
                scope.launch {
                    FactoryRepository.createSupply(
                        productId = product.id,
                        quantityId = quantity.id,
                        amount = amountValue,
                        depotId = depot.id,
                        supplierId = supplierIdValue,
                    ).onSuccess {
                        confirmation = "Supply of $amountValue × ${quantity.quantity} ${product.name} sent to ${depot.name}."
                        selectedProduct = null
                        selectedQuantity = null
                        amount = ""
                        onSupplied()
                    }.onFailure {
                        errorMessage = it.message
                    }
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting && selectedDepot != null && selectedProduct != null &&
                selectedQuantity != null && amount.toIntOrNull()?.let { it > 0 } == true &&
                supplierId.toIntOrNull() != null,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text("Create Supply")
            }
        }

        confirmation?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(text = it, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

@Composable
private fun SupplyRow(
    supply: SupplyResponse,
    modifier: Modifier = Modifier,
    onDeleted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var isDeleting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(modifier = modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = supply.productName, fontWeight = FontWeight.Bold)
            Text(
                text = "Amount: ${supply.amount} × ${supply.quantityValue}",
                style = MaterialTheme.typography.bodyMedium,
            )
            supply.depotName?.let {
                Text(text = "Depo: $it", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Status: ${supply.status}", style = MaterialTheme.typography.bodyMedium)
            supply.rejectionReason?.let {
                Text(
                    text = "Rejection reason: $it",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Text(
                text = "Date: ${formatDisplayDate(supply.createdDate)}",
                style = MaterialTheme.typography.bodyMedium,
            )
            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }
            if (supply.status == "pending") {
                OutlinedButton(
                    onClick = {
                        isDeleting = true
                        errorMessage = null
                        scope.launch {
                            FactoryRepository.deleteSupply(supply.id)
                                .onSuccess { onDeleted() }
                                .onFailure { errorMessage = it.message }
                            isDeleting = false
                        }
                    },
                    enabled = !isDeleting,
                    modifier = Modifier.padding(top = 8.dp),
                ) {
                    Text(if (isDeleting) "Deleting…" else "Delete Supply")
                }
            }
        }
    }
}
