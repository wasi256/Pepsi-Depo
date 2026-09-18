package com.example.pepsi.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.AuthUserCreateRequest
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch

/** Creates a sign-in account for an existing personnel record via POST /auth/users. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(onBack: () -> Unit, onCreated: () -> Unit) {
    var username by rememberSaveable { mutableStateOf("") }
    var personnelId by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val passwordMismatch = confirmPassword.isNotEmpty() && confirmPassword != password
    val canSubmit = username.isNotBlank() && personnelId.toIntOrNull() != null &&
        password.isNotEmpty() && confirmPassword == password && !isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create account") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isSubmitting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to sign in")
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Accounts are linked to a personnel record your administrator has already registered. " +
                    "After creating one, sign in with the email on that personnel record.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = personnelId,
                onValueChange = { input -> personnelId = input.filter { it.isDigit() } },
                label = { Text("Personnel ID") },
                supportingText = { Text("Ask your administrator for this number") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm password") },
                singleLine = true,
                isError = passwordMismatch,
                supportingText = { if (passwordMismatch) Text("Passwords do not match") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = {
                    val personnel = personnelId.toIntOrNull() ?: return@Button
                    isSubmitting = true
                    errorMessage = null
                    scope.launch {
                        try {
                            val response = RetrofitClient.authApi.createUsers(
                                listOf(AuthUserCreateRequest(username.trim(), password, personnel)),
                            )
                            if (response.isSuccessful) {
                                Toast.makeText(context, "Account created. You can sign in now.", Toast.LENGTH_LONG).show()
                                onCreated()
                            } else {
                                errorMessage = response.readErrorMessage()
                            }
                        } catch (e: Exception) {
                            errorMessage = "Couldn't reach the server (${e.message}). Try again in a moment."
                        } finally {
                            isSubmitting = false
                        }
                    }
                },
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Create account")
                }
            }
        }
    }
}
