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
import com.example.pepsi.ui.overview.FactoryOverviewScreen
import com.example.pepsi.ui.overview.OverviewScreen
import com.example.pepsi.ui.products.ProductsScreen
import com.example.pepsi.ui.profile.FactoryProfileScreen
import com.example.pepsi.ui.profile.ProfileScreen
import com.example.pepsi.ui.receive.ReceiveProductsScreen
import com.example.pepsi.ui.sales.SalesScreen
import com.example.pepsi.ui.saleshistory.SalesHistoryScreen
import com.example.pepsi.ui.sell.SellProductsScreen
import com.example.pepsi.ui.stocks.ViewStocksScreen
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
            popUpTo(Screen.DepotOverview.route) { inclusive = false }
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
            startDestination = Screen.DepotOverview.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Screen.DepotOverview.route) {
                OverviewScreen(
                    onMenuClick = ::openDrawer,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onViewStocks = { navigateTo(Screen.ViewStocks) },
                    onSellProducts = { navigateTo(Screen.SellProducts) },
                    onReceiveProducts = { navigateTo(Screen.ReceiveProducts) },
                )
            }
            composable(Screen.SalesHistory.route) {
                SalesHistoryScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.ReceiveProducts.route) {
                ReceiveProductsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.SellProducts.route) {
                SellProductsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.ViewStocks.route) {
                ViewStocksScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.FactoryOverview.route) {
                FactoryOverviewScreen(
                    onMenuClick = ::openDrawer,
                    onProfileClick = { navController.navigate(Screen.FactoryProfile.route) },
                    onViewProducts = { navigateTo(Screen.FactoryProducts) },
                    onViewDepos = { navigateTo(Screen.FactoryDepos) },
                )
            }
            composable(Screen.FactorySales.route) {
                SalesScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.FactoryProducts.route) {
                ProductsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.FactoryDepos.route) {
                DeposScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.FactoryWorkers.route) {
                WorkersScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            composable(Screen.FactoryProfile.route) {
                FactoryProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { navigateTo(Screen.FactoryOverview) },
                )
            }
        }
    }
}
