package com.example.pepsi.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.pepsi.data.network.model.AdminProductDto

/**
 * A product dropdown whose text field doubles as a search box, filtering
 * [products] by name as the user types.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableProductDropdown(
    products: List<AdminProductDto>,
    selectedProduct: AdminProductDto?,
    onProductSelected: (AdminProductDto) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    label: String = "Product",
) {
    var expanded by remember { mutableStateOf(false) }
    var query by remember(selectedProduct) { mutableStateOf(selectedProduct?.name ?: "") }
    val filteredProducts = remember(query, products) {
        if (query.isBlank()) products else products.filter { it.name.contains(query, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (products.isNotEmpty()) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                query = it
                expanded = true
            },
            label = { Text(if (isLoading) "Loading products…" else label) },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable),
        )
        if (filteredProducts.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                filteredProducts.forEach { product ->
                    DropdownMenuItem(
                        text = { Text(product.name) },
                        onClick = {
                            onProductSelected(product)
                            query = product.name
                            expanded = false
                        },
                    )
                }
            }
        }
    }
}
