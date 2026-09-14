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
import com.example.pepsi.ui.landing.RoleSelectionScreen
import com.example.pepsi.ui.overview.FactoryOverviewScreen
import com.example.pepsi.ui.overview.OverviewScreen
import com.example.pepsi.ui.production.ProductionRecordsScreen
import com.example.pepsi.ui.production.RecordProductionScreen
import com.example.pepsi.ui.profile.ProfileScreen
import com.example.pepsi.ui.receive.ReceiveProductsScreen
import com.example.pepsi.ui.saleshistory.SalesHistoryScreen
import com.example.pepsi.ui.sell.SellProductsScreen
import com.example.pepsi.ui.stocks.ViewStocksScreen
import com.example.pepsi.ui.supply.RecordSupplyScreen
import com.example.pepsi.ui.supply.SupplyRecordsScreen
import kotlinx.coroutines.launch

@Composable
fun PepsiNavHost(navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val isFactoryRoute = factoryDrawerScreens.any { it.route == currentRoute }
    val currentDrawerScreens = if (isFactoryRoute) factoryDrawerScreens else depotDrawerScreens
    val currentModuleHome = if (isFactoryRoute) Screen.FactoryOverview.route else Screen.DepotOverview.route

    fun navigateTo(screen: Screen) {
        scope.launch { drawerState.close() }
        navController.navigate(screen.route) {
            popUpTo(currentModuleHome) { inclusive = false }
            launchSingleTop = true
        }
    }

    fun openDrawer() {
        scope.launch { drawerState.open() }
    }

    fun switchRole() {
        scope.launch { drawerState.close() }
        navController.navigate(Screen.RoleSelection.route) {
            popUpTo(navController.graph.id) { inclusive = true }
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                screens = currentDrawerScreens,
                currentRoute = currentRoute,
                onItemClick = ::navigateTo,
                onSwitchRole = ::switchRole,
            )
        },
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.RoleSelection.route,
            modifier = Modifier.fillMaxSize(),
        ) {
            composable(Screen.RoleSelection.route) {
                RoleSelectionScreen(
                    onSelectDepot = {
                        navController.navigate(Screen.DepotOverview.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                    onSelectFactory = {
                        navController.navigate(Screen.FactoryOverview.route) {
                            popUpTo(Screen.RoleSelection.route) { inclusive = true }
                        }
                    },
                )
            }
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
                    onRecordProduction = { navigateTo(Screen.RecordProduction) },
                    onRecordSupply = { navigateTo(Screen.RecordSupply) },
                    onViewProductionRecords = { navigateTo(Screen.ProductionRecords) },
                    onViewSupplyRecords = { navigateTo(Screen.SupplyRecords) },
                )
            }
            composable(Screen.RecordProduction.route) {
                RecordProductionScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.RecordSupply.route) {
                RecordSupplyScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.ProductionRecords.route) {
                ProductionRecordsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.SupplyRecords.route) {
                SupplyRecordsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}
