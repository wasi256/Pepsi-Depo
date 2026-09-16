package com.example.pepsi.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.repository.FactoryRepository
import com.example.pepsi.data.session.SessionManager
import kotlinx.coroutines.launch

/**
 * Wraps a Factory-API-backed screen: shows a loading state while a saved
 * session is being validated, a login form when signed out, and [content]
 * once authenticated. Keeps the same title/menu icon as the real screen so
 * signing in doesn't feel like a different destination.
 */
@Composable
fun LoginGate(
    title: String,
    onMenuClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    when {
        SessionManager.isRestoring -> {
            Scaffold(
                topBar = { PepsiTopBar(title = title, onMenuClick = onMenuClick) },
            ) { padding ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        !SessionManager.isLoggedIn -> {
            LoginForm(title = title, onMenuClick = onMenuClick)
        }
        else -> content()
    }
}

@Composable
private fun LoginForm(title: String, onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { PepsiTopBar(title = title, onMenuClick = onMenuClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Sign in required",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "This screen reads live data from the Pepsi Depo server. Sign in with your factory account to continue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 20.dp),
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it; errorMessage = null },
                label = { Text("Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; errorMessage = null },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            )
            errorMessage?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            Button(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    scope.launch {
                        FactoryRepository.login(email.trim(), password)
                            .onSuccess { token ->
                                SessionManager.onLoggedIn(context, token.accessToken, token.user)
                            }
                            .onFailure { errorMessage = it.message }
                        isLoading = false
                    }
                },
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text("Log In")
                }
            }
        }
    }
}
