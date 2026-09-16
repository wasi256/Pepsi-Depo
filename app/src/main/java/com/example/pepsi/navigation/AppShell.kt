package com.example.pepsi.navigation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.pepsi.R
import com.example.pepsi.systemadmin.audit.AuditLogsScreen
import com.example.pepsi.systemadmin.depos.DeposScreen
import com.example.pepsi.systemadmin.depos.RegisterDepoScreen
import com.example.pepsi.systemadmin.health.SystemHealthScreen
import com.example.pepsi.systemadmin.overview.OverviewScreen
import com.example.pepsi.systemadmin.products.ProductsScreen
import com.example.pepsi.systemadmin.products.RegisterPriceScreen
import com.example.pepsi.systemadmin.products.RegisterProductScreen
import com.example.pepsi.systemadmin.products.RegisterQuantityScreen
import com.example.pepsi.systemadmin.settings.SettingsScreen
import com.example.pepsi.systemadmin.users.RegisterUserScreen
import com.example.pepsi.systemadmin.users.UsersScreen
import kotlinx.coroutines.launch

/**
 * Shell for the system_admin module: a drawer (hamburger menu) around the
 * top-level destinations, with a back-arrow top bar for pushed sub-screens
 * such as Register User / Register Depo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppShell(navController: NavHostController = rememberNavController()) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    val isTopLevel = Screen.topLevel.any { it.route == currentRoute }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = isTopLevel,
        drawerContent = {
            AppDrawerContent(
                currentRoute = currentRoute,
                onDestinationClick = { screen ->
                    scope.launch { drawerState.close() }
                    if (screen.route != currentRoute) {
                        navController.navigate(screen.route) {
                            popUpTo(Screen.Overview.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
            )
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.pepsi_logo),
                                contentDescription = null,
                                modifier = Modifier.size(28.dp),
                            )
                            Text(
                                text = titleFor(currentRoute),
                                modifier = Modifier.padding(start = 10.dp),
                            )
                        }
                    },
                    navigationIcon = {
                        if (isTopLevel) {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                Icon(Icons.Filled.Menu, contentDescription = "Menu")
                            }
                        } else {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = MaterialTheme.colorScheme.onPrimary,
                        navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                        actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Overview.route,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Screen.Overview.route) {
                    OverviewScreen()
                }
                composable(Screen.Users.route) {
                    UsersScreen(onRegisterUser = { navController.navigate(Screen.RegisterUser.route) })
                }
                composable(Screen.RegisterUser.route) {
                    RegisterUserScreen(onDone = { navController.popBackStack() })
                }
                composable(Screen.Depos.route) {
                    DeposScreen(onRegisterDepo = { navController.navigate(Screen.RegisterDepo.route) })
                }
                composable(Screen.RegisterDepo.route) {
                    RegisterDepoScreen(onDone = { navController.popBackStack() })
                }
                composable(Screen.Products.route) {
                    ProductsScreen(
                        onRegisterProduct = { navController.navigate(Screen.RegisterProduct.route) },
                        onRegisterQuantity = { navController.navigate(Screen.RegisterQuantity.route) },
                        onRegisterPrice = { navController.navigate(Screen.RegisterPrice.route) },
                    )
                }
                composable(Screen.RegisterProduct.route) {
                    RegisterProductScreen(onDone = { navController.popBackStack() })
                }
                composable(Screen.RegisterQuantity.route) {
                    RegisterQuantityScreen(onDone = { navController.popBackStack() })
                }
                composable(Screen.RegisterPrice.route) {
                    RegisterPriceScreen(onDone = { navController.popBackStack() })
                }
                composable(Screen.AuditLogs.route) {
                    AuditLogsScreen()
                }
                composable(Screen.SystemHealth.route) {
                    SystemHealthScreen()
                }
                composable(Screen.Settings.route) {
                    SettingsScreen()
                }
            }
        }
    }
}

private fun titleFor(route: String?): String = when (route) {
    Screen.RegisterUser.route -> Screen.RegisterUser.title
    Screen.RegisterDepo.route -> Screen.RegisterDepo.title
    Screen.RegisterProduct.route -> Screen.RegisterProduct.title
    Screen.RegisterQuantity.route -> Screen.RegisterQuantity.title
    Screen.RegisterPrice.route -> Screen.RegisterPrice.title
    else -> Screen.topLevel.firstOrNull { it.route == route }?.title ?: "Pepsi Depo"
}
