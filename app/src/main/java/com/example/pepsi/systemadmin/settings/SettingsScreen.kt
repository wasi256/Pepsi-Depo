package com.example.pepsi.systemadmin.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.Role
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun SettingsScreen(onMenuClick: () -> Unit) {
    val admin = AdminDataStore.users.firstOrNull { it.role == Role.SYSTEM_ADMIN }
    var passwordVisible by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(text = "Account", style = MaterialTheme.typography.titleMedium)

                SettingsInfoRow(icon = Icons.Filled.Badge, label = admin?.fullName ?: "System Admin")
                SettingsInfoRow(icon = Icons.Filled.Email, label = admin?.companyEmail ?: "")

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Password,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                    Text(
                        text = if (passwordVisible) admin?.password.orEmpty() else "•".repeat(admin?.password?.length ?: 8),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showChangePassword = true },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Change Password")
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

    if (showChangePassword && admin != null) {
        ChangePasswordDialog(
            onDismiss = { showChangePassword = false },
            onSubmit = { current, new ->
                val success = AdminDataStore.changePassword(admin.id, current, new)
                Toast.makeText(
                    context,
                    if (success) "Password updated" else "Current password is incorrect",
                    Toast.LENGTH_SHORT,
                ).show()
                if (success) showChangePassword = false
            },
        )
    }
}

@Composable
private fun SettingsInfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 12.dp),
        )
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (current: String, new: String) -> Unit,
) {
    var current by rememberSaveable { mutableStateOf("") }
    var new by rememberSaveable { mutableStateOf("") }
    var confirm by rememberSaveable { mutableStateOf("") }
    val error = when {
        new.isNotEmpty() && new.length < 6 -> "New password must be at least 6 characters"
        confirm.isNotEmpty() && confirm != new -> "Passwords do not match"
        else -> null
    }
    val canSubmit = current.isNotBlank() && new.isNotBlank() && confirm.isNotBlank() && error == null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = new,
                    onValueChange = { new = it },
                    label = { Text("New password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm new password") },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Text(text = error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onSubmit(current, new) }, enabled = canSubmit) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
