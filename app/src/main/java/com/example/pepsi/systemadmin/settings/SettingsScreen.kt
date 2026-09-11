package com.example.pepsi.systemadmin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.Role

@Composable
fun SettingsScreen() {
    val admin = AdminDataStore.users.firstOrNull { it.role == Role.SYSTEM_ADMIN }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Account", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "Signed in as ${admin?.fullName ?: "System Admin"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(text = admin?.companyEmail ?: "", style = MaterialTheme.typography.bodyMedium)
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
