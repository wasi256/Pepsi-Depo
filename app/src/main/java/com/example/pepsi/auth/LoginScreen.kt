package com.example.pepsi.auth

import android.util.Patterns
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pepsi.R
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.LoginRequest
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(onCreateAccount: () -> Unit) {
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    val emailValid = Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val canSubmit = emailValid && password.isNotEmpty() && !isSubmitting

    fun submit() {
        if (!canSubmit) return
        isSubmitting = true
        errorMessage = null
        scope.launch {
            try {
                val response = RetrofitClient.authApi.login(LoginRequest(email.trim(), password))
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    AuthSession.signIn(body)
                } else {
                    errorMessage = if (response.isSuccessful) "The server sent an empty response." else response.readErrorMessage()
                }
            } catch (e: Exception) {
                errorMessage = "Couldn't reach the server (${e.message}). It may be waking up, so try again in a moment."
            } finally {
                isSubmitting = false
            }
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(R.drawable.pepsi_logo),
                contentDescription = "Pepsi logo",
                modifier = Modifier.size(96.dp),
            )
            Text(text = "Pepsi Depo", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                text = "Sign in to continue",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                singleLine = true,
                isError = email.isNotBlank() && !emailValid,
                supportingText = { if (email.isNotBlank() && !emailValid) Text("Enter a valid email address") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )

            errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
            }

            Button(
                onClick = ::submit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth(),
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text("Sign in")
                }
            }

            TextButton(onClick = onCreateAccount, enabled = !isSubmitting) {
                Text("Don't have an account? Create account")
            }
        }
    }
}
