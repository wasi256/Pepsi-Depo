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
import com.example.pepsi.systemadmin.audit.AuditLogsScreen
import com.example.pepsi.systemadmin.depos.DeposScreen as AdminDeposScreen
import com.example.pepsi.systemadmin.depos.RegisterDepoScreen
import com.example.pepsi.systemadmin.health.SystemHealthScreen
import com.example.pepsi.systemadmin.overview.OverviewScreen as AdminOverviewScreen
import com.example.pepsi.systemadmin.products.ProductsScreen as AdminProductsScreen
import com.example.pepsi.systemadmin.products.RegisterPriceScreen
import com.example.pepsi.systemadmin.products.RegisterProductScreen
import com.example.pepsi.systemadmin.products.RegisterQuantityScreen
import com.example.pepsi.systemadmin.settings.SettingsScreen
import com.example.pepsi.systemadmin.users.RegisterRoleScreen
import com.example.pepsi.systemadmin.users.RegisterUserScreen
import com.example.pepsi.systemadmin.users.UsersScreen
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
            composable(Screen.AdminOverview.route) {
                AdminOverviewScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.AdminUsers.route) {
                UsersScreen(
                    onMenuClick = ::openDrawer,
                    onRegisterUser = { navController.navigate(Screen.AdminRegisterUser.route) },
                    onRegisterRole = { navController.navigate(Screen.AdminRegisterRole.route) },
                )
            }
            composable(Screen.AdminRegisterUser.route) {
                RegisterUserScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.AdminRegisterRole.route) {
                RegisterRoleScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.AdminDepos.route) {
                AdminDeposScreen(
                    onMenuClick = ::openDrawer,
                    onRegisterDepo = { navController.navigate(Screen.AdminRegisterDepo.route) },
                )
            }
            composable(Screen.AdminRegisterDepo.route) {
                RegisterDepoScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.AdminProducts.route) {
                AdminProductsScreen(
                    onMenuClick = ::openDrawer,
                    onRegisterProduct = { navController.navigate(Screen.AdminRegisterProduct.route) },
                    onRegisterQuantity = { navController.navigate(Screen.AdminRegisterQuantity.route) },
                    onRegisterPrice = { navController.navigate(Screen.AdminRegisterPrice.route) },
                )
            }
            composable(Screen.AdminRegisterProduct.route) {
                RegisterProductScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.AdminRegisterQuantity.route) {
                RegisterQuantityScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.AdminRegisterPrice.route) {
                RegisterPriceScreen(onDone = { navController.popBackStack() })
            }
            composable(Screen.AdminAuditLogs.route) {
                AuditLogsScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.AdminSystemHealth.route) {
                SystemHealthScreen(onMenuClick = ::openDrawer)
            }
            composable(Screen.AdminSettings.route) {
                SettingsScreen(onMenuClick = ::openDrawer)
            }
        }
    }
}
