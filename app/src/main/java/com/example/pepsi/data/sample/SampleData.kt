package com.example.pepsi.data.sample

import com.example.pepsi.data.model.Delivery
import com.example.pepsi.data.model.DeliveryItem
import com.example.pepsi.data.model.DeliveryStatus
import com.example.pepsi.data.model.DepoAttendantProfile
import com.example.pepsi.data.model.Product
import com.example.pepsi.data.model.SaleRecord
import com.example.pepsi.data.model.StockItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Static placeholder data so screens are fully navigable before a backend exists.
 * Replace with a real repository once the API/data layer lands.
 */
object SampleData {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    private fun daysAgo(days: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -days)
        return dateFormat.format(calendar.time)
    }

    val products = listOf(
        Product("Pepsi 500ml", unitPrice = 1500),
        Product("Mirinda 500ml", unitPrice = 1500),
        Product("7UP 500ml", unitPrice = 1500),
        Product("Pepsi 1.5L", unitPrice = 3500),
        Product("Mountain Dew 500ml", unitPrice = 1800),
    )

    val initialStock = listOf(
        StockItem("Pepsi 500ml", 1200),
        StockItem("Mirinda 500ml", 800),
        StockItem("7UP 500ml", 650),
        StockItem("Pepsi 1.5L", 400),
        StockItem("Mountain Dew 500ml", 300),
    )

    val initialSales = listOf(
        SaleRecord("Pepsi 500ml", 30, 30 * 1500, daysAgo(0)),
        SaleRecord("Mirinda 500ml", 18, 18 * 1500, daysAgo(0)),
        SaleRecord("7UP 500ml", 12, 12 * 1500, daysAgo(1)),
        SaleRecord("Pepsi 500ml", 25, 25 * 1500, daysAgo(2)),
        SaleRecord("Pepsi 1.5L", 10, 10 * 3500, daysAgo(4)),
        SaleRecord("Mountain Dew 500ml", 15, 15 * 1800, daysAgo(6)),
        SaleRecord("Mirinda 500ml", 40, 40 * 1500, daysAgo(15)),
        SaleRecord("Pepsi 500ml", 60, 60 * 1500, daysAgo(20)),
        SaleRecord("7UP 500ml", 22, 22 * 1500, daysAgo(28)),
        SaleRecord("Pepsi 1.5L", 35, 35 * 3500, daysAgo(90)),
        SaleRecord("Mountain Dew 500ml", 50, 50 * 1800, daysAgo(200)),
        SaleRecord("Pepsi 500ml", 80, 80 * 1500, daysAgo(300)),
    )

    val initialDeliveries = listOf(
        Delivery(
            id = "DEL-1001",
            factoryManagerName = "Diana Nakato",
            date = daysAgo(0),
            items = listOf(
                DeliveryItem("Mirinda 500ml", 500),
                DeliveryItem("Pepsi 500ml", 300),
            ),
            status = DeliveryStatus.Pending,
        ),
        Delivery(
            id = "DEL-1002",
            factoryManagerName = "Diana Nakato",
            date = daysAgo(1),
            items = listOf(
                DeliveryItem("7UP 500ml", 200),
                DeliveryItem("Mountain Dew 500ml", 150),
            ),
            status = DeliveryStatus.Pending,
        ),
    )

    val profile = DepoAttendantProfile(
        name = "Sarah Namutebi",
        role = "Depo Attendant",
        email = "sarah.namutebi@pepsidepo.com",
        contact = "+256 700 111 222",
        gender = "Female",
        password = "Depo@2026",
        depoName = "Depo Kampala Central",
    )

    fun todayDateString(): String = dateFormat.format(Date())
}
