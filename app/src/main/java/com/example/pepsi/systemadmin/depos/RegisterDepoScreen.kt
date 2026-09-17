package com.example.pepsi.systemadmin.depos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.pepsi.common.data.UgandaDistricts
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.DepotCreateRequest
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch

/**
 * Registers a depot directly against POST /admin/depots. Standalone screen:
 * it doesn't read or write any local mock state, only the real backend.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterDepoScreen(onDone: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf<String?>(null) }
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val canSave = name.isNotBlank() && location != null && !isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Depo") },
                navigationIcon = {
                    IconButton(onClick = onDone) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
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

        Button(
            onClick = {
                isSubmitting = true
                scope.launch {
                    try {
                        val response = RetrofitClient.adminApi.createDepot(
                            DepotCreateRequest(
                                name = name.trim(),
                                location = location!!,
                            ),
                        )
                        if (response.isSuccessful) {
                            Toast.makeText(context, "Depot registered successfully", Toast.LENGTH_LONG).show()
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
}
