package com.example.pepsi.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pepsi.ui.depos.DeposScreen
import com.example.pepsi.ui.overview.OverviewScreen
import com.example.pepsi.ui.products.ProductsScreen
import com.example.pepsi.ui.profile.ProfileScreen
import com.example.pepsi.ui.sales.SalesScreen
import com.example.pepsi.ui.workers.WorkersScreen
import kotlinx.coroutines.launch

@Composable
fun PepsiNavHost(navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    fun navigateTo(screen: Screen) {
        scope.launch { drawerState.close() }
        navController.navigate(screen.route) {
            popUpTo(Screen.Overview.route) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(currentRoute = currentRoute, onItemClick = ::navigateTo)
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Overview.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Screen.Overview.route) {
                OverviewScreen(
                    onMenuClick = ::openDrawer,
                    onViewCurrentStock = { navigateTo(Screen.Depos) },
                    onViewHistory = { navigateTo(Screen.Products) },
                    onAdminClick = { navController.navigate(Screen.Profile.route) },
                )
            }
            composable(Screen.Sales.route) {
                SalesScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.Products.route) {
                ProductsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.Depos.route) {
                DeposScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.Workers.route) {
                WorkersScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { navigateTo(Screen.Overview) },
                )
            }
        }
    }
}
