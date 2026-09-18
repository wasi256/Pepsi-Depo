package com.example.pepsi.data.model

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
