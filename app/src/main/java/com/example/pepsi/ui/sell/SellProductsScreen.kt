package com.example.pepsi.ui.sell

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pepsi.auth.AuthSession
import com.example.pepsi.data.model.ChartEntry
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.CurrentStockResponse
import com.example.pepsi.network.model.SaleCreateRequest
import com.example.pepsi.network.model.SaleResponse
import com.example.pepsi.network.readErrorMessage
import com.example.pepsi.ui.components.EntryBarChart
import com.example.pepsi.ui.components.ListStatus
import com.example.pepsi.ui.components.OnResume
import com.example.pepsi.ui.components.PepsiTopBar
import com.example.pepsi.ui.components.QuantityStepperField
import com.example.pepsi.ui.components.RangePeriodSelector
import com.example.pepsi.ui.components.dateFrom
import com.example.pepsi.ui.components.formatMoney
import com.example.pepsi.ui.components.todayString
import kotlinx.coroutines.launch

private const val CHART_PAGE_SIZE = 100
private const val CHART_MAX_PAGES = 5

@Composable
fun SellProductsScreen(onMenuClick: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var stock by remember { mutableStateOf<List<CurrentStockResponse>>(emptyList()) }
    var stockLoading by remember { mutableStateOf(true) }
    var stockError by remember { mutableStateOf<String?>(null) }
    // Unit prices by quantity_id; empty when the role can't read prices, then the amount is typed in.
    var prices by remember { mutableStateOf<Map<Int, Double>>(emptyMap()) }

    var quantities by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var manualAmounts by remember { mutableStateOf<Map<Int, String>>(emptyMap()) }
    var isSubmitting by remember { mutableStateOf(false) }
    var confirmation by remember { mutableStateOf<String?>(null) }

    var chartPeriod by remember { mutableStateOf(RangePeriod.Today) }
    var chartSales by remember { mutableStateOf<List<SaleResponse>>(emptyList()) }
    var chartLoading by remember { mutableStateOf(true) }
    var chartError by remember { mutableStateOf<String?>(null) }
    var chartTick by remember { mutableIntStateOf(0) }

    suspend fun loadStock() {
        stockLoading = true
        stockError = null
        try {
            val response = RetrofitClient.depotApi.listCurrentStock(AuthSession.depotId)
            if (response.isSuccessful) stock = response.body().orEmpty() else stockError = response.readErrorMessage()
        } catch (e: Exception) {
            stockError = "Network error: ${e.message}"
        }
        stockLoading = false
    }

    OnResume(key = AuthSession.depotId) {
        loadStock()
        prices = runCatching {
            RetrofitClient.adminApi.listPrices(page = 1, pageSize = 100).takeIf { it.isSuccessful }?.body()
                ?.items?.associate { it.quantity_id to it.amount }
        }.getOrNull().orEmpty()
    }

    LaunchedEffect(chartPeriod, chartTick, AuthSession.depotId) {
        chartLoading = true
        chartError = null
        try {
            val collected = mutableListOf<SaleResponse>()
            var pageNumber = 1
            while (pageNumber <= CHART_MAX_PAGES) {
                val response = RetrofitClient.depotApi.listSales(
                    depotId = AuthSession.depotId,
                    dateFrom = chartPeriod.dateFrom(),
                    dateTo = todayString(),
                    page = pageNumber,
                    pageSize = CHART_PAGE_SIZE,
                )
                val body = response.body()
                if (!response.isSuccessful || body == null) {
                    chartError = response.readErrorMessage()
                    break
                }
                collected += body.items
                if (pageNumber >= body.total_pages) break
                pageNumber++
            }
            chartSales = collected
        } catch (e: Exception) {
            chartError = "Network error: ${e.message}"
        }
        chartLoading = false
    }

    fun amountFor(item: CurrentStockResponse, quantity: Int): Double? {
        val unitPrice = prices[item.quantity_id]
        return if (unitPrice != null) unitPrice * quantity else manualAmounts[item.id]?.toDoubleOrNull()
    }

    val chosen = stock.mapNotNull { item ->
        val quantity = quantities[item.id]?.toIntOrNull() ?: 0
        if (quantity > 0) item to quantity else null
    }
    val overStock = chosen.any { (item, quantity) -> quantity > item.current_amount }
    val missingAmount = chosen.any { (item, quantity) -> amountFor(item, quantity) == null }
    val personnelId = AuthSession.user?.personnel_id

    fun submit() {
        val sellerId = personnelId ?: return
        val requests = chosen.map { (item, quantity) ->
            SaleCreateRequest(
                depot_id = item.depot_id ?: AuthSession.depotId ?: return,
                product_id = item.product_id,
                quantity_id = item.quantity_id,
                quantity_sold = quantity,
                amount_sold = amountFor(item, quantity) ?: return,
                sold_by_id = sellerId,
            )
        }
        scope.launch {
            isSubmitting = true
            try {
                val response = RetrofitClient.depotApi.recordSales(requests)
                val created = response.body()
                if (response.isSuccessful && created != null) {
                    confirmation = "Sold ${created.sumOf { it.quantity_sold }} units for ${formatMoney(created.sumOf { it.amount_sold })}."
                    quantities = emptyMap()
                    manualAmounts = emptyMap()
                    chartTick++
                    loadStock()
                } else {
                    Toast.makeText(context, response.readErrorMessage(), Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            isSubmitting = false
        }
    }

    val perProductEntries = chartSales
        .groupBy { it.product_name ?: "Product ${it.product_id}" }
        .map { (name, sales) -> ChartEntry(label = name.substringBefore(" "), value = sales.sumOf { it.quantity_sold }) }
        .sortedByDescending { it.value }
    val totalUnits = perProductEntries.sumOf { it.value }

    Scaffold(
        topBar = { PepsiTopBar(title = "Sell Products", onMenuClick = onMenuClick) },
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
                    text = "Record a Sale",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                ListStatus(
                    isLoading = stockLoading,
                    error = stockError,
                    isEmpty = !stockLoading && stockError == null && stock.isEmpty(),
                    emptyText = "There is no stock to sell yet. Confirm a delivery under Receive Products first.",
                )
            }

            items(stock, key = { it.id }) { item ->
                val quantity = quantities[item.id].orEmpty()
                val quantityValue = quantity.toIntOrNull() ?: 0
                ProductQuantityRow(
                    item = item,
                    quantity = quantity,
                    onQuantityChange = { value ->
                        val digits = value.filter(Char::isDigit)
                        quantities = quantities + (item.id to digits)
                    },
                    unitPrice = prices[item.quantity_id],
                    manualAmount = manualAmounts[item.id].orEmpty(),
                    onManualAmountChange = { value ->
                        manualAmounts = manualAmounts + (item.id to value.filter { it.isDigit() || it == '.' })
                    },
                    overStock = quantityValue > item.current_amount,
                )
            }

            item {
                if (personnelId == null) {
                    Text(
                        text = "Your account isn't linked to a personnel record, so sales can't be attributed to you.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                Button(
                    onClick = ::submit,
                    enabled = chosen.isNotEmpty() && !overStock && !missingAmount && !isSubmitting && personnelId != null,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    val total = chosen.sumOf { (item, quantity) -> amountFor(item, quantity) ?: 0.0 }
                    Text(
                        when {
                            isSubmitting -> "Recording…"
                            chosen.isEmpty() -> "Confirm Sold"
                            else -> "Confirm Sold · ${formatMoney(total)}"
                        },
                    )
                }
            }

            confirmation?.let { message ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    ) {
                        Text(text = message, modifier = Modifier.padding(12.dp))
                    }
                }
            }

            item { HorizontalDivider() }

            item {
                Text(
                    text = "Sales Rate by Product",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            item {
                RangePeriodSelector(selected = chartPeriod, onSelect = { chartPeriod = it })
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total units sold (${chartPeriod.label.lowercase()}): $totalUnits",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        ListStatus(
                            isLoading = chartLoading,
                            error = chartError,
                            isEmpty = !chartLoading && chartError == null && perProductEntries.isEmpty(),
                            emptyText = "No sales in this period.",
                        )
                        if (perProductEntries.isNotEmpty()) {
                            EntryBarChart(data = perProductEntries, modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductQuantityRow(
    item: CurrentStockResponse,
    quantity: String,
    onQuantityChange: (String) -> Unit,
    unitPrice: Double?,
    manualAmount: String,
    onManualAmountChange: (String) -> Unit,
    overStock: Boolean,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "${item.product_name ?: "Product #${item.product_id}"} ${item.quantity_value.orEmpty()}".trim(),
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = buildString {
                    append("Available in stock: ${item.current_amount}")
                    if (unitPrice != null) append(" · ${formatMoney(unitPrice)} each")
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            QuantityStepperField(
                quantity = quantity,
                onQuantityChange = onQuantityChange,
                label = "Quantity to Sell",
            )
            if (overStock) {
                Text(
                    text = "Only ${item.current_amount} in stock.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
            val quantityValue = quantity.toIntOrNull() ?: 0
            if (quantityValue > 0) {
                if (unitPrice != null) {
                    Text(
                        text = "Amount: ${formatMoney(unitPrice * quantityValue)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                } else {
                    OutlinedTextField(
                        value = manualAmount,
                        onValueChange = onManualAmountChange,
                        label = { Text("Amount sold (UGX)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}
