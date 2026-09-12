package com.example.pepsi.data.model

data class Product(
    val name: String,
    val unitPrice: Int,
)

data class StockItem(
    val productName: String,
    val quantity: Int,
)

data class SaleRecord(
    val productName: String,
    val quantity: Int,
    val amount: Int,
    val date: String,
)

data class DeliveryItem(
    val productName: String,
    val quantitySent: Int,
)

enum class DeliveryStatus {
    Pending,
    Confirmed,
    Rejected,
}

data class Delivery(
    val id: String,
    val factoryManagerName: String,
    val date: String,
    val items: List<DeliveryItem>,
    val status: DeliveryStatus,
    val responseMessage: String? = null,
)

enum class RangePeriod(val label: String) {
    Today("Today"),
    Weekly("Weekly"),
    Monthly("Monthly"),
    Yearly("Yearly"),
}

data class ChartEntry(
    val label: String,
    val value: Int,
)

data class DepoAttendantProfile(
    val name: String,
    val role: String,
    val email: String,
    val contact: String,
    val gender: String,
    val password: String,
    val depoName: String,
)
