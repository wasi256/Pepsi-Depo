package com.example.pepsi.systemadmin.depos

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
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.PermissionModule
import com.example.pepsi.common.data.UgandaDistricts
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.DepotCreateRequest
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.PepsiTopBar
import kotlinx.coroutines.launch

@Composable
fun DeposScreen(onMenuClick: () -> Unit, onRegisterDepo: () -> Unit) {
    var depots by remember { mutableStateOf<List<DepotResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var query by rememberSaveable { mutableStateOf("") }
    var editingDepot by remember { mutableStateOf<DepotResponse?>(null) }
    var deletingDepot by remember { mutableStateOf<DepotResponse?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun loadDepots() {
        isLoading = true
        errorMessage = null
        try {
            val response = RetrofitClient.adminApi.listDepots()
            if (response.isSuccessful) {
                depots = response.body()?.items ?: emptyList()
            } else {
                errorMessage = response.readErrorMessage()
            }
        } catch (e: Exception) {
            errorMessage = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { loadDepots() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val filteredDepots = if (query.isBlank()) {
        depots
    } else {
        depots.filter { depot ->
            listOf(depot.id.toString(), depot.name, depot.location).any { it.contains(query, ignoreCase = true) }
        }
    }

    val canCreate = AppAccess.canCreate(PermissionModule.ADMIN_DEPOTS)
    val canUpdate = AppAccess.canUpdate(PermissionModule.ADMIN_DEPOTS)
    val canDelete = AppAccess.canDelete(PermissionModule.ADMIN_DEPOTS)

    Scaffold(
        topBar = { PepsiTopBar(title = "Depo", onMenuClick = onMenuClick) },
        floatingActionButton = {
            if (canCreate) {
                ExtendedFloatingActionButton(
                    text = { Text("Register Depo") },
                    icon = { Icon(Icons.Filled.AddBusiness, contentDescription = null) },
                    onClick = onRegisterDepo,
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                isLoading && depots.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null && depots.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(text = errorMessage ?: "", style = MaterialTheme.typography.bodyMedium)
                        OutlinedButton(
                            onClick = { scope.launch { loadDepots() } },
                            modifier = Modifier.padding(top = 12.dp),
                        ) {
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
                                onValueChange = { query = it },
                                placeholder = { Text("Search by name, ID or location") },
                                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                                trailingIcon = {
                                    if (query.isNotEmpty()) {
                                        IconButton(onClick = { query = "" }) {
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
                                text = "${filteredDepots.size} of ${depots.size} registered depos",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                            )
                        }
                        items(filteredDepots, key = { it.id }) { depot ->
                            DepoRow(
                                depot = depot,
                                onEdit = if (canUpdate) ({ editingDepot = depot }) else null,
                                onDelete = if (canDelete) ({ deletingDepot = depot }) else null,
                            )
                        }
                    }
                }
            }
        }
    }

    editingDepot?.let { depot ->
        EditDepoDialog(
            depot = depot,
            onDismiss = { editingDepot = null },
            onSaved = { updated ->
                depots = depots.map { if (it.id == updated.id) updated else it }
                editingDepot = null
                Toast.makeText(context, "Depot updated successfully", Toast.LENGTH_LONG).show()
            },
        )
    }

    deletingDepot?.let { depot ->
        DeleteDepoDialog(
            depot = depot,
            onDismiss = { deletingDepot = null },
            onDeleted = {
                depots = depots.filterNot { it.id == depot.id }
                deletingDepot = null
                Toast.makeText(context, "Depot deleted successfully", Toast.LENGTH_LONG).show()
            },
        )
    }
}

@Composable
private fun DepoRow(depot: DepotResponse, onEdit: (() -> Unit)?, onDelete: (() -> Unit)?) {
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
                Text(text = depot.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit depot")
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete depot")
                        }
                    }
                }
            }
            Text(text = "ID: ${depot.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Location: ${depot.location}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDepoDialog(depot: DepotResponse, onDismiss: () -> Unit, onSaved: (DepotResponse) -> Unit) {
    var name by remember { mutableStateOf(depot.name) }
    var location by remember { mutableStateOf(depot.location) }
    var locationMenuExpanded by remember { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Edit Depot") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                        value = location,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Location (district)") },
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
            }
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && location.isNotBlank() && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.updateDepot(
                                depotId = depot.id,
                                body = DepotCreateRequest(name = name.trim(), location = location),
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
private fun DeleteDepoDialog(depot: DepotResponse, onDismiss: () -> Unit, onDeleted: () -> Unit) {
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Delete Depot") },
        text = { Text("Are you sure you want to delete \"${depot.name}\"? This cannot be undone.") },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.deleteDepot(depot.id)
                            if (response.isSuccessful) {
                                onDeleted()
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
