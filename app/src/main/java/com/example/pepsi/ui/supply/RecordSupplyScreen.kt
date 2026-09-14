package com.example.pepsi.ui.supply

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import com.example.pepsi.data.network.model.SupplyRequest
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityDropdown
import com.example.pepsi.ui.components.QuantityStepperField
import com.example.pepsi.ui.components.SearchableProductDropdown
import kotlinx.coroutines.launch
import retrofit2.HttpException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSupplyScreen(onMenuClick: () -> Unit) {
    val apiService = remember { RetrofitClient.createService(FactoryApiService::class.java) }
    val scope = rememberCoroutineScope()

    var products by remember { mutableStateOf<List<AdminProductDto>>(emptyList()) }
    var productsLoading by remember { mutableStateOf(true) }
    var productsError by remember { mutableStateOf<String?>(null) }

    var quantities by remember { mutableStateOf<List<AdminQuantityDto>>(emptyList()) }
    var quantitiesLoading by remember { mutableStateOf(true) }
    var quantitiesError by remember { mutableStateOf<String?>(null) }

    var selectedProduct by remember { mutableStateOf<AdminProductDto?>(null) }
    var selectedQuantity by remember { mutableStateOf<AdminQuantityDto?>(null) }
    var amount by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var submitError by remember { mutableStateOf<String?>(null) }
    var confirmation by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            products = apiService.getProducts(limit = 100).items
        } catch (e: Exception) {
            productsError = e.localizedMessage ?: "Failed to load products."
        } finally {
            productsLoading = false
        }
        try {
            quantities = apiService.getQuantities(limit = 100).items
        } catch (e: Exception) {
            quantitiesError = e.localizedMessage ?: "Failed to load quantities."
        } finally {
            quantitiesLoading = false
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Record Supply", onMenuClick = onMenuClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "Record Supply",
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
                quantities = quantities,
                selectedQuantity = selectedQuantity,
                onQuantitySelected = { selectedQuantity = it },
                isLoading = quantitiesLoading,
            )
            quantitiesError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            QuantityStepperField(
                quantity = amount,
                onQuantityChange = { amount = it },
                label = "Amount",
            )

            Button(
                onClick = {
                    val product = selectedProduct ?: return@Button
                    val quantityDto = selectedQuantity ?: return@Button
                    val amountValue = amount.toIntOrNull() ?: return@Button
                    if (amountValue <= 0) return@Button
                    isSubmitting = true
                    submitError = null
                    confirmation = null
                    scope.launch {
                        try {
                            val response = apiService.createSupply(
                                SupplyRequest(
                                    productId = product.id,
                                    quantityId = quantityDto.id,
                                    amount = amountValue,
                                ),
                            )
                            confirmation = "Supply of ${response.amount} x ${response.productName} " +
                                "(${response.quantityValue}) recorded."
                            selectedProduct = null
                            selectedQuantity = null
                            amount = ""
                        } catch (e: HttpException) {
                            submitError = if (e.code() == 409) {
                                "Not enough factory stock available for this supply."
                            } else {
                                e.localizedMessage ?: "Failed to record supply."
                            }
                        } catch (e: Exception) {
                            submitError = e.localizedMessage ?: "Failed to record supply."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = !isSubmitting &&
                    selectedProduct != null &&
                    selectedQuantity != null &&
                    amount.toIntOrNull()?.let { it > 0 } == true,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (isSubmitting) "Recording…" else "Record Supply")
            }

            submitError?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
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
}
