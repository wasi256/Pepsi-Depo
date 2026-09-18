package com.example.pepsi.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pepsi.ui.components.PepsiTopBar

/** Shown when a signed-in user reaches a screen their permissions don't cover. */
@Composable
fun AccessDeniedScreen(onMenuClick: () -> Unit) {
    Scaffold(topBar = { PepsiTopBar(title = "Access denied", onMenuClick = onMenuClick) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LockedMessage(message = "Your role doesn't have permission to view this screen.")
        }
    }
}

/** Shown when the account is valid but its role holds no module permissions at all. */
@Composable
fun NoAccessScreen() {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            LockedMessage(
                message = "Your account is signed in, but its role has no permissions yet. " +
                    "Ask a system administrator to assign some, then sign in again.",
            )
            Button(onClick = { AuthSession.signOut() }) { Text("Sign out") }
        }
    }
}

@Composable
private fun LockedMessage(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = message, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
    }
}
