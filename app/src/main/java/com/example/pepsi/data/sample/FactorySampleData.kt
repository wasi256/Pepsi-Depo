package com.example.pepsi.data.sample

import com.example.pepsi.data.model.Depo
import com.example.pepsi.data.model.FactoryProduct
import com.example.pepsi.data.model.HourlySales
import com.example.pepsi.data.model.ManagerProfile
import com.example.pepsi.data.model.ProductionDistribution
import com.example.pepsi.data.model.Sale
import com.example.pepsi.data.model.StockLevel
import com.example.pepsi.data.model.TrendPeriod
import com.example.pepsi.data.model.TrendPoint
import com.example.pepsi.data.model.Worker
import com.example.pepsi.data.model.WorkerRole

/**
 * Static placeholder data for the factory module screens, separate from the
 * depot attendant module's SampleData to avoid colliding class/field shapes
 * (e.g. FactoryProduct vs the depot's Product).
 */
object FactorySampleData {

    val sales = listOf(
        Sale("Pepsi 500ml", 120, "Depo Kampala Central", "2026-09-08"),
        Sale("Mirinda 500ml", 80, "Depo Jinja", "2026-09-08"),
        Sale("7UP 500ml", 65, "Depo Mbarara", "2026-09-09"),
        Sale("Pepsi 1.5L", 40, "Depo Kampala Central", "2026-09-09"),
        Sale("Mountain Dew 500ml", 55, "Depo Gulu", "2026-09-10"),
        Sale("Pepsi 500ml", 95, "Depo Jinja", "2026-09-10"),
    )

    val hourlySales = listOf(
        HourlySales("8am", 30),
        HourlySales("10am", 55),
        HourlySales("12pm", 90),
        HourlySales("2pm", 75),
        HourlySales("4pm", 60),
        HourlySales("6pm", 100),
        HourlySales("8pm", 70),
        HourlySales("10pm", 40),
        HourlySales("12am", 15),
    )

    val products = listOf(
        FactoryProduct("Pepsi 500ml", 4500, "2026-08-01", "2027-02-01"),
        FactoryProduct("Mirinda 500ml", 3200, "2026-08-05", "2027-02-05"),
        FactoryProduct("7UP 500ml", 2800, "2026-08-10", "2027-02-10"),
        FactoryProduct("Pepsi 1.5L", 1500, "2026-08-12", "2027-02-12"),
        FactoryProduct("Mountain Dew 500ml", 1900, "2026-08-15", "2027-02-15"),
    )

    val productionDistributions = listOf(
        ProductionDistribution("Pepsi 500ml", 1000, "2026-09-08", "Depo Kampala Central"),
        ProductionDistribution("Mirinda 500ml", 700, "2026-09-08", "Depo Jinja"),
        ProductionDistribution("7UP 500ml", 600, "2026-09-09", "Depo Mbarara"),
        ProductionDistribution("Pepsi 1.5L", 450, "2026-09-09", "Depo Gulu"),
        ProductionDistribution("Mountain Dew 500ml", 500, "2026-09-10", "Depo Jinja"),
    )

    val depos = listOf(
        Depo("Depo Kampala Central", "Kampala", "Sarah Namutebi"),
        Depo("Depo Jinja", "Jinja", "Brian Okello"),
        Depo("Depo Mbarara", "Mbarara", "Grace Kyomugisha"),
        Depo("Depo Gulu", "Gulu", "Patrick Ochieng"),
    )

    val depoStock: Map<String, List<StockLevel>> = mapOf(
        "Depo Kampala Central" to listOf(
            StockLevel("Pepsi 500ml", 1200),
            StockLevel("Mirinda 500ml", 800),
            StockLevel("7UP 500ml", 650),
            StockLevel("Pepsi 1.5L", 400),
        ),
        "Depo Jinja" to listOf(
            StockLevel("Pepsi 500ml", 900),
            StockLevel("Mirinda 500ml", 700),
            StockLevel("Mountain Dew 500ml", 500),
        ),
        "Depo Mbarara" to listOf(
            StockLevel("7UP 500ml", 950),
            StockLevel("Pepsi 500ml", 600),
        ),
        "Depo Gulu" to listOf(
            StockLevel("Pepsi 1.5L", 350),
            StockLevel("Mountain Dew 500ml", 400),
            StockLevel("Pepsi 500ml", 700),
        ),
    )

    val workers = listOf(
        Worker("Sarah Namutebi", "+256 700 111 222", WorkerRole.DepoAttendant),
        Worker("Brian Okello", "+256 700 222 333", WorkerRole.DepoAttendant),
        Worker("Grace Kyomugisha", "+256 700 333 444", WorkerRole.DepoAttendant),
        Worker("Patrick Ochieng", "+256 700 444 555", WorkerRole.DepoAttendant),
        Worker("Daniel Mugisha", "+256 700 555 666", WorkerRole.Factory),
        Worker("Esther Nabirye", "+256 700 666 777", WorkerRole.Factory),
        Worker("Joseph Tumusiime", "+256 700 777 888", WorkerRole.SystemAdmin),
    )

    val totalDistributions = productionDistributions.sumOf { it.quantity }
    val totalSales = sales.sumOf { it.quantity }
    val totalDepos = depos.size
    val totalProductions = products.sumOf { it.quantity }

    val productsSoldTrends: Map<TrendPeriod, List<TrendPoint>> = mapOf(
        TrendPeriod.Daily to listOf(
            TrendPoint("Mon", 120), TrendPoint("Tue", 150), TrendPoint("Wed", 90),
            TrendPoint("Thu", 200), TrendPoint("Fri", 175), TrendPoint("Sat", 220),
            TrendPoint("Sun", 160),
        ),
        TrendPeriod.Weekly to listOf(
            TrendPoint("W1", 900), TrendPoint("W2", 1050), TrendPoint("W3", 980),
            TrendPoint("W4", 1200), TrendPoint("W5", 1100), TrendPoint("W6", 1300),
        ),
        TrendPeriod.Monthly to listOf(
            TrendPoint("Apr", 3200), TrendPoint("May", 3500), TrendPoint("Jun", 3100),
            TrendPoint("Jul", 3800), TrendPoint("Aug", 4000), TrendPoint("Sep", 4200),
        ),
        TrendPeriod.Yearly to listOf(
            TrendPoint("2021", 28000), TrendPoint("2022", 31000), TrendPoint("2023", 33500),
            TrendPoint("2024", 36000), TrendPoint("2025", 39500), TrendPoint("2026", 42000),
        ),
    )

    val productsManufacturedTrends: Map<TrendPeriod, List<TrendPoint>> = mapOf(
        TrendPeriod.Daily to listOf(
            TrendPoint("Mon", 300), TrendPoint("Tue", 320), TrendPoint("Wed", 280),
            TrendPoint("Thu", 350), TrendPoint("Fri", 400), TrendPoint("Sat", 380),
            TrendPoint("Sun", 360),
        ),
        TrendPeriod.Weekly to listOf(
            TrendPoint("W1", 2000), TrendPoint("W2", 2100), TrendPoint("W3", 1950),
            TrendPoint("W4", 2300), TrendPoint("W5", 2200), TrendPoint("W6", 2400),
        ),
        TrendPeriod.Monthly to listOf(
            TrendPoint("Apr", 8000), TrendPoint("May", 8200), TrendPoint("Jun", 7800),
            TrendPoint("Jul", 8600), TrendPoint("Aug", 9000), TrendPoint("Sep", 9200),
        ),
        TrendPeriod.Yearly to listOf(
            TrendPoint("2021", 60000), TrendPoint("2022", 64000), TrendPoint("2023", 67000),
            TrendPoint("2024", 70000), TrendPoint("2025", 75000), TrendPoint("2026", 79000),
        ),
    )

    val manager = ManagerProfile(
        name = "Diana Nakato",
        telephone = "+256 700 888 999",
        email = "diana.nakato@pepsidepo.com",
        password = "Pepsi@2026",
        gender = "Female",
    )

    val systemAdministrator = workers.first { it.role == WorkerRole.SystemAdmin }
}
