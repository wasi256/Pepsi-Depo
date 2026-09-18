package com.example.pepsi.systemadmin.users

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.PermissionModule
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.model.PersonnelCreateRequest
import com.example.pepsi.network.model.PermissionResponse
import com.example.pepsi.network.model.PersonnelResponse
import com.example.pepsi.network.model.RoleCreateRequest
import com.example.pepsi.network.model.RolePermissionsRequest
import com.example.pepsi.network.model.RoleResponse
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.PepsiTopBar
import kotlinx.coroutines.launch

private const val TAB_USERS = 0
private const val TAB_ROLES = 1

@Composable
fun UsersScreen(
    onMenuClick: () -> Unit,
    onRegisterUser: () -> Unit,
    onRegisterRole: () -> Unit,
) {
    val canReadUsers = AppAccess.canRead(PermissionModule.ADMIN_PERSONNEL)
    val canReadRoles = AppAccess.canRead(PermissionModule.ADMIN_ROLES)
    var requestedTab by rememberSaveable { mutableStateOf(TAB_USERS) }
    val selectedTab = when {
        requestedTab == TAB_USERS && canReadUsers -> TAB_USERS
        requestedTab == TAB_ROLES && canReadRoles -> TAB_ROLES
        canReadUsers -> TAB_USERS
        else -> TAB_ROLES
    }
    val tabs = buildList {
        if (canReadUsers) add(TAB_USERS)
        if (canReadRoles) add(TAB_ROLES)
    }
    val canEditUsers = AppAccess.canUpdate(PermissionModule.ADMIN_PERSONNEL)
    val canDeleteUsers = AppAccess.canDelete(PermissionModule.ADMIN_PERSONNEL)
    val canEditRoles = AppAccess.canUpdate(PermissionModule.ADMIN_ROLES)
    val canDeleteRoles = AppAccess.canDelete(PermissionModule.ADMIN_ROLES)
    val canViewPermissions = AppAccess.canRead(PermissionModule.AUTH_PERMISSIONS)
    var permissionsRole by remember { mutableStateOf<RoleResponse?>(null) }

    var personnel by remember { mutableStateOf<List<PersonnelResponse>>(emptyList()) }
    var personnelLoading by remember { mutableStateOf(true) }
    var personnelError by remember { mutableStateOf<String?>(null) }
    var personnelQuery by rememberSaveable { mutableStateOf("") }
    var editingPersonnel by remember { mutableStateOf<PersonnelResponse?>(null) }
    var deletingPersonnel by remember { mutableStateOf<PersonnelResponse?>(null) }

    var roles by remember { mutableStateOf<List<RoleResponse>>(emptyList()) }
    var rolesLoading by remember { mutableStateOf(true) }
    var rolesError by remember { mutableStateOf<String?>(null) }
    var roleQuery by rememberSaveable { mutableStateOf("") }
    var editingRole by remember { mutableStateOf<RoleResponse?>(null) }
    var deletingRole by remember { mutableStateOf<RoleResponse?>(null) }

    var depots by remember { mutableStateOf<List<DepotResponse>>(emptyList()) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    suspend fun loadPersonnel() {
        personnelLoading = true
        personnelError = null
        try {
            val response = RetrofitClient.adminApi.listPersonnel()
            if (response.isSuccessful) {
                personnel = response.body()?.items ?: emptyList()
            } else {
                personnelError = response.readErrorMessage()
            }
        } catch (e: Exception) {
            personnelError = "Network error: ${e.message}"
        } finally {
            personnelLoading = false
        }
    }

    suspend fun loadRoles() {
        rolesLoading = true
        rolesError = null
        try {
            val response = RetrofitClient.adminApi.listRoles()
            if (response.isSuccessful) {
                roles = response.body()?.items ?: emptyList()
            } else {
                rolesError = response.readErrorMessage()
            }
        } catch (e: Exception) {
            rolesError = "Network error: ${e.message}"
        } finally {
            rolesLoading = false
        }
    }

    suspend fun loadDepots() {
        try {
            val response = RetrofitClient.adminApi.listDepots()
            if (response.isSuccessful) {
                depots = response.body()?.items ?: emptyList()
            }
        } catch (e: Exception) {
            // Depot names are a display nicety; leave the list empty and fall back to raw IDs.
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch {
                    if (canReadUsers) loadPersonnel()
                    if (canReadRoles) loadRoles()
                    loadDepots()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val roleById = roles.associateBy { it.id }
    val depotById = depots.associateBy { it.id }

    Scaffold(
        topBar = { PepsiTopBar(title = "Users", onMenuClick = onMenuClick) },
        floatingActionButton = {
            val canCreateHere = if (selectedTab == TAB_USERS) {
                AppAccess.canCreate(PermissionModule.ADMIN_PERSONNEL)
            } else {
                AppAccess.canCreate(PermissionModule.ADMIN_ROLES)
            }
            if (canCreateHere) {
                ExtendedFloatingActionButton(
                    text = { Text(if (selectedTab == TAB_USERS) "Register User" else "Add Role") },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = { if (selectedTab == TAB_USERS) onRegisterUser() else onRegisterRole() },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary,
                )
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0)) {
                tabs.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { requestedTab = tab },
                        text = { Text(if (tab == TAB_USERS) "Users" else "Roles") },
                    )
                }
            }

            if (selectedTab == TAB_USERS) {
                val filtered = if (personnelQuery.isBlank()) {
                    personnel
                } else {
                    personnel.filter { person ->
                        val roleLabel = roleById[person.role_id]?.name.orEmpty()
                        val depotLabel = depotById[person.depot_id]?.name.orEmpty()
                        listOf(person.id.toString(), person.name, person.email.orEmpty(), person.contact, roleLabel, depotLabel)
                            .any { it.contains(personnelQuery, ignoreCase = true) }
                    }
                }
                CatalogList(
                    isLoading = personnelLoading,
                    errorMessage = personnelError,
                    isEmptyData = personnel.isEmpty(),
                    onRetry = { scope.launch { loadPersonnel() } },
                    query = personnelQuery,
                    onQueryChange = { personnelQuery = it },
                    searchPlaceholder = "Search by name, email, role or depot",
                    countLabel = "${filtered.size} of ${personnel.size} users",
                    items = filtered,
                    key = { it.id },
                ) { person ->
                    PersonnelRow(
                        person = person,
                        roleLabel = roleById[person.role_id]?.name ?: person.role_id?.let { "Role #$it" } ?: "Unassigned",
                        depotLabel = depotById[person.depot_id]?.name ?: person.depot_id?.let { "Depot #$it" } ?: "Unassigned",
                        onEdit = if (canEditUsers) ({ editingPersonnel = person }) else null,
                        onDelete = if (canDeleteUsers) ({ deletingPersonnel = person }) else null,
                    )
                }
            } else {
                val filteredRoles = if (roleQuery.isBlank()) {
                    roles
                } else {
                    roles.filter { listOf(it.id.toString(), it.name).any { field -> field.contains(roleQuery, true) } }
                }
                CatalogList(
                    isLoading = rolesLoading,
                    errorMessage = rolesError,
                    isEmptyData = roles.isEmpty(),
                    onRetry = { scope.launch { loadRoles() } },
                    query = roleQuery,
                    onQueryChange = { roleQuery = it },
                    searchPlaceholder = "Search by role name or ID",
                    countLabel = "${filteredRoles.size} of ${roles.size} roles",
                    items = filteredRoles,
                    key = { it.id },
                ) { role ->
                    RoleRow(
                        role = role,
                        onEdit = if (canEditRoles) ({ editingRole = role }) else null,
                        onDelete = if (canDeleteRoles) ({ deletingRole = role }) else null,
                        onPermissions = if (canViewPermissions) ({ permissionsRole = role }) else null,
                    )
                }
            }
        }
    }

    editingPersonnel?.let { person ->
        EditPersonnelDialog(
            person = person,
            roles = roles,
            depots = depots,
            onDismiss = { editingPersonnel = null },
            onSaved = { updated ->
                personnel = personnel.map { if (it.id == updated.id) updated else it }
                editingPersonnel = null
                Toast.makeText(context, "User updated successfully", Toast.LENGTH_LONG).show()
            },
        )
    }

    deletingPersonnel?.let { person ->
        ConfirmDeleteDialog(
            title = "Delete User",
            message = "Are you sure you want to delete \"${person.name}\"? This cannot be undone.",
            onDismiss = { deletingPersonnel = null },
            onConfirm = {
                val response = RetrofitClient.adminApi.deletePersonnel(person.id)
                if (response.isSuccessful) {
                    personnel = personnel.filterNot { it.id == person.id }
                    deletingPersonnel = null
                    Toast.makeText(context, "User deleted successfully", Toast.LENGTH_LONG).show()
                    true
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                    false
                }
            },
        )
    }

    editingRole?.let { role ->
        EditRoleDialog(
            role = role,
            onDismiss = { editingRole = null },
            onSaved = { updated ->
                roles = roles.map { if (it.id == updated.id) updated else it }
                editingRole = null
                Toast.makeText(context, "Role updated successfully", Toast.LENGTH_LONG).show()
            },
        )
    }

    permissionsRole?.let { role ->
        RolePermissionsDialog(role = role, onDismiss = { permissionsRole = null })
    }

    deletingRole?.let { role ->
        ConfirmDeleteDialog(
            title = "Delete Role",
            message = "Are you sure you want to delete \"${role.name}\"? This cannot be undone.",
            onDismiss = { deletingRole = null },
            onConfirm = {
                val response = RetrofitClient.adminApi.deleteRole(role.id)
                if (response.isSuccessful) {
                    roles = roles.filterNot { it.id == role.id }
                    deletingRole = null
                    Toast.makeText(context, "Role deleted successfully", Toast.LENGTH_LONG).show()
                    true
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                    false
                }
            },
        )
    }
}

@Composable
private fun <T> CatalogList(
    isLoading: Boolean,
    errorMessage: String?,
    isEmptyData: Boolean,
    onRetry: () -> Unit,
    query: String,
    onQueryChange: (String) -> Unit,
    searchPlaceholder: String,
    countLabel: String,
    items: List<T>,
    key: (T) -> Any,
    row: @Composable (T) -> Unit,
) {
    when {
        isLoading && isEmptyData -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        errorMessage != null && isEmptyData -> {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = errorMessage, style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onRetry, modifier = Modifier.padding(top = 12.dp)) {
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
                        onValueChange = onQueryChange,
                        placeholder = { Text(searchPlaceholder) },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { onQueryChange("") }) {
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
                        text = countLabel,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                items(items, key = key) { item -> row(item) }
            }
        }
    }
}

@Composable
private fun PersonnelRow(
    person: PersonnelResponse,
    roleLabel: String,
    depotLabel: String,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
) {
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
                Text(text = person.name, style = MaterialTheme.typography.titleMedium)
                Row {
                    if (onEdit != null) {
                        IconButton(onClick = onEdit) {
                            Icon(Icons.Filled.Edit, contentDescription = "Edit user")
                        }
                    }
                    if (onDelete != null) {
                        IconButton(onClick = onDelete) {
                            Icon(Icons.Filled.Delete, contentDescription = "Delete user")
                        }
                    }
                }
            }
            Text(text = "ID: ${person.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Role: $roleLabel", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Depot: $depotLabel", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Email: ${person.email.orEmpty()}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Contact: ${person.contact}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Gender: ${person.gender}", style = MaterialTheme.typography.bodyMedium)
            person.salary?.let { Text(text = "Salary: $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun RoleRow(
    role: RoleResponse,
    onEdit: (() -> Unit)?,
    onDelete: (() -> Unit)?,
    onPermissions: (() -> Unit)?,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(text = role.name, style = MaterialTheme.typography.titleMedium)
                Text(text = "ID: ${role.id}", style = MaterialTheme.typography.bodyMedium)
            }
            Row {
                if (onPermissions != null) {
                    IconButton(onClick = onPermissions) {
                        Icon(Icons.Filled.Security, contentDescription = "Role permissions")
                    }
                }
                if (onEdit != null) {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit role")
                    }
                }
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete role")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditPersonnelDialog(
    person: PersonnelResponse,
    roles: List<RoleResponse>,
    depots: List<DepotResponse>,
    onDismiss: () -> Unit,
    onSaved: (PersonnelResponse) -> Unit,
) {
    var name by remember { mutableStateOf(person.name) }
    var email by remember { mutableStateOf(person.email.orEmpty()) }
    var contact by remember { mutableStateOf(person.contact) }
    var gender by remember { mutableStateOf(person.gender) }
    var genderMenuExpanded by remember { mutableStateOf(false) }
    var selectedRoleId by remember { mutableStateOf(person.role_id) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var selectedDepotId by remember { mutableStateOf(person.depot_id) }
    var depotMenuExpanded by remember { mutableStateOf(false) }
    var salary by remember { mutableStateOf(person.salary.orEmpty()) }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val canSave = name.isNotBlank() && contact.isNotBlank() && selectedRoleId != null &&
        selectedDepotId != null && salary.toDoubleOrNull() != null && !isSubmitting

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Edit User") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("Contact") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                    Text("Male", modifier = Modifier.padding(end = 12.dp))
                    RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                    Text("Female")
                }
                ExposedDropdownMenuBox(
                    expanded = roleMenuExpanded,
                    onExpandedChange = { roleMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = roles.firstOrNull { it.id == selectedRoleId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = { selectedRoleId = role.id; roleMenuExpanded = false },
                            )
                        }
                    }
                }
                ExposedDropdownMenuBox(
                    expanded = depotMenuExpanded,
                    onExpandedChange = { depotMenuExpanded = it },
                ) {
                    OutlinedTextField(
                        value = depots.firstOrNull { it.id == selectedDepotId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Depot") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = depotMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(expanded = depotMenuExpanded, onDismissRequest = { depotMenuExpanded = false }) {
                        depots.forEach { depot ->
                            DropdownMenuItem(
                                text = { Text(depot.name) },
                                onClick = { selectedDepotId = depot.id; depotMenuExpanded = false },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = salary,
                    onValueChange = { input -> salary = input.filter { it.isDigit() || it == '.' } },
                    label = { Text("Salary") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                enabled = canSave,
                onClick = {
                    val roleId = selectedRoleId ?: return@Button
                    val depotId = selectedDepotId ?: return@Button
                    val salaryValue = salary.toDoubleOrNull() ?: return@Button
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.updatePersonnel(
                                personnelId = person.id,
                                body = PersonnelCreateRequest(
                                    role_id = roleId,
                                    depot_id = depotId,
                                    name = name.trim(),
                                    email = email.trim(),
                                    gender = gender,
                                    contact = contact.trim(),
                                    salary = salaryValue,
                                ),
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
private fun EditRoleDialog(role: RoleResponse, onDismiss: () -> Unit, onSaved: (RoleResponse) -> Unit) {
    var name by remember { mutableStateOf(role.name) }
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text("Edit Role") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Role name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.updateRole(role.id, RoleCreateRequest(name.trim()))
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
private fun ConfirmDeleteDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: suspend () -> Boolean,
) {
    var isSubmitting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            Button(
                enabled = !isSubmitting,
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        try {
                            onConfirm()
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

/**
 * Lists every permission on the platform grouped by module, ticked for the ones this role
 * holds. Saving assigns the newly ticked permissions and revokes the unticked ones.
 * Only users who can update permissions may change the ticks.
 */
@Composable
private fun RolePermissionsDialog(role: RoleResponse, onDismiss: () -> Unit) {
    val canEdit = AppAccess.canUpdate(PermissionModule.AUTH_PERMISSIONS)
    var allPermissions by remember { mutableStateOf<List<PermissionResponse>>(emptyList()) }
    var granted by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var original by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    androidx.compose.runtime.LaunchedEffect(role.id) {
        try {
            val all = RetrofitClient.authApi.listPermissions()
            val mine = RetrofitClient.authApi.listRolePermissions(role.id)
            if (all.isSuccessful && mine.isSuccessful) {
                allPermissions = all.body().orEmpty()
                original = mine.body().orEmpty().map { it.id }.toSet()
                granted = original
            } else {
                loadError = (if (!all.isSuccessful) all else mine).readErrorMessage()
            }
        } catch (e: Exception) {
            loadError = "Network error: ${e.message}"
        } finally {
            isLoading = false
        }
    }

    val changed = granted != original

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Permissions: ${role.name}") },
        text = {
            when {
                isLoading -> Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                loadError != null -> Text(loadError.orEmpty(), color = MaterialTheme.colorScheme.error)
                else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    allPermissions
                        .groupBy { it.module_name ?: it.module_key }
                        .forEach { (module, permissions) ->
                            Text(
                                text = module,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                            permissions.forEach { permission ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(
                                        checked = permission.id in granted,
                                        enabled = canEdit && !isSaving,
                                        onCheckedChange = { checked ->
                                            granted = if (checked) granted + permission.id else granted - permission.id
                                        },
                                    )
                                    Text(permission.action.replaceFirstChar { it.uppercase() })
                                }
                            }
                        }
                }
            }
        },
        confirmButton = {
            if (canEdit) {
                Button(
                    enabled = changed && !isSaving && !isLoading && loadError == null,
                    onClick = {
                        isSaving = true
                        scope.launch {
                            try {
                                val toAdd = (granted - original).toList()
                                val toRemove = (original - granted).toList()
                                var failure: String? = null
                                if (toAdd.isNotEmpty()) {
                                    val response = RetrofitClient.authApi.assignRolePermissions(role.id, RolePermissionsRequest(toAdd))
                                    if (!response.isSuccessful) failure = response.readErrorMessage()
                                }
                                if (failure == null) {
                                    for (id in toRemove) {
                                        val response = RetrofitClient.authApi.revokeRolePermission(role.id, id)
                                        if (!response.isSuccessful) {
                                            failure = response.readErrorMessage()
                                            break
                                        }
                                    }
                                }
                                if (failure == null) {
                                    Toast.makeText(context, "Permissions updated. Affected users see the change when they next sign in.", Toast.LENGTH_LONG).show()
                                    onDismiss()
                                } else {
                                    Toast.makeText(context, failure, Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
                            } finally {
                                isSaving = false
                            }
                        }
                    },
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Save")
                    }
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss, enabled = !isSaving) {
                Text(if (canEdit) "Cancel" else "Close")
            }
        },
    )
}
