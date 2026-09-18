package com.example.pepsi.systemadmin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                    Text(
                        text = "Sign-in isn't wired up yet. Once authentication ships, your account details will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
