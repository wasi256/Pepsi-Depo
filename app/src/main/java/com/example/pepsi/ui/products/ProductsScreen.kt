package com.example.pepsi.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
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
import com.example.pepsi.data.network.model.FactoryStockResponse
import com.example.pepsi.data.network.model.ProductRead
import com.example.pepsi.data.network.model.ProductionResponse
import com.example.pepsi.data.network.model.QuantityRead
import com.example.pepsi.data.network.model.SupplyResponse
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityStepperField
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun formatIsoInstant(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun formatDisplayDate(iso: String?): String {
    if (iso == null) return "—"
    return iso.substringBefore("T")
}

private data class ProductsUiState(
    val products: List<ProductRead> = emptyList(),
    val quantities: List<QuantityRead> = emptyList(),
    val depots: List<DepotRead> = emptyList(),
    val stock: List<FactoryStockResponse> = emptyList(),
    val productionHistory: List<ProductionResponse> = emptyList(),
    val supplyHistory: List<SupplyResponse> = emptyList(),
)

@Composable
fun ProductsScreen(onMenuClick: () -> Unit) {
    ProductsContent(onMenuClick = onMenuClick)
}

@Composable
private fun ProductsContent(onMenuClick: () -> Unit) {
    var uiState by remember { mutableStateOf(ProductsUiState()) }
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
            val stockDeferred = async { FactoryRepository.fetchStock() }
            val productionDeferred = async { FactoryRepository.fetchProductionHistory() }
            val supplyDeferred = async { FactoryRepository.fetchSupplyHistory() }

            val results = awaitAll(
                productsDeferred, quantitiesDeferred, depotsDeferred,
                stockDeferred, productionDeferred, supplyDeferred,
            )
            val firstFailure = results.firstOrNull { it.isFailure }
            if (firstFailure != null) {
                errorMessage = firstFailure.exceptionOrNull()?.message
            }
            uiState = ProductsUiState(
                products = productsDeferred.await().getOrDefault(emptyList()),
                quantities = quantitiesDeferred.await().getOrDefault(emptyList()),
                depots = depotsDeferred.await().getOrDefault(emptyList()),
                stock = stockDeferred.await().getOrDefault(emptyList()),
                productionHistory = productionDeferred.await().getOrDefault(emptyList()),
                supplyHistory = supplyDeferred.await().getOrDefault(emptyList()),
            )
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            PepsiTopBar(title = "Products", onMenuClick = onMenuClick)
        },
    ) { padding ->
        if (isLoading && uiState.products.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) {
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
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                ) {
                    SectionHeader("Current Stock", modifier = Modifier.weight(1f))
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
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            item {
                if (uiState.stock.isEmpty()) {
                    EmptyHint("No stock recorded yet.")
                } else {
                    StockRow(uiState.stock)
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                RecordProductionSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    products = uiState.products,
                    quantities = uiState.quantities,
                    onRecorded = { reloadKey++ },
                )
            }

            item {
                SectionHeader("Production History", modifier = Modifier.padding(horizontal = 16.dp))
            }
            item {
                if (uiState.productionHistory.isEmpty()) {
                    EmptyHint("No production recorded yet.")
                } else {
                    ProductionHistoryRow(uiState.productionHistory)
                }
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                CreateSupplySection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    products = uiState.products,
                    quantities = uiState.quantities,
                    depots = uiState.depots,
                    onSupplied = { reloadKey++ },
                )
            }

            item {
                SectionHeader("Supply History", modifier = Modifier.padding(horizontal = 16.dp))
            }
            if (uiState.supplyHistory.isEmpty()) {
                item { EmptyHint("No supplies recorded yet.", modifier = Modifier.padding(horizontal = 16.dp)) }
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

@Composable
private fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = modifier,
    )
}

@Composable
private fun EmptyHint(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun StockRow(stock: List<FactoryStockResponse>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        stock.forEach { item ->
            Card(modifier = Modifier.width(200.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = item.productName, fontWeight = FontWeight.Bold)
                    item.quantityValue?.let {
                        Text(text = "Unit: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "Available: ${item.availableQuantity}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Updated: ${formatDisplayDate(item.updatedDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun ProductionHistoryRow(entries: List<ProductionResponse>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        entries.forEach { entry ->
            Card(modifier = Modifier.width(200.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = entry.productName, fontWeight = FontWeight.Bold)
                    entry.quantityValue?.let {
                        Text(text = "Unit: $it", style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "Produced: ${entry.quantityProduced}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Date: ${formatDisplayDate(entry.productionDate)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordProductionSection(
    modifier: Modifier = Modifier,
    products: List<ProductRead>,
    quantities: List<QuantityRead>,
    onRecorded: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    var productExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<ProductRead?>(null) }
    var quantityExpanded by remember { mutableStateOf(false) }
    var selectedQuantity by remember { mutableStateOf<QuantityRead?>(null) }
    var quantityProduced by remember { mutableStateOf("") }
    var productionDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<String?>(null) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionHeader("Record Production")

        ExposedDropdownMenuBox(
            expanded = productExpanded,
            onExpandedChange = { productExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedProduct?.name ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Product") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = productExpanded,
                onDismissRequest = { productExpanded = false },
            ) {
                products.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(product.name) },
                        onClick = {
                            selectedProduct = product
                            productExpanded = false
                        },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = quantityExpanded,
            onExpandedChange = { quantityExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedQuantity?.quantity ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Quantity (unit size)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quantityExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = quantityExpanded,
                onDismissRequest = { quantityExpanded = false },
            ) {
                quantities.forEach { quantity ->
                    DropdownMenuItem(
                        text = { Text(quantity.quantity) },
                        onClick = {
                            selectedQuantity = quantity
                            quantityExpanded = false
                        },
                    )
                }
            }
        }

        QuantityStepperField(
            quantity = quantityProduced,
            onQuantityChange = { quantityProduced = it },
            label = "Quantity Produced",
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = productionDateMillis?.let { formatIsoInstant(it).substringBefore("T") } ?: "Today (default)",
                onValueChange = {},
                readOnly = true,
                label = { Text("Production Date (optional)") },
                trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Pick production date") },
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = { showDatePicker = true }),
            )
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }

        Button(
            onClick = {
                val product = selectedProduct ?: return@Button
                val quantity = selectedQuantity ?: return@Button
                val qty = quantityProduced.toIntOrNull() ?: return@Button
                if (qty <= 0) return@Button
                isSubmitting = true
                errorMessage = null
                scope.launch {
                    FactoryRepository.recordProduction(
                        productId = product.id,
                        quantityId = quantity.id,
                        quantityProduced = qty,
                        productionDate = productionDateMillis?.let { formatIsoInstant(it) },
                    ).onSuccess {
                        confirmation = "Recorded $qty × ${quantity.quantity} of ${product.name}."
                        selectedProduct = null
                        selectedQuantity = null
                        quantityProduced = ""
                        productionDateMillis = null
                        onRecorded()
                    }.onFailure {
                        errorMessage = it.message
                    }
                    isSubmitting = false
                }
            },
            enabled = !isSubmitting && selectedProduct != null && selectedQuantity != null &&
                quantityProduced.toIntOrNull()?.let { it > 0 } == true,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text("Record Production")
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = productionDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    productionDateMillis = datePickerState.selectedDateMillis
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            },
        ) {
            DatePicker(state = datePickerState)
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
        SectionHeader("Create Supply")
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
