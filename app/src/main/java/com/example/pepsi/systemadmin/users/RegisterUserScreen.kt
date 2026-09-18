package com.example.pepsi.systemadmin.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.pepsi.common.model.CountryCodes
import com.example.pepsi.common.model.DefaultCountryCode
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.model.PersonnelCreateRequest
import com.example.pepsi.network.model.RoleResponse
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch

/** Registers personnel directly against POST /admin/personnel. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserScreen(onDone: () -> Unit) {
    var name by rememberSaveable { mutableStateOf("") }
    var dialCode by rememberSaveable { mutableStateOf(DefaultCountryCode.dialCode) }
    var localNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var gender by rememberSaveable { mutableStateOf<String?>(null) }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var salary by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    var roles by remember { mutableStateOf<List<RoleResponse>>(emptyList()) }
    var selectedRoleId by rememberSaveable { mutableStateOf<Int?>(null) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var rolesLoading by remember { mutableStateOf(true) }

    var depots by remember { mutableStateOf<List<DepotResponse>>(emptyList()) }
    var selectedDepotId by rememberSaveable { mutableStateOf<Int?>(null) }
    var depotMenuExpanded by remember { mutableStateOf(false) }
    var depotsLoading by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        rolesLoading = true
        try {
            val response = RetrofitClient.adminApi.listRoles()
            if (response.isSuccessful) roles = response.body()?.items ?: emptyList()
        } catch (e: Exception) {
            // Leave the dropdown empty; the user can retry by reopening the screen.
        } finally {
            rolesLoading = false
        }

        depotsLoading = true
        try {
            val response = RetrofitClient.adminApi.listDepots()
            if (response.isSuccessful) depots = response.body()?.items ?: emptyList()
        } catch (e: Exception) {
            // Leave the dropdown empty; the user can retry by reopening the screen.
        } finally {
            depotsLoading = false
        }
    }

    val country = CountryCodes.first { it.dialCode == dialCode }

    val canSave = name.isNotBlank() && localNumber.length == country.localDigits &&
        email.isNotBlank() && gender != null && selectedRoleId != null && selectedDepotId != null &&
        salary.toDoubleOrNull() != null && !isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register User") },
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
                label = { Text("Full name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ExposedDropdownMenuBox(
                    expanded = countryMenuExpanded,
                    onExpandedChange = { countryMenuExpanded = it },
                    modifier = Modifier.width(128.dp),
                ) {
                    OutlinedTextField(
                        value = country.display,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Code") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = countryMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    )
                    ExposedDropdownMenu(
                        expanded = countryMenuExpanded,
                        onDismissRequest = { countryMenuExpanded = false },
                    ) {
                        CountryCodes.forEach { option ->
                            DropdownMenuItem(
                                text = { Text("${option.display} ${option.countryName}") },
                                onClick = {
                                    dialCode = option.dialCode
                                    localNumber = localNumber.take(option.localDigits)
                                    countryMenuExpanded = false
                                },
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = localNumber,
                    onValueChange = { input -> localNumber = input.filter { it.isDigit() }.take(country.localDigits) },
                    label = { Text("Contact (${country.localDigits} digits)") },
                    singleLine = true,
                    isError = localNumber.isNotEmpty() && localNumber.length != country.localDigits,
                    supportingText = {
                        if (localNumber.isNotEmpty() && localNumber.length != country.localDigits) {
                            Text("Enter exactly ${country.localDigits} digits for ${country.countryName}")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )

            Column {
                Text("Gender", style = MaterialTheme.typography.bodyMedium)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Male", onClick = { gender = "Male" })
                    Text("Male", modifier = Modifier.padding(end = 16.dp))
                    RadioButton(selected = gender == "Female", onClick = { gender = "Female" })
                    Text("Female")
                }
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
                    placeholder = {
                        Text(if (rolesLoading) "Loading roles…" else if (roles.isEmpty()) "No roles registered yet" else "Select a role")
                    },
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
                    placeholder = {
                        Text(if (depotsLoading) "Loading depots…" else if (depots.isEmpty()) "No depots registered yet" else "Select a depot")
                    },
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

            Button(
                onClick = {
                    val roleId = selectedRoleId ?: return@Button
                    val depotId = selectedDepotId ?: return@Button
                    val genderValue = gender ?: return@Button
                    val salaryValue = salary.toDoubleOrNull() ?: return@Button

                    isSubmitting = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.adminApi.registerPersonnel(
                                listOf(
                                    PersonnelCreateRequest(
                                        role_id = roleId,
                                        depot_id = depotId,
                                        name = name.trim(),
                                        email = email.trim(),
                                        gender = genderValue,
                                        contact = "${country.dialCode}$localNumber",
                                        salary = salaryValue,
                                    ),
                                ),
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "User registered successfully", Toast.LENGTH_LONG).show()
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
