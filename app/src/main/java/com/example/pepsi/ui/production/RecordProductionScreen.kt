package com.example.pepsi.ui.production

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
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
import com.example.pepsi.data.network.FactoryApiService
import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.AdminProductDto
import com.example.pepsi.data.network.model.AdminQuantityDto
import com.example.pepsi.data.network.model.ProductionRequest
import com.example.pepsi.data.network.model.ProductionResponse
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityDropdown
import com.example.pepsi.ui.components.QuantityStepperField
import com.example.pepsi.ui.components.SearchableProductDropdown
import com.example.pepsi.util.formatApiDateTime
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun nowIso8601(): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date())
}

@Composable
fun RecordProductionScreen(onMenuClick: () -> Unit) {
    val apiService = remember { RetrofitClient.createService(FactoryApiService::class.java) }
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<AdminProductDto>>(emptyList()) }
    var productsLoading by remember { mutableStateOf(true) }
    var productsError by remember { mutableStateOf<String?>(null) }

    var packSizes by remember { mutableStateOf<List<AdminQuantityDto>>(emptyList()) }
    var packSizesLoading by remember { mutableStateOf(true) }
    var packSizesError by remember { mutableStateOf<String?>(null) }

    val recentEntries = remember { mutableStateListOf<ProductionResponse>() }

    var selectedProduct by remember { mutableStateOf<AdminProductDto?>(null) }
    var selectedPackSize by remember { mutableStateOf<AdminQuantityDto?>(null) }
    var quantity by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            products = apiService.getProducts(limit = 100).items
        } catch (e: Exception) {
            productsError = e.localizedMessage ?: "Failed to load products."
        } finally {
            productsLoading = false
        }
        try {
            packSizes = apiService.getQuantities(limit = 100).items
        } catch (e: Exception) {
            packSizesError = e.localizedMessage ?: "Failed to load pack sizes."
        } finally {
            packSizesLoading = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Record Production", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Record Production",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )

                    SearchableProductDropdown(
                        products = products,
                        selectedProduct = selectedProduct,
                        onProductSelected = { selectedProduct = it },
                        isLoading = productsLoading,
                    )
                    productsError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    QuantityDropdown(
                        quantities = packSizes,
                        selectedQuantity = selectedPackSize,
                        onQuantitySelected = { selectedPackSize = it },
                        isLoading = packSizesLoading,
                        label = "Pack Size",
                    )
                    packSizesError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }

                    QuantityStepperField(
                        quantity = quantity,
                        onQuantityChange = { quantity = it },
                        label = "Quantity Produced",
                    )

                    Button(
                        onClick = {
                            val product = selectedProduct ?: return@Button
                            val qty = quantity.toIntOrNull() ?: return@Button
                            if (qty <= 0) return@Button
                            isSubmitting = true
                            submitError = null
                            scope.launch {
                                try {
                                    val response = apiService.recordProduction(
                                        ProductionRequest(
                                            productId = product.id,
                                            quantityProduced = qty,
                                            productionDate = nowIso8601(),
                                        ),
                                    )
                                    recentEntries.add(0, response)
                                    selectedProduct = null
                                    selectedPackSize = null
                                    quantity = ""
                                } catch (e: Exception) {
                                    submitError = e.localizedMessage ?: "Failed to record production."
                                } finally {
                                    isSubmitting = false
                                }
                            }
                        },
                        enabled = !isSubmitting && selectedProduct != null && quantity.toIntOrNull()?.let { it > 0 } == true,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(if (isSubmitting) "Recording…" else "Record Production")
                    }

                    submitError?.let {
                        Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            if (recentEntries.isNotEmpty()) {
                item {
                    Text(
                        text = "Just Recorded",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(recentEntries.size) { index ->
                    val entry = recentEntries[index]
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = entry.productName, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Quantity Produced: ${entry.quantityProduced}",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            entry.productionDate?.let {
                                Text(text = "Produced: ${formatApiDateTime(it)}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
