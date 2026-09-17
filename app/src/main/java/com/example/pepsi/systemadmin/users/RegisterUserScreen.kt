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
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.CountryCodes
import com.example.pepsi.common.model.DefaultCountryCode
import com.example.pepsi.common.model.Gender
import com.example.pepsi.common.model.Role
import com.example.pepsi.common.util.generatePassword
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.PersonnelCreateRequest
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserScreen(onDone: () -> Unit) {
    var firstName by rememberSaveable { mutableStateOf("") }
    var lastName by rememberSaveable { mutableStateOf("") }
    var dialCode by rememberSaveable { mutableStateOf(DefaultCountryCode.dialCode) }
    var localNumber by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf(generatePassword()) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var gender by rememberSaveable { mutableStateOf<Gender?>(null) }
    var role by rememberSaveable { mutableStateOf<Role?>(null) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var countryMenuExpanded by remember { mutableStateOf(false) }
    var roleId by rememberSaveable { mutableStateOf("") }
    var depotId by rememberSaveable { mutableStateOf("") }
    var salary by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val country = CountryCodes.first { it.dialCode == dialCode }

    val canSave = firstName.isNotBlank() && lastName.isNotBlank() &&
        localNumber.length == country.localDigits && email.isNotBlank() &&
        gender != null && role != null &&
        roleId.toIntOrNull() != null && depotId.toIntOrNull() != null && salary.toDoubleOrNull() != null &&
        !isSubmitting

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
            value = AdminDataStore.nextUserId(),
            onValueChange = {},
            readOnly = true,
            label = { Text("ID (auto-generated)") },
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = firstName,
            onValueChange = { input -> firstName = input.filter { it.isLetter() || it == ' ' } },
            label = { Text("First name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = lastName,
            onValueChange = { input -> lastName = input.filter { it.isLetter() || it == ' ' } },
            label = { Text("Last name") },
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
                label = { Text("Tel (${country.localDigits} digits)") },
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
            label = { Text("Company email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth(),
        )

        OutlinedTextField(
            value = password,
            onValueChange = {},
            readOnly = true,
            label = { Text("Password (auto-generated)") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                Row {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        )
                    }
                    IconButton(onClick = { password = generatePassword() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Regenerate password")
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )

        Column {
            Text("Gender", style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = gender == Gender.MALE, onClick = { gender = Gender.MALE })
                Text("Male", modifier = Modifier.padding(end = 16.dp))
                RadioButton(selected = gender == Gender.FEMALE, onClick = { gender = Gender.FEMALE })
                Text("Female")
            }
        }

        ExposedDropdownMenuBox(
            expanded = roleMenuExpanded,
            onExpandedChange = { roleMenuExpanded = it },
        ) {
            OutlinedTextField(
                value = role?.label ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = roleMenuExpanded,
                onDismissRequest = { roleMenuExpanded = false },
            ) {
                listOf(Role.MANAGER, Role.DEPO_ATTENDANT).forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.label) },
                        onClick = {
                            role = option
                            roleMenuExpanded = false
                        },
                    )
                }
            }
        }

        Text(
            text = "Backend linking (temporary — until role/depot lookups are wired up)",
            style = MaterialTheme.typography.bodyMedium,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                value = roleId,
                onValueChange = { input -> roleId = input.filter { it.isDigit() } },
                label = { Text("Role ID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = depotId,
                onValueChange = { input -> depotId = input.filter { it.isDigit() } },
                label = { Text("Depot ID") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
            )
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
                val roleIdValue = roleId.toIntOrNull()
                val depotIdValue = depotId.toIntOrNull()
                val salaryValue = salary.toDoubleOrNull()
                if (roleIdValue == null || depotIdValue == null || salaryValue == null) return@Button

                isSubmitting = true
                scope.launch {
                    try {
                        val response = RetrofitClient.adminApi.registerPersonnel(
                            PersonnelCreateRequest(
                                role_id = roleIdValue,
                                depot_id = depotIdValue,
                                name = "${firstName.trim()} ${lastName.trim()}".trim(),
                                email = email.trim(),
                                gender = if (gender == Gender.MALE) "Male" else "Female",
                                contact = "${country.dialCode}$localNumber",
                                salary = salaryValue,
                            ),
                        )
                        if (response.isSuccessful) {
                            AdminDataStore.registerUser(
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                tel = "${country.dialCode} $localNumber",
                                email = email.trim(),
                                password = password,
                                gender = gender!!,
                                role = role!!,
                            )
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
