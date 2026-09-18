package com.example.pepsi.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pepsi.auth.AccessDeniedScreen
import com.example.pepsi.auth.AppAccess
import com.example.pepsi.auth.AuthSession
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
fun PepsiNavHost(startRoute: String, navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    fun navigateTo(screen: Screen) {
        scope.launch { drawerState.close() }
        navController.navigate(screen.route) {
            popUpTo(startRoute) { inclusive = false }
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
            startDestination = startRoute,
            modifier = Modifier.fillMaxSize(),
        ) {
            guarded(Screen.DepotOverview.route, ::openDrawer) {
                OverviewScreen(
                    onMenuClick = ::openDrawer,
                    onProfileClick = { navController.navigate(Screen.Profile.route) },
                    onViewStocks = { navigateTo(Screen.ViewStocks) },
                    onSellProducts = { navigateTo(Screen.SellProducts) },
                    onReceiveProducts = { navigateTo(Screen.ReceiveProducts) },
                )
            }
            guarded(Screen.SalesHistory.route, ::openDrawer) {
                SalesHistoryScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.ReceiveProducts.route, ::openDrawer) {
                ReceiveProductsScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.SellProducts.route, ::openDrawer) {
                SellProductsScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.ViewStocks.route, ::openDrawer) {
                ViewStocksScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.FactoryOverview.route, ::openDrawer) {
                FactoryOverviewScreen(
                    onMenuClick = ::openDrawer,
                    onProfileClick = { navController.navigate(Screen.FactoryProfile.route) },
                    onViewProducts = { navigateTo(Screen.FactoryProducts) },
                    onViewDepos = { navigateTo(Screen.FactoryDepos) },
                )
            }
            guarded(Screen.FactorySales.route, ::openDrawer) {
                SalesScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.FactoryProducts.route, ::openDrawer) {
                ProductsScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.FactoryDepos.route, ::openDrawer) {
                DeposScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.FactoryWorkers.route, ::openDrawer) {
                WorkersScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.Profile.route, ::openDrawer) {
                ProfileScreen(onBack = { navController.popBackStack() })
            }
            guarded(Screen.FactoryProfile.route, ::openDrawer) {
                FactoryProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { AuthSession.signOut() },
                )
            }
            guarded(Screen.AdminOverview.route, ::openDrawer) {
                AdminOverviewScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.AdminUsers.route, ::openDrawer) {
                UsersScreen(
                    onMenuClick = ::openDrawer,
                    onRegisterUser = { navController.navigate(Screen.AdminRegisterUser.route) },
                    onRegisterRole = { navController.navigate(Screen.AdminRegisterRole.route) },
                )
            }
            guarded(Screen.AdminRegisterUser.route, ::openDrawer) {
                RegisterUserScreen(onDone = { navController.popBackStack() })
            }
            guarded(Screen.AdminRegisterRole.route, ::openDrawer) {
                RegisterRoleScreen(onDone = { navController.popBackStack() })
            }
            guarded(Screen.AdminDepos.route, ::openDrawer) {
                AdminDeposScreen(
                    onMenuClick = ::openDrawer,
                    onRegisterDepo = { navController.navigate(Screen.AdminRegisterDepo.route) },
                )
            }
            guarded(Screen.AdminRegisterDepo.route, ::openDrawer) {
                RegisterDepoScreen(onDone = { navController.popBackStack() })
            }
            guarded(Screen.AdminProducts.route, ::openDrawer) {
                AdminProductsScreen(
                    onMenuClick = ::openDrawer,
                    onRegisterProduct = { navController.navigate(Screen.AdminRegisterProduct.route) },
                    onRegisterQuantity = { navController.navigate(Screen.AdminRegisterQuantity.route) },
                    onRegisterPrice = { navController.navigate(Screen.AdminRegisterPrice.route) },
                )
            }
            guarded(Screen.AdminRegisterProduct.route, ::openDrawer) {
                RegisterProductScreen(onDone = { navController.popBackStack() })
            }
            guarded(Screen.AdminRegisterQuantity.route, ::openDrawer) {
                RegisterQuantityScreen(onDone = { navController.popBackStack() })
            }
            guarded(Screen.AdminRegisterPrice.route, ::openDrawer) {
                RegisterPriceScreen(onDone = { navController.popBackStack() })
            }
            guarded(Screen.AdminAuditLogs.route, ::openDrawer) {
                AuditLogsScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.AdminSystemHealth.route, ::openDrawer) {
                SystemHealthScreen(onMenuClick = ::openDrawer)
            }
            guarded(Screen.AdminSettings.route, ::openDrawer) {
                SettingsScreen(onMenuClick = ::openDrawer)
            }
        }
    }
}

/** A destination that shows an access-denied screen unless the user's permissions cover [route]. */
private fun NavGraphBuilder.guarded(route: String, onMenuClick: () -> Unit, content: @Composable () -> Unit) {
    composable(route) {
        if (AppAccess.canAccessRoute(route)) content() else AccessDeniedScreen(onMenuClick)
    }
}
