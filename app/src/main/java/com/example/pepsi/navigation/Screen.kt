package com.example.pepsi.navigation

sealed class Screen(val route: String, val label: String) {
    data object Overview : Screen("overview", "Overview")
    data object SalesHistory : Screen("sales_history", "Sales History")
    data object ReceiveProducts : Screen("receive_products", "Receive Products")
    data object SellProducts : Screen("sell_products", "Sell Products")
    data object ViewStocks : Screen("view_stocks", "View Stocks")
    data object Profile : Screen("profile", "Profile")
}

val drawerScreens: List<Screen> = listOf(
    Screen.Overview,
    Screen.SalesHistory,
    Screen.ReceiveProducts,
    Screen.SellProducts,
    Screen.ViewStocks,
)
