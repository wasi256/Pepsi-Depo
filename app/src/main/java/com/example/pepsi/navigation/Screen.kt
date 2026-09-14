package com.example.pepsi.navigation

sealed class Screen(val route: String, val label: String) {
    data object Overview : Screen("overview", "Overview")
    data object SalesHistory : Screen("sales_history", "Sales History")
    data object ReceiveProducts : Screen("receive_products", "Receive Products")
    data object SellProducts : Screen("sell_products", "Sell Products")
    data object ViewStocks : Screen("view_stocks", "View Stocks")
    data object RecordProduction : Screen("record_production", "Record Production")
    data object RecordSupply : Screen("record_supply", "Record Supply")
    data object ProductionRecords : Screen("production_records", "View Production Records")
    data object SupplyRecords : Screen("supply_records", "View Supply Records")
    data object Profile : Screen("profile", "Profile")
}

val drawerScreens: List<Screen> = listOf(
    Screen.Overview,
    Screen.SalesHistory,
    Screen.ReceiveProducts,
    Screen.SellProducts,
    Screen.ViewStocks,
    Screen.RecordProduction,
    Screen.RecordSupply,
    Screen.ProductionRecords,
    Screen.SupplyRecords,
)
