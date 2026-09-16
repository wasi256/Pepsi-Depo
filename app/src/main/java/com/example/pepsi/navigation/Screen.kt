package com.example.pepsi.navigation

sealed class Screen(val route: String, val title: String) {
    data object Overview : Screen("overview", "Overview")
    data object Users : Screen("users", "Users")
    data object RegisterUser : Screen("users/register", "Register User")
    data object Depos : Screen("depos", "Depo")
    data object RegisterDepo : Screen("depos/register", "Register Depo")
    data object Products : Screen("products", "Products")
    data object RegisterProduct : Screen("products/register", "Register Product")
    data object RegisterQuantity : Screen("products/quantities/register", "Register Quantity")
    data object RegisterPrice : Screen("products/prices/register", "Register Price")
    data object AuditLogs : Screen("audit_logs", "Audit Logs")
    data object SystemHealth : Screen("system_health", "System Health")
    data object Settings : Screen("settings", "Settings")

    companion object {
        /** Destinations reachable directly from the hamburger menu. */
        val topLevel = listOf(Overview, Users, Depos, Products, AuditLogs, SystemHealth, Settings)
    }
}
