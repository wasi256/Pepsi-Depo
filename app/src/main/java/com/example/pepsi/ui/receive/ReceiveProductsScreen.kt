package com.example.pepsi.ui.receive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.Delivery
import com.example.pepsi.data.model.DeliveryStatus
import com.example.pepsi.data.state.DepoState
import com.example.pepsi.ui.components.PepsiTopBar

@Composable
fun ReceiveProductsScreen(onMenuClick: () -> Unit) {
    val pending = DepoState.deliveries.filter { it.status == DeliveryStatus.Pending }
    val responded = DepoState.deliveries.filter { it.status != DeliveryStatus.Pending }

    Scaffold(
        topBar = { PepsiTopBar(title = "Receive Products", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Text(
                    text = "Pending Deliveries",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (pending.isEmpty()) {
                item {
                    Text(
                        text = "No pending deliveries from the factory manager.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(pending, key = { it.id }) { delivery ->
                PendingDeliveryCard(delivery)
            }

            if (responded.isNotEmpty()) {
                item { HorizontalDivider() }
                item {
                    Text(
                        text = "Recently Responded",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                items(responded, key = { it.id }) { delivery ->
                    RespondedDeliveryCard(delivery)
                }
            }
        }
    }
}

private enum class ReceiveAction { None, Confirming, Rejecting }

@Composable
private fun PendingDeliveryCard(delivery: Delivery) {
    var action by remember(delivery.id) { mutableStateOf(ReceiveAction.None) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "From: ${delivery.factoryManagerName}", fontWeight = FontWeight.Bold)
            Text(text = "Date: ${delivery.date}", style = MaterialTheme.typography.bodyMedium)
            delivery.items.forEach { item ->
                Text(
                    text = "${item.productName} — ${item.quantitySent} bottles",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (action == ReceiveAction.None) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { action = ReceiveAction.Confirming }, modifier = Modifier.weight(1f)) {
                        Text("Confirm")
                    }
                    OutlinedButton(onClick = { action = ReceiveAction.Rejecting }, modifier = Modifier.weight(1f)) {
                        Text("Reject")
                    }
                }
            }

            if (action == ReceiveAction.Confirming) {
                ConfirmSection(
                    onCancel = { action = ReceiveAction.None },
                    onSend = { message ->
                        DepoState.confirmDelivery(delivery, message)
                        action = ReceiveAction.None
                    },
                )
            }

            if (action == ReceiveAction.Rejecting) {
                RejectSection(
                    delivery = delivery,
                    onCancel = { action = ReceiveAction.None },
                    onSend = { receivedQuantities, message ->
                        DepoState.rejectDelivery(delivery, receivedQuantities, message)
                        action = ReceiveAction.None
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfirmSection(
    onCancel: () -> Unit,
    onSend: (String) -> Unit,
) {
    var message by remember { mutableStateOf("All items received in good condition.") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(text = "Feedback to Factory Manager", fontWeight = FontWeight.Bold)
        OutlinedTextField(
            value = message,
            onValueChange = { message = it },
            label = { Text("Message") },
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = { onSend(message) }, modifier = Modifier.weight(1f)) {
                Text("Send")
            }
            OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun RejectSection(
    delivery: Delivery,
    onCancel: () -> Unit,
    onSend: (Map<String, Int>, String) -> Unit,
) {
    val receivedInputs = remember(delivery.id) {
        mutableStateOf(delivery.items.associate { it.productName to "" })
    }
    var calculated by remember(delivery.id) { mutableStateOf(false) }
    var message by remember(delivery.id) { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        HorizontalDivider()
        Text(text = "Quantity Actually Received", fontWeight = FontWeight.Bold)
        delivery.items.forEach { item ->
            OutlinedTextField(
                value = receivedInputs.value[item.productName] ?: "",
                onValueChange = { value ->
                    receivedInputs.value = receivedInputs.value.toMutableMap().apply {
                        this[item.productName] = value.filter(Char::isDigit)
                    }
                    calculated = false
                },
                label = { Text("${item.productName} (sent ${item.quantitySent})") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Button(
            onClick = {
                val lines = delivery.items.map { item ->
                    val received = receivedInputs.value[item.productName]?.toIntOrNull() ?: 0
                    val lost = (item.quantitySent - received).coerceAtLeast(0)
                    "${item.productName}: sent ${item.quantitySent}, received $received, lost $lost"
                }
                message = "Discrepancy report:\n" + lines.joinToString("\n")
                calculated = true
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Calculate Loss")
        }

        if (calculated) {
            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Message to Factory Manager") },
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        val receivedQuantities = receivedInputs.value.mapValues { it.value.toIntOrNull() ?: 0 }
                        onSend(receivedQuantities, message)
                    },
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Send")
                }
                OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
            }
        } else {
            OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth()) {
                Text("Cancel")
            }
        }
    }
}

@Composable
private fun RespondedDeliveryCard(delivery: Delivery) {
    val statusLabel = if (delivery.status == DeliveryStatus.Confirmed) "Confirmed" else "Rejected"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = "${delivery.id} — $statusLabel", fontWeight = FontWeight.Bold)
            Text(text = "From: ${delivery.factoryManagerName}", style = MaterialTheme.typography.bodyMedium)
            delivery.responseMessage?.let {
                Text(text = it, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}
