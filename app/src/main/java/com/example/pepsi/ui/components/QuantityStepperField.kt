package com.example.pepsi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun QuantityStepperField(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "Quantity",
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = quantity,
            onValueChange = { onQuantityChange(it.filter(Char::isDigit)) },
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Column(modifier = Modifier.height(56.dp), verticalArrangement = Arrangement.SpaceBetween) {
            IconButton(
                onClick = { onQuantityChange(((quantity.toIntOrNull() ?: 0) + 1).toString()) },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase quantity")
            }
            IconButton(
                onClick = {
                    onQuantityChange(((quantity.toIntOrNull() ?: 0) - 1).coerceAtLeast(0).toString())
                },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease quantity")
            }
        }
    }
}
