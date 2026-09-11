package com.example.pepsi.systemadmin.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.CountryCodes
import com.example.pepsi.common.model.DefaultCountryCode
import com.example.pepsi.common.model.Gender
import com.example.pepsi.common.model.Role
import com.example.pepsi.common.util.generatePassword

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

    val country = CountryCodes.first { it.dialCode == dialCode }

    val canSave = firstName.isNotBlank() && lastName.isNotBlank() &&
        localNumber.length == country.localDigits && email.isNotBlank() &&
        gender != null && role != null

    Column(
        modifier = Modifier
            .fillMaxSize()
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

        Button(
            onClick = {
                AdminDataStore.registerUser(
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    tel = "${country.dialCode} $localNumber",
                    email = email.trim(),
                    password = password,
                    gender = gender!!,
                    role = role!!,
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
