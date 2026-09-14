package com.example.pepsi.navigation

sealed class Screen(val route: String, val label: String) {
    data object Overview : Screen("overview", "Overview")
    data object Sales : Screen("sales", "Sales")
    data object Products : Screen("products", "Products")
    data object Depos : Screen("depos", "Depos")
    data object Workers : Screen("workers", "Workers")
    data object Profile : Screen("profile", "Profile")
}

val drawerScreens: List<Screen> = listOf(
    Screen.Overview,
    Screen.Sales,
    Screen.Products,
    Screen.Depos,
    Screen.Workers,
)
