package com.example.pepsi.systemadmin.users

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.User
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun UsersScreen(onMenuClick: () -> Unit, onRegisterUser: () -> Unit) {
    val users = AdminDataStore.users
    var query by rememberSaveable { mutableStateOf("") }
    val filteredUsers = if (query.isBlank()) {
        users
    } else {
        users.filter { user ->
            listOf(user.fullName, user.id, user.tel, user.companyEmail, user.role.label)
                .any { it.contains(query, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = { PepsiTopBar(title = "Users", onMenuClick = onMenuClick) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Register User") },
                icon = { Icon(Icons.Filled.PersonAdd, contentDescription = null) },
                onClick = onRegisterUser,
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
            )
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search by name, ID, tel or role") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                item {
                    Text(
                        text = "${filteredUsers.size} of ${users.size} registered users",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
                    )
                }
                items(filteredUsers, key = { it.id }) { user ->
                    UserRow(user)
                }
            }
        }
    }
}

@Composable
private fun UserRow(user: User) {
    var passwordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(text = user.fullName, style = MaterialTheme.typography.titleMedium)
                AssistChip(onClick = {}, label = { Text(user.role.label) })
            }
            Text(text = "ID: ${user.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Tel: ${user.tel}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Email: ${user.companyEmail}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Gender: ${user.gender.name.lowercase().replaceFirstChar { it.uppercase() }}",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text(
                    text = "Password: " + if (passwordVisible) user.password else "•".repeat(user.password.length),
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            }
        }
    }
}
