package com.example.pepsi.systemadmin.audit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.pepsi.common.data.AdminDataStore
import com.example.pepsi.common.model.AuditLog
import com.example.pepsi.ui.components.PepsiTopBar
import java.text.SimpleDateFormat
import java.util.Locale

private val timestampFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())

@Composable
fun AuditLogsScreen(onMenuClick: () -> Unit) {
    val logs = AdminDataStore.auditLogs

    Scaffold(
        topBar = { PepsiTopBar(title = "Audit Logs", onMenuClick = onMenuClick) },
    ) { padding ->
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().padding(padding),
    ) {
        item {
            Text(
                text = "${logs.size} audit log entries",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 4.dp),
            )
        }
        items(logs, key = { it.id }) { log ->
            AuditLogRow(log)
        }
    }
    }
}

@Composable
private fun AuditLogRow(log: AuditLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = log.action, style = MaterialTheme.typography.titleMedium)
            Text(text = log.details, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "${log.actor} • ${timestampFormat.format(log.timestamp)}",
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
