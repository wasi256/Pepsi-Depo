package com.example.pepsi.navigation

sealed class Screen(val route: String, val label: String) {
    data object DepotOverview : Screen("depot_overview", "Overview")
    data object SalesHistory : Screen("sales_history", "Sales History")
    data object ReceiveProducts : Screen("receive_products", "Receive Products")
    data object SellProducts : Screen("sell_products", "Sell Products")
    data object ViewStocks : Screen("view_stocks", "View Stocks")

    data object FactoryOverview : Screen("factory_overview", "Overview")
    data object FactorySales : Screen("factory_sales", "Sales")
    data object FactoryProducts : Screen("factory_products", "Products")
    data object FactoryDepos : Screen("factory_depos", "Depos")
    data object FactoryWorkers : Screen("factory_workers", "Workers")

    data object Profile : Screen("profile", "Profile")
    data object FactoryProfile : Screen("factory_profile", "Factory Profile")
}

val depotDrawerScreens: List<Screen> = listOf(
    Screen.DepotOverview,
    Screen.SalesHistory,
    Screen.ReceiveProducts,
    Screen.SellProducts,
    Screen.ViewStocks,
)

val factoryDrawerScreens: List<Screen> = listOf(
    Screen.FactoryOverview,
    Screen.FactorySales,
    Screen.FactoryProducts,
    Screen.FactoryDepos,
    Screen.FactoryWorkers,
)
