package com.example.pepsi.systemadmin.depos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddBusiness
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.Depo

@Composable
fun DeposScreen(onRegisterDepo: () -> Unit) {
    val depos = AdminDataStore.depos

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Register Depo") },
                icon = { Icon(Icons.Filled.AddBusiness, contentDescription = null) },
                onClick = onRegisterDepo,
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
                    Text(
                        text = "${depos.size} registered depos",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(bottom = 4.dp),
                    )
                }
                items(depos, key = { it.id }) { depo ->
                    DepoRow(depo)
                }
            }
        }
    }
}

@Composable
private fun DepoRow(depo: Depo) {
    val attendant = AdminDataStore.userById(depo.depoAttendantId)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = depo.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "ID: ${depo.id}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Location: ${depo.location}", style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "Depo Attendant: ${attendant?.fullName ?: "Unassigned"}",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
