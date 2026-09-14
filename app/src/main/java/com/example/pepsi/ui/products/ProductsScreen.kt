package com.example.pepsi.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.pepsi.data.model.Product
import com.example.pepsi.data.model.ProductionDistribution
import com.example.pepsi.data.model.ProductionEntry
import com.example.pepsi.data.sample.SampleData
import com.example.pepsi.ui.components.PepsiTopBar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun ProductsScreen(onMenuClick: () -> Unit) {
    val productionEntries = remember { mutableStateListOf<ProductionEntry>() }
    val distributions = remember { mutableStateListOf(*SampleData.productionDistributions.toTypedArray()) }

    Scaffold(
        topBar = { PepsiTopBar(title = "Products", onMenuClick = onMenuClick) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                SectionHeader("Products")
            }
            item {
                ProductsRow(SampleData.products)
            }

            item {
                SectionHeader("Production Distribution")
            }
            item {
                DistributionsRow(distributions)
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                RecordProductionSection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    entries = productionEntries,
                    onRecord = { entry -> productionEntries.add(0, entry) },
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                CreateDeliverySection(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onCreateDelivery = { delivery -> distributions.add(0, delivery) },
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 16.dp),
    )
}

@Composable
private fun ProductsRow(products: List<Product>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        products.forEach { product ->
            Card(modifier = Modifier.width(200.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = product.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Quantity: ${product.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Manufactured: ${product.manufacturingDate}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        text = "Expires: ${product.expiryDate}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun DistributionsRow(distributions: List<ProductionDistribution>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        distributions.forEach { distribution ->
            Card(modifier = Modifier.width(200.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = distribution.productName, fontWeight = FontWeight.Bold)
                    Text(
                        text = "Quantity: ${distribution.quantity}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(text = "Date: ${distribution.date}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = "Depo: ${distribution.depoName}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

private const val EXPIRY_YEARS = 5

private fun formatDateMillis(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

private fun addYears(millis: Long, years: Int): Long {
    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendar.timeInMillis = millis
    calendar.add(Calendar.YEAR, years)
    return calendar.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RecordProductionSection(
    modifier: Modifier = Modifier,
    entries: List<ProductionEntry>,
    onRecord: (ProductionEntry) -> Unit,
) {
    val knownProductNames = remember { SampleData.products.map { it.name }.distinct() }

    var productName by remember { mutableStateOf("") }
    var productNameExpanded by remember { mutableStateOf(false) }
    var quantity by remember { mutableStateOf("") }
    var manufacturingDateMillis by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val manufacturingDateText = manufacturingDateMillis?.let { formatDateMillis(it) } ?: ""
    val expiryDateText = manufacturingDateMillis?.let { formatDateMillis(addYears(it, EXPIRY_YEARS)) } ?: ""

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Record Production",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        ExposedDropdownMenuBox(
            expanded = productNameExpanded,
            onExpandedChange = { productNameExpanded = it },
        ) {
            OutlinedTextField(
                value = productName,
                onValueChange = {
                    productName = it
                    productNameExpanded = true
                },
                label = { Text("Product Name") },
                singleLine = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productNameExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryEditable),
            )
            val suggestions = knownProductNames.filter {
                productName.isBlank() || it.contains(productName, ignoreCase = true)
            }
            if (suggestions.isNotEmpty()) {
                ExposedDropdownMenu(
                    expanded = productNameExpanded,
                    onDismissRequest = { productNameExpanded = false },
                ) {
                    suggestions.forEach { name ->
                        DropdownMenuItem(
                            text = { Text(name) },
                            onClick = {
                                productName = name
                                productNameExpanded = false
                            },
                        )
                    }
                }
            }
        }

        QuantityStepperField(
            quantity = quantity,
            onQuantityChange = { quantity = it },
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = manufacturingDateText,
                onValueChange = {},
                readOnly = true,
                label = { Text("Manufacturing Date") },
                trailingIcon = { Icon(Icons.Filled.DateRange, contentDescription = "Pick manufacturing date") },
                modifier = Modifier.fillMaxWidth(),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = { showDatePicker = true }),
            )
        }

        OutlinedTextField(
            value = expiryDateText,
            onValueChange = {},
            readOnly = true,
            enabled = false,
            label = { Text("Expiry Date (auto, +$EXPIRY_YEARS years)") },
            modifier = Modifier.fillMaxWidth(),
        )

        Button(
            onClick = {
                val qty = quantity.toIntOrNull() ?: return@Button
                val manufactureMillis = manufacturingDateMillis ?: return@Button
                if (productName.isBlank() || qty <= 0) return@Button
                onRecord(
                    ProductionEntry(
                        productName = productName.trim(),
                        quantity = qty,
                        manufacturingDate = formatDateMillis(manufactureMillis),
                        expiryDate = formatDateMillis(addYears(manufactureMillis, EXPIRY_YEARS)),
                    ),
                )
                productName = ""
                quantity = ""
                manufacturingDateMillis = null
            },
            enabled = productName.isNotBlank() &&
                manufacturingDateMillis != null &&
                quantity.toIntOrNull()?.let { it > 0 } == true,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Record Production")
        }

        if (entries.isNotEmpty()) {
            Text(
                text = "Recently Recorded",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp),
            )
            entries.forEach { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = entry.productName, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Quantity: ${entry.quantity}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Manufactured: ${entry.manufacturingDate}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        Text(
                            text = "Expires: ${entry.expiryDate}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = manufacturingDateMillis)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        manufacturingDateMillis = datePickerState.selectedDateMillis
                        showDatePicker = false
                    },
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            },
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateDeliverySection(
    modifier: Modifier = Modifier,
    onCreateDelivery: (ProductionDistribution) -> Unit,
) {
    var depoExpanded by remember { mutableStateOf(false) }
    var selectedDepo by remember { mutableStateOf<String?>(null) }
    var productExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<String?>(null) }
    var quantity by remember { mutableStateOf("") }
    var confirmation by remember { mutableStateOf<String?>(null) }

    val availableProducts = selectedDepo
        ?.let { SampleData.depoStock[it]?.map { stock -> stock.productName } }
        ?.takeIf { it.isNotEmpty() }
        ?: SampleData.products.map { it.name }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Create Delivery",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )

        ExposedDropdownMenuBox(
            expanded = depoExpanded,
            onExpandedChange = { depoExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedDepo ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Depo Name") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = depoExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = depoExpanded,
                onDismissRequest = { depoExpanded = false },
            ) {
                SampleData.depos.forEach { depo ->
                    DropdownMenuItem(
                        text = { Text(depo.name) },
                        onClick = {
                            selectedDepo = depo.name
                            selectedProduct = null
                            depoExpanded = false
                        },
                    )
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = productExpanded,
            onExpandedChange = { productExpanded = it },
        ) {
            OutlinedTextField(
                value = selectedProduct ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Product Name") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = productExpanded,
                onDismissRequest = { productExpanded = false },
            ) {
                availableProducts.forEach { productName ->
                    DropdownMenuItem(
                        text = { Text(productName) },
                        onClick = {
                            selectedProduct = productName
                            productExpanded = false
                        },
                    )
                }
            }
        }

        QuantityStepperField(
            quantity = quantity,
            onQuantityChange = { quantity = it },
        )

        Button(
            onClick = {
                val qty = quantity.toIntOrNull() ?: return@Button
                val depoName = selectedDepo ?: return@Button
                val productName = selectedProduct ?: return@Button
                if (qty <= 0) return@Button
                onCreateDelivery(
                    ProductionDistribution(
                        productName = productName,
                        quantity = qty,
                        date = todayDateString(),
                        depoName = depoName,
                    ),
                )
                confirmation = "Delivery note created for $productName to the $depoName attendant."
                selectedProduct = null
                quantity = ""
            },
            enabled = selectedDepo != null && selectedProduct != null && quantity.toIntOrNull()?.let { it > 0 } == true,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create Delivery Note")
        }

        confirmation?.let {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(text = it, modifier = Modifier.padding(12.dp))
            }
        }
    }
}

private fun todayDateString(): String =
    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

@Composable
private fun QuantityStepperField(
    quantity: String,
    onQuantityChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        OutlinedTextField(
            value = quantity,
            onValueChange = { onQuantityChange(it.filter(Char::isDigit)) },
            label = { Text("Quantity") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.weight(1f),
        )
        Column(modifier = Modifier.height(56.dp), verticalArrangement = Arrangement.SpaceBetween) {
            IconButton(
                onClick = { onQuantityChange(((quantity.toIntOrNull() ?: 0) + 1).toString()) },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Increase quantity")
            }
            IconButton(
                onClick = {
                    onQuantityChange(((quantity.toIntOrNull() ?: 0) - 1).coerceAtLeast(0).toString())
                },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "Decrease quantity")
            }
        }
    }
}
