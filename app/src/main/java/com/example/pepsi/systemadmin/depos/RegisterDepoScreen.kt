package com.example.pepsi.systemadmin.depos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.data.UgandaDistricts
import com.example.pepsi.common.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDepoScreen(onDone: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf<String?>(null) }
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var attendant by rememberSaveable { mutableStateOf<String?>(null) }
    var attendantMenuExpanded by remember { mutableStateOf(false) }

    val depoAttendants = AdminDataStore.depoAttendants()
    val selectedAttendant: User? = depoAttendants.firstOrNull { it.id == attendant }
    val canSave = name.isNotBlank() && location != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = AdminDataStore.nextDepoId(),
            onValueChange = {},
            readOnly = true,
            label = { Text("ID (auto-generated)") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        ExposedDropdownMenuBox(
            expanded = locationMenuExpanded,
            onExpandedChange = { locationMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = location ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Location (district)") },
                placeholder = { Text("Select a district") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = locationMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = locationMenuExpanded,
                onDismissRequest = { locationMenuExpanded = false },
            ) {
                UgandaDistricts.forEach { district ->
                    DropdownMenuItem(
                        text = { Text(district) },
                        onClick = {
                            location = district
                            locationMenuExpanded = false
                        },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = attendantMenuExpanded,
            onExpandedChange = { attendantMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedAttendant?.fullName ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Assign Depo Attendant") },
                placeholder = { Text(if (depoAttendants.isEmpty()) "No depo attendants registered yet" else "Select a depo attendant") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = attendantMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = attendantMenuExpanded,
                onDismissRequest = { attendantMenuExpanded = false },
            ) {
                depoAttendants.forEach { user ->
                    DropdownMenuItem(
                        text = { Text("${user.fullName} (${user.id})") },
                        onClick = {
                            attendant = user.id
                            attendantMenuExpanded = false
                        },
                    )
                }
            }
        }

        Button(
            onClick = {
                AdminDataStore.registerDepo(
                    name = name.trim(),
                    location = location!!,
                    depoAttendantId = attendant,
                )
                onDone()
            },
            enabled = canSave,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Save")
        }
    }
}
