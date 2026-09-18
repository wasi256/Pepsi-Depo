package com.example.pepsi.systemadmin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun SettingsScreen(onMenuClick: () -> Unit) {
    Scaffold(
        topBar = { PepsiTopBar(title = "Settings", onMenuClick = onMenuClick) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Account", style = MaterialTheme.typography.titleMedium)
                    val user = AuthSession.user
                    Text(text = user?.personnel_name ?: "Unknown user", style = MaterialTheme.typography.bodyLarge)
                    user?.username?.let { Text(text = "Username: $it", style = MaterialTheme.typography.bodyMedium) }
                    user?.role_name?.let { Text(text = "Role: $it", style = MaterialTheme.typography.bodyMedium) }
                    Text(
                        text = "${AuthSession.permissions.size} permissions granted",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    OutlinedButton(
                        onClick = { AuthSession.signOut() },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) {
                        Text("Sign out")
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "About", style = MaterialTheme.typography.titleMedium)
                    Text(text = "Pepsi Depo — version 1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "More settings coming soon.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
