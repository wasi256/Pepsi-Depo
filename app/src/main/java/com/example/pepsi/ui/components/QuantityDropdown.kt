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
import com.example.pepsi.data.network.model.AdminQuantityDto

/** A dropdown for picking one of the Admin `quantities` (pack sizes, e.g. 320ml, 1.5L). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityDropdown(
    quantities: List<AdminQuantityDto>,
    selectedQuantity: AdminQuantityDto?,
    onQuantitySelected: (AdminQuantityDto) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    label: String = "Quantity",
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (quantities.isNotEmpty()) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedQuantity?.quantity ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isLoading) "Loading quantities…" else label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            quantities.forEach { quantity ->
                DropdownMenuItem(
                    text = { Text(quantity.quantity) },
                    onClick = {
                        onQuantitySelected(quantity)
                        expanded = false
                    },
                )
            }
        }
    }
}
