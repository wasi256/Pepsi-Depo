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

// --- Factory module models (see FactorySampleData) ---

data class Sale(
    val productName: String,
    val quantity: Int,
    val depoName: String,
    val date: String,
)

data class HourlySales(
    val hourLabel: String,
    val unitsSold: Int,
)

data class FactoryProduct(
    val name: String,
    val quantity: Int,
    val manufacturingDate: String,
    val expiryDate: String,
)

data class ProductionDistribution(
    val productName: String,
    val quantity: Int,
    val date: String,
    val depoName: String,
)

data class ProductionEntry(
    val productName: String,
    val quantity: Int,
    val manufacturingDate: String,
    val expiryDate: String,
)

data class Depo(
    val name: String,
    val location: String,
    val attendantName: String,
)

data class StockLevel(
    val productName: String,
    val stockLevel: Int,
)

enum class WorkerRole {
    SystemAdmin,
    Factory,
    DepoAttendant,
}

data class Worker(
    val name: String,
    val telephone: String,
    val role: WorkerRole,
)

enum class TrendPeriod(val label: String) {
    Daily("Daily"),
    Weekly("Weekly"),
    Monthly("Monthly"),
    Yearly("Yearly"),
}

data class TrendPoint(
    val label: String,
    val value: Int,
)

data class ManagerProfile(
    val name: String,
    val telephone: String,
    val email: String,
    val password: String,
    val gender: String,
    val role: String = "Manager",
)
