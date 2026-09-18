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

    data object AdminOverview : Screen("admin_overview", "Overview")
    data object AdminUsers : Screen("admin_users", "Users")
    data object AdminRegisterUser : Screen("admin_users/register", "Register User")
    data object AdminRegisterRole : Screen("admin_users/roles/register", "Register Role")
    data object AdminDepos : Screen("admin_depos", "Depo")
    data object AdminRegisterDepo : Screen("admin_depos/register", "Register Depo")
    data object AdminProducts : Screen("admin_products", "Products")
    data object AdminRegisterProduct : Screen("admin_products/register", "Register Product")
    data object AdminRegisterQuantity : Screen("admin_products/quantities/register", "Register Quantity")
    data object AdminRegisterPrice : Screen("admin_products/prices/register", "Register Price")
    data object AdminAuditLogs : Screen("admin_audit_logs", "Audit Logs")
    data object AdminSystemHealth : Screen("admin_system_health", "System Health")
    data object AdminSettings : Screen("admin_settings", "Settings")
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

val adminDrawerScreens: List<Screen> = listOf(
    Screen.AdminOverview,
    Screen.AdminUsers,
    Screen.AdminDepos,
    Screen.AdminProducts,
    Screen.AdminAuditLogs,
    Screen.AdminSystemHealth,
    Screen.AdminSettings,
)
