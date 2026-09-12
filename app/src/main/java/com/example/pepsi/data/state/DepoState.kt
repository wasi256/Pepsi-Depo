package com.example.pepsi.data.state

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pepsi.data.model.DepoAttendantProfile
import com.example.pepsi.data.model.Delivery
import com.example.pepsi.data.model.DeliveryStatus
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.data.model.SaleRecord
import com.example.pepsi.data.model.StockItem
import com.example.pepsi.data.sample.SampleData
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * In-memory, shared app state so Sell Products / Receive Products / View Stocks stay in sync
 * without a backend. Replace with a real repository once the API/data layer lands.
 */
object DepoState {

    val stock = mutableStateListOf(*SampleData.initialStock.toTypedArray())
    val sales = mutableStateListOf(*SampleData.initialSales.toTypedArray())
    val deliveries = mutableStateListOf(*SampleData.initialDeliveries.toTypedArray())
    val profile = mutableStateOf(SampleData.profile)

    fun stockQuantity(productName: String): Int =
        stock.firstOrNull { it.productName == productName }?.quantity ?: 0

    private fun adjustStock(productName: String, delta: Int) {
        val index = stock.indexOfFirst { it.productName == productName }
        if (index == -1) {
            if (delta > 0) stock.add(StockItem(productName, delta))
            return
        }
        val current = stock[index]
        stock[index] = current.copy(quantity = (current.quantity + delta).coerceAtLeast(0))
    }

    fun sellProducts(items: List<Pair<String, Int>>, unitPrices: Map<String, Int>): List<SaleRecord> {
        val today = SampleData.todayDateString()
        val created = mutableListOf<SaleRecord>()
        items.filter { it.second > 0 }.forEach { (productName, quantity) ->
            val price = unitPrices[productName] ?: 0
            val record = SaleRecord(
                productName = productName,
                quantity = quantity,
                amount = quantity * price,
                date = today,
            )
            adjustStock(productName, -quantity)
            sales.add(0, record)
            created.add(record)
        }
        return created
    }

    fun confirmDelivery(delivery: Delivery, message: String) {
        delivery.items.forEach { item -> adjustStock(item.productName, item.quantitySent) }
        replaceDelivery(delivery.copy(status = DeliveryStatus.Confirmed, responseMessage = message))
    }

    fun rejectDelivery(delivery: Delivery, receivedQuantities: Map<String, Int>, message: String) {
        delivery.items.forEach { item ->
            val received = receivedQuantities[item.productName] ?: 0
            adjustStock(item.productName, received)
        }
        replaceDelivery(delivery.copy(status = DeliveryStatus.Rejected, responseMessage = message))
    }

    private fun replaceDelivery(updated: Delivery) {
        val index = deliveries.indexOfFirst { it.id == updated.id }
        if (index != -1) deliveries[index] = updated
    }

    fun changePassword(currentPassword: String, newPassword: String): Boolean {
        if (currentPassword != profile.value.password) return false
        profile.value = profile.value.copy(password = newPassword)
        return true
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun matchesPeriod(dateString: String, period: RangePeriod): Boolean {
        val date = runCatching { dateFormat.parse(dateString) }.getOrNull() ?: return false
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)

        val target = Calendar.getInstance()
        target.time = date
        target.set(Calendar.HOUR_OF_DAY, 0)
        target.set(Calendar.MINUTE, 0)
        target.set(Calendar.SECOND, 0)
        target.set(Calendar.MILLISECOND, 0)

        val diffDays = TimeUnit.MILLISECONDS.toDays(today.timeInMillis - target.timeInMillis)
        return when (period) {
            RangePeriod.Today -> diffDays == 0L
            RangePeriod.Weekly -> diffDays in 0..6
            RangePeriod.Monthly -> diffDays in 0..29
            RangePeriod.Yearly -> diffDays in 0..364
        }
    }
}
