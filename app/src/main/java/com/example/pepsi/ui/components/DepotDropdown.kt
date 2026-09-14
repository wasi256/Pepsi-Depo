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
import com.example.pepsi.data.network.model.AdminDepotDto

/** A dropdown for picking which depot the attendant is acting for. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DepotDropdown(
    depots: List<AdminDepotDto>,
    selectedDepot: AdminDepotDto?,
    onDepotSelected: (AdminDepotDto) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier,
    label: String = "Depot",
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (depots.isNotEmpty()) expanded = it },
        modifier = modifier,
    ) {
        OutlinedTextField(
            value = selectedDepot?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(if (isLoading) "Loading depots…" else label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            depots.forEach { depot ->
                DropdownMenuItem(
                    text = { Text("${depot.name} — ${depot.location}") },
                    onClick = {
                        onDepotSelected(depot)
                        expanded = false
                    },
                )
            }
        }
    }
}
