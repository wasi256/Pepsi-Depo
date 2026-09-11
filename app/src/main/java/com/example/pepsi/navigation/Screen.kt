package com.example.pepsi.navigation

sealed class Screen(val route: String, val title: String) {
    data object Overview : Screen("overview", "Overview")
    data object Users : Screen("users", "Users")
    data object RegisterUser : Screen("users/register", "Register User")
    data object Depos : Screen("depos", "Depo")
    data object RegisterDepo : Screen("depos/register", "Register Depo")
    data object AuditLogs : Screen("audit_logs", "Audit Logs")
    data object SystemHealth : Screen("system_health", "System Health")
    data object Settings : Screen("settings", "Settings")

    companion object {
        /** Destinations reachable directly from the hamburger menu. */
        val topLevel = listOf(Overview, Users, Depos, AuditLogs, SystemHealth, Settings)
    }
}
