package com.example.pepsi.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.DepotResponse
import com.example.pepsi.network.model.PersonnelResponse

/** Personnel record and depot for the signed-in user; null until (or unless) the API returns them. */
private class ProfileDetails(val personnel: PersonnelResponse?, val depot: DepotResponse?, val unavailable: Boolean)

/**
 * The signed-in user's own information, shared by the Admin settings screen and the Factory and
 * Depot profile screens. Everything comes from the login session; contact details and depot are
 * looked up from the personnel record when the user's role is allowed to read it.
 */
@Composable
fun AccountProfileContent(modifier: Modifier = Modifier) {
    val user = AuthSession.user
    var details by remember { mutableStateOf<ProfileDetails?>(null) }
    var showPermissions by remember { mutableStateOf(false) }

    LaunchedEffect(user?.personnel_id) {
        val personnelId = user?.personnel_id
        if (personnelId == null) {
            details = ProfileDetails(null, null, unavailable = false)
            return@LaunchedEffect
        }
        val personnel = runCatching {
            RetrofitClient.adminApi.getPersonnel(personnelId).takeIf { it.isSuccessful }?.body()
        }.getOrNull()
        val depotId = personnel?.depot_id ?: AuthSession.depotId
        val depot = depotId?.let {
            runCatching { RetrofitClient.adminApi.getDepot(it).takeIf { r -> r.isSuccessful }?.body() }.getOrNull()
        }
        details = ProfileDetails(personnel, depot, unavailable = personnel == null)
    }

    val displayName = user?.personnel_name ?: user?.username ?: "Not signed in"
    val personnel = details?.personnel
    val depot = details?.depot

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(88.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = initialsOf(displayName),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                text = user?.role_name ?: "No role assigned",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        InfoCard(title = "Sign-in details") {
            InfoRow("Username", user?.username)
            InfoRow("Password", "••••••••")
            Text(
                text = "Passwords are never stored or shown. Ask a system administrator to reset yours.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            InfoRow("Role", user?.role_name)
        }

        InfoCard(title = "Personal details") {
            InfoRow("Personnel ID", user?.personnel_id?.toString())
            InfoRow("Full name", user?.personnel_name ?: personnel?.name)
            InfoRow("Email", personnel?.email)
            InfoRow("Phone", personnel?.contact)
            InfoRow("Gender", personnel?.gender)
            InfoRow("Depot", depot?.let { "${it.name} (${it.location})" } ?: AuthSession.depotId?.let { "Depot #$it" })
            if (details?.unavailable == true && user?.personnel_id != null) {
                Text(
                    text = "Contact details aren't available to your role.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        InfoCard(title = "Access") {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { showPermissions = !showPermissions },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${AuthSession.permissions.size} permissions granted",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (showPermissions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (showPermissions) "Hide permissions" else "Show permissions",
                )
            }
            if (showPermissions) {
                AuthSession.permissions.sorted().forEach {
                    Text(text = it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        OutlinedButton(onClick = { AuthSession.signOut() }, modifier = Modifier.fillMaxWidth()) {
            Text("Sign out")
        }
    }
}

/** Standalone profile page (back arrow) used by the Factory and Depot modules. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        AccountProfileContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        )
    }
}

private fun initialsOf(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts[0].take(2).uppercase()
        else -> "${parts.first().first()}${parts.last().first()}".uppercase()
    }
}

@Composable
private fun InfoCard(title: String, content: @Composable () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            HorizontalDivider()
            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
