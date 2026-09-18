package com.example.pepsi.systemadmin.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.PriceResponse
import com.example.pepsi.network.model.PriceUpdateRequest
import com.example.pepsi.network.model.ProductCreateRequest
import com.example.pepsi.network.model.ProductResponse
import com.example.pepsi.network.model.QuantityCreateRequest
import com.example.pepsi.network.model.QuantityResponse
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.PepsiTopBar
import kotlinx.coroutines.launch

private const val TAB_PRODUCTS = 0
private const val TAB_QUANTITIES = 1
private const val TAB_PRICES = 2

@Composable
fun ProductsScreen(
    onMenuClick: () -> Unit,
    onRegisterProduct: () -> Unit,
    onRegisterQuantity: () -> Unit,
    onRegisterPrice: () -> Unit,
) {
    var selectedTab by rememberSaveable { mutableStateOf(TAB_PRODUCTS) }

    var products by remember { mutableStateOf<List<ProductResponse>>(emptyList()) }
    var productsLoading by remember { mutableStateOf(true) }
    var productsError by remember { mutableStateOf<String?>(null) }
    var productQuery by rememberSaveable { mutableStateOf("") }
    var editingProduct by remember { mutableStateOf<ProductResponse?>(null) }
    var deletingProduct by remember { mutableStateOf<ProductResponse?>(null) }

    var quantities by remember { mutableStateOf<List<QuantityResponse>>(emptyList()) }
    var quantitiesLoading by remember { mutableStateOf(true) }
    var quantitiesError by remember { mutableStateOf<String?>(null) }
    var quantityQuery by rememberSaveable { mutableStateOf("") }
    var editingQuantity by remember { mutableStateOf<QuantityResponse?>(null) }
    var deletingQuantity by remember { mutableStateOf<QuantityResponse?>(null) }

    var prices by remember { mutableStateOf<List<PriceResponse>>(emptyList()) }
    var pricesLoading by remember { mutableStateOf(true) }
    var pricesError by remember { mutableStateOf<String?>(null) }
    var priceQuery by rememberSaveable { mutableStateOf("") }
    var editingPrice by remember { mutableStateOf<PriceResponse?>(null) }
    var deletingPrice by remember { mutableStateOf<PriceResponse?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun loadProducts() {
        productsLoading = true
        productsError = null
        try {
            val response = RetrofitClient.adminApi.listProducts()
            if (response.isSuccessful) {
                products = response.body()?.items ?: emptyList()
            } else {
                productsError = response.readErrorMessage()
            }
        } catch (e: Exception) {
            productsError = "Network error: ${e.message}"
        } finally {
            productsLoading = false
        }
    }

    suspend fun loadQuantities() {
        quantitiesLoading = true
        quantitiesError = null
        try {
            val response = RetrofitClient.adminApi.listQuantities()
            if (response.isSuccessful) {
                quantities = response.body()?.items ?: emptyList()
            } else {
                quantitiesError = response.readErrorMessage()
            }
        } catch (e: Exception) {
            quantitiesError = "Network error: ${e.message}"
        } finally {
            quantitiesLoading = false
        }
    }

    suspend fun loadPrices() {
        pricesLoading = true
        pricesError = null
        try {
            val response = RetrofitClient.adminApi.listPrices()
            if (response.isSuccessful) {
                prices = response.body()?.items ?: emptyList()
            } else {
                pricesError = response.readErrorMessage()
            }
        } catch (e: Exception) {
            pricesError = "Network error: ${e.message}"
        } finally {
            pricesLoading = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    loadProducts()
                    loadQuantities()
                    loadPrices()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val quantityById = quantities.associateBy { it.id }

    Scaffold(
        topBar = { PepsiTopBar(title = "Products", onMenuClick = onMenuClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = {
                    Text(
                        when (selectedTab) {
                            TAB_PRODUCTS -> "Add Product"
                            TAB_QUANTITIES -> "Add Quantity"
                            else -> "Add Price"
                        },
                    )
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                onClick = {
                    when (selectedTab) {
                        TAB_PRODUCTS -> onRegisterProduct()
                        TAB_QUANTITIES -> onRegisterQuantity()
                        else -> onRegisterPrice()
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == TAB_PRODUCTS, onClick = { selectedTab = TAB_PRODUCTS }, text = { Text("Products") })
                Tab(selected = selectedTab == TAB_QUANTITIES, onClick = { selectedTab = TAB_QUANTITIES }, text = { Text("Quantities") })
                Tab(selected = selectedTab == TAB_PRICES, onClick = { selectedTab = TAB_PRICES }, text = { Text("Prices") })
            }

            when (selectedTab) {
                TAB_PRODUCTS -> {
                    val filtered = if (productQuery.isBlank()) {
                        products
                    } else {
                        products.filter { listOf(it.id.toString(), it.name).any { field -> field.contains(productQuery, true) } }
                    }
                    CatalogList(
                        isLoading = productsLoading,
                        errorMessage = productsError,
                        isEmptyData = products.isEmpty(),
                        onRetry = { scope.launch { loadProducts() } },
                        query = productQuery,
                        onQueryChange = { productQuery = it },
                        searchPlaceholder = "Search by name or ID",
                        countLabel = "${filtered.size} of ${products.size} products",
                        items = filtered,
                        key = { it.id },
                    ) { product ->
                        SimpleCatalogRow(
                            title = product.name,
                            subtitle = "ID: ${product.id}",
                            onEdit = { editingProduct = product },
                            onDelete = { deletingProduct = product },
                        )
                    }
                }

                TAB_QUANTITIES -> {
                    val filtered = if (quantityQuery.isBlank()) {
                        quantities
                    } else {
                        quantities.filter { listOf(it.id.toString(), it.quantity).any { field -> field.contains(quantityQuery, true) } }
                    }
                    CatalogList(
                        isLoading = quantitiesLoading,
                        errorMessage = quantitiesError,
                        isEmptyData = quantities.isEmpty(),
                        onRetry = { scope.launch { loadQuantities() } },
                        query = quantityQuery,
                        onQueryChange = { quantityQuery = it },
                        searchPlaceholder = "Search by quantity or ID",
                        countLabel = "${filtered.size} of ${quantities.size} quantities",
                        items = filtered,
                        key = { it.id },
                    ) { quantity ->
                        SimpleCatalogRow(
                            title = quantity.quantity,
                            subtitle = "ID: ${quantity.id}",
                            onEdit = { editingQuantity = quantity },
                            onDelete = { deletingQuantity = quantity },
                        )
                    }
                }

                else -> {
                    val filtered = if (priceQuery.isBlank()) {
                        prices
                    } else {
                        prices.filter { price ->
                            val quantityLabel = quantityById[price.quantity_id]?.quantity.orEmpty()
                            listOf(price.id.toString(), price.quantity_id.toString(), price.amount.toString(), quantityLabel)
                                .any { field -> field.contains(priceQuery, true) }
                        }
                    }
                    CatalogList(
                        isLoading = pricesLoading,
                        errorMessage = pricesError,
                        isEmptyData = prices.isEmpty(),
                        onRetry = { scope.launch { loadPrices() } },
                        query = priceQuery,
                        onQueryChange = { priceQuery = it },
                        searchPlaceholder = "Search by quantity or amount",
                        countLabel = "${filtered.size} of ${prices.size} prices",
                        items = filtered,
                        key = { it.id },
                    ) { price ->
                        PriceRow(
                            price = price,
                            quantityLabel = quantityById[price.quantity_id]?.quantity ?: "Quantity #${price.quantity_id}",
                            onEdit = { editingPrice = price },
                            onDelete = { deletingPrice = price },
                        )
                    }
                }
            }
        }
    }

    editingProduct?.let { product ->
        EditNameDialog(
            title = "Edit Product",
            label = "Product name",
            initialValue = product.name,
            onDismiss = { editingProduct = null },
            onSave = { newName ->
                RetrofitClient.adminApi.updateProduct(product.id, ProductCreateRequest(newName))
            },
            onSaved = { updated ->
                products = products.map { if (it.id == updated.id) updated else it }
                editingProduct = null
                Toast.makeText(context, "Product updated successfully", Toast.LENGTH_LONG).show()
            },
        )
    }

    deletingProduct?.let { product ->
        ConfirmDeleteDialog(
            title = "Delete Product",
            message = "Are you sure you want to delete \"${product.name}\"? This cannot be undone.",
            onDismiss = { deletingProduct = null },
            onConfirm = {
                val response = RetrofitClient.adminApi.deleteProduct(product.id)
                if (response.isSuccessful) {
                    products = products.filterNot { it.id == product.id }
                    deletingProduct = null
                    Toast.makeText(context, "Product deleted successfully", Toast.LENGTH_LONG).show()
                    true
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                    false
                }
            },
        )
    }

    editingQuantity?.let { quantity ->
        EditNameDialog(
            title = "Edit Quantity",
            label = "Quantity",
            initialValue = quantity.quantity,
            onDismiss = { editingQuantity = null },
            onSave = { newValue ->
                RetrofitClient.adminApi.updateQuantity(quantity.id, QuantityCreateRequest(newValue))
            },
            onSaved = { updated ->
                quantities = quantities.map { if (it.id == updated.id) updated else it }
                editingQuantity = null
                Toast.makeText(context, "Quantity updated successfully", Toast.LENGTH_LONG).show()
            },
        )
    }

    deletingQuantity?.let { quantity ->
        ConfirmDeleteDialog(
            title = "Delete Quantity",
            message = "Are you sure you want to delete \"${quantity.quantity}\"? This cannot be undone.",
            onDismiss = { deletingQuantity = null },
            onConfirm = {
                val response = RetrofitClient.adminApi.deleteQuantity(quantity.id)
                if (response.isSuccessful) {
                    quantities = quantities.filterNot { it.id == quantity.id }
                    deletingQuantity = null
                    Toast.makeText(context, "Quantity deleted successfully", Toast.LENGTH_LONG).show()
                    true
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                    false
                }
            },
        )
    }

    editingPrice?.let { price ->
        EditPriceDialog(
            price = price,
            quantityLabel = quantityById[price.quantity_id]?.quantity ?: "Quantity #${price.quantity_id}",
            onDismiss = { editingPrice = null },
            onSaved = { updated ->
                prices = prices.map { if (it.id == updated.id) updated else it }
                editingPrice = null
                Toast.makeText(context, "Price updated successfully", Toast.LENGTH_LONG).show()
            },
        )
    }

    deletingPrice?.let { price ->
        ConfirmDeleteDialog(
            title = "Delete Price",
            message = "Are you sure you want to delete the price for \"${quantityById[price.quantity_id]?.quantity ?: "Quantity #${price.quantity_id}"}\"? This cannot be undone.",
            onDismiss = { deletingPrice = null },
            onConfirm = {
                val response = RetrofitClient.adminApi.deletePrice(price.quantity_id)
                if (response.isSuccessful) {
                    prices = prices.filterNot { it.id == price.id }
                    deletingPrice = null
                    Toast.makeText(context, "Price deleted successfully", Toast.LENGTH_LONG).show()
                    true
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                    false
                }
            },
        )
    }
}

@Composable
private fun <T> CatalogList(
    isLoading: Boolean,
    errorMessage: String?,
    isEmptyData: Boolean,
    onRetry: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    countLabel: String,
    items: List<T>,
    key: (T) -> Any,
    row: @Composable (T) -> Unit,
) {
    when {
        isLoading && isEmptyData -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        errorMessage != null && isEmptyData -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
                    Text("Retry")
                }
            }
        }
        else -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        placeholder = { Text(searchPlaceholder) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Text(
                        text = countLabel,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                items(items, key = key) { item -> row(item) }
            }
        }
    }
}

@Composable
private fun SimpleCatalogRow(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = title, style = MaterialTheme.typography.titleMedium)
                Text(text = subtitle, style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}

@Composable
private fun PriceRow(price: PriceResponse, quantityLabel: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = quantityLabel, style = MaterialTheme.typography.titleMedium)
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit price")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete price")
                    }
                }
            }
            Text(text = "ID: ${price.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Amount: ${price.amount}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun <T : Any> EditNameDialog(
    title: String,
    label: String,
    initialValue: String,
    onDismiss: () -> Unit,
    onSave: suspend (String) -> retrofit2.Response<T>,
    onSaved: (T) -> Unit,
) {
    var value by remember { mutableStateOf(initialValue) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text(label) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                enabled = value.isNotBlank() && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = onSave(value.trim())
                            if (response.isSuccessful) {
                                response.body()?.let(onSaved)
                            } else {
                                Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun EditPriceDialog(
    price: PriceResponse,
    quantityLabel: String,
    onDismiss: () -> Unit,
    onSaved: (PriceResponse) -> Unit,
) {
    var amount by remember { mutableStateOf(price.amount.toString()) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Edit Price") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Quantity: $quantityLabel", style = MaterialTheme.typography.bodyMedium)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { input -> amount = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = amount.toDoubleOrNull() != null && !isSubmitting,
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: return@Button
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.updatePrice(
                                quantityId = price.quantity_id,
                                body = PriceUpdateRequest(amount = amountValue),
                            )
                            if (response.isSuccessful) {
                                response.body()?.let(onSaved)
                            } else {
                                Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: suspend () -> Boolean,
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            onConfirm()
                        } catch (e: Exception) {
                            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Text("Delete")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSubmitting) {
                Text("Cancel")
            }
        },
    )
}
