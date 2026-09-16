package com.example.pepsi.systemadmin.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.PriceCreateRequest
import com.example.pepsi.network.model.QuantityResponse
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch

/** Registers a price for a quantity directly against POST /admin/prices. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterPriceScreen(onDone: () -> Unit) {
    var quantities by remember { mutableStateOf<List<QuantityResponse>>(emptyList()) }
    var quantitiesLoading by remember { mutableStateOf(true) }
    var selectedQuantity by rememberSaveable { mutableStateOf<Int?>(null) }
    var quantityMenuExpanded by remember { mutableStateOf(false) }
    var amount by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        quantitiesLoading = true
        try {
            val response = RetrofitClient.adminApi.listQuantities()
            if (response.isSuccessful) {
                quantities = response.body()?.items ?: emptyList()
            } else {
                Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            quantitiesLoading = false
        }
    }

    val selectedQuantityLabel = quantities.firstOrNull { it.id == selectedQuantity }?.quantity ?: ""
    val canSave = selectedQuantity != null && amount.toDoubleOrNull() != null && !isSubmitting

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ExposedDropdownMenuBox(
            expanded = quantityMenuExpanded,
            onExpandedChange = { quantityMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedQuantityLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Quantity") },
                placeholder = {
                    Text(if (quantitiesLoading) "Loading quantities…" else if (quantities.isEmpty()) "No quantities registered yet" else "Select a quantity")
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = quantityMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = quantityMenuExpanded,
                onDismissRequest = { quantityMenuExpanded = false },
            ) {
                quantities.forEach { quantity ->
                    DropdownMenuItem(
                        text = { Text("${quantity.quantity} (#${quantity.id})") },
                        onClick = {
                            selectedQuantity = quantity.id
                            quantityMenuExpanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = amount,
            onValueChange = { input -> amount = input.filter { it.isDigit() || it == '.' } },
            label = { Text("Amount") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val quantityId = selectedQuantity ?: return@Button
                val amountValue = amount.toDoubleOrNull() ?: return@Button
                isSubmitting = true
                scope.launch {
                    try {
                        val response = RetrofitClient.adminApi.createPrice(
                            PriceCreateRequest(quantity_id = quantityId, amount = amountValue),
                        )
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Price registered successfully", Toast.LENGTH_LONG).show()
                            onDone()
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
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Save")
            }
        }
    }
}
