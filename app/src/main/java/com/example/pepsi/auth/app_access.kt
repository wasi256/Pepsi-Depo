package com.example.pepsi.auth

import com.example.pepsi.navigation.Screen
import com.example.pepsi.navigation.adminDrawerScreens
import com.example.pepsi.navigation.depotDrawerScreens
import com.example.pepsi.navigation.factoryDrawerScreens

/*
 * Single source of truth for who can see and do what.
 *
 * The API decides which permissions a user holds (they come back from POST /auth/login
 * as "<module>:<action>" strings, e.g. "admin.depots:update"). Nothing here hard-codes
 * which role owns which permission; this file only describes the catalog of permissions
 * that exist and turns the user's granted permissions into routing and UI decisions:
 *
 *   - which drawer sections (Admin / Factory / Depot) and screens are visible,
 *   - which screen a user lands on after signing in,
 *   - whether the create / edit / delete controls of a module are shown.
 *
 * A system administrator holds every permission, so they see everything; a factory
 * manager or depot attendant only holds their own module's permissions, so only their
 * own section and its child screens appear.
 */

enum class PermissionAction(val key: String) {
    CREATE("create"),
    READ("read"),
    UPDATE("update"),
    DELETE("delete"),
}

/** The 12 permission modules on the platform (each has create/read/update/delete). */
object PermissionModule {
    const val ADMIN_DEPOTS = "admin.depots"
    const val ADMIN_PERSONNEL = "admin.personnel"
    const val ADMIN_PRICES = "admin.prices"
    const val ADMIN_PRODUCTS = "admin.products"
    const val ADMIN_QUANTITIES = "admin.quantities"
    const val ADMIN_ROLES = "admin.roles"
    const val AUTH_PERMISSIONS = "auth.permissions"
    const val AUTH_USERS = "auth.users"
    const val DEPOT_RESTOCK = "depot.restock"
    const val DEPOT_SALES = "depot.sales"
    const val FACTORY_PRODUCTION = "factory.production"
    const val FACTORY_SUPPLIES = "factory.supplies"

    val ALL = listOf(
        ADMIN_DEPOTS, ADMIN_PERSONNEL, ADMIN_PRICES, ADMIN_PRODUCTS, ADMIN_QUANTITIES, ADMIN_ROLES,
        AUTH_PERMISSIONS, AUTH_USERS, DEPOT_RESTOCK, DEPOT_SALES, FACTORY_PRODUCTION, FACTORY_SUPPLIES,
    )
}

object Permissions {
    fun of(module: String, action: PermissionAction): String = "$module:${action.key}"

    /** All 48 permissions in the system: "admin.depots:create" ... "factory.supplies:delete". */
    val ALL: List<String> = PermissionModule.ALL.flatMap { module ->
        PermissionAction.entries.map { action -> of(module, action) }
    }
}

/** The three top-level areas of the app, matching the drawer categories. */
enum class AppSection(val prefix: String, val label: String) {
    ADMIN("admin.", "Admin"),
    FACTORY("factory.", "Factory"),
    DEPOT("depot.", "Depot"),
}

object AppAccess {

    fun has(permission: String): Boolean = AuthSession.has(permission)

    fun can(module: String, action: PermissionAction): Boolean = has(Permissions.of(module, action))

    fun canRead(module: String) = can(module, PermissionAction.READ)
    fun canCreate(module: String) = can(module, PermissionAction.CREATE)
    fun canUpdate(module: String) = can(module, PermissionAction.UPDATE)
    fun canDelete(module: String) = can(module, PermissionAction.DELETE)

    /** A user can enter a section when they hold at least one permission belonging to it. */
    fun canAccessSection(section: AppSection): Boolean =
        AuthSession.permissions.any { it.startsWith(section.prefix) }

    fun visibleSections(): List<AppSection> = AppSection.entries.filter(::canAccessSection)

    /** Where to send the user right after signing in, or null when they hold no module permission. */
    fun homeRoute(): String? = when {
        canAccessSection(AppSection.ADMIN) -> Screen.AdminOverview.route
        canAccessSection(AppSection.FACTORY) -> Screen.FactoryOverview.route
        canAccessSection(AppSection.DEPOT) -> Screen.DepotOverview.route
        else -> null
    }

    /** The drawer entries of [section] that the current user is allowed to open. */
    fun visibleScreens(section: AppSection): List<Screen> {
        val screens = when (section) {
            AppSection.ADMIN -> adminDrawerScreens
            AppSection.FACTORY -> factoryDrawerScreens
            AppSection.DEPOT -> depotDrawerScreens
        }
        return screens.filter { canAccessRoute(it.route) }
    }

    fun canAccessRoute(route: String): Boolean = when (route) {
        // Admin
        Screen.AdminOverview.route,
        Screen.AdminAuditLogs.route,
        Screen.AdminSystemHealth.route,
        Screen.AdminSettings.route,
        -> canAccessSection(AppSection.ADMIN)

        Screen.AdminUsers.route ->
            canRead(PermissionModule.ADMIN_PERSONNEL) || canRead(PermissionModule.ADMIN_ROLES)
        Screen.AdminRegisterUser.route -> canCreate(PermissionModule.ADMIN_PERSONNEL)
        Screen.AdminRegisterRole.route -> canCreate(PermissionModule.ADMIN_ROLES)
        Screen.AdminDepos.route -> canRead(PermissionModule.ADMIN_DEPOTS)
        Screen.AdminRegisterDepo.route -> canCreate(PermissionModule.ADMIN_DEPOTS)
        Screen.AdminProducts.route ->
            canRead(PermissionModule.ADMIN_PRODUCTS) ||
                canRead(PermissionModule.ADMIN_QUANTITIES) ||
                canRead(PermissionModule.ADMIN_PRICES)
        Screen.AdminRegisterProduct.route -> canCreate(PermissionModule.ADMIN_PRODUCTS)
        Screen.AdminRegisterQuantity.route -> canCreate(PermissionModule.ADMIN_QUANTITIES)
        Screen.AdminRegisterPrice.route -> canCreate(PermissionModule.ADMIN_PRICES)

        // Factory
        Screen.FactoryOverview.route,
        Screen.FactorySales.route,
        Screen.FactoryProducts.route,
        Screen.FactoryDepos.route,
        Screen.FactoryWorkers.route,
        Screen.FactoryProfile.route,
        -> canAccessSection(AppSection.FACTORY)

        // Depot
        Screen.DepotOverview.route,
        Screen.ViewStocks.route,
        Screen.Profile.route,
        -> canAccessSection(AppSection.DEPOT)
        Screen.SalesHistory.route -> canRead(PermissionModule.DEPOT_SALES)
        Screen.SellProducts.route -> canCreate(PermissionModule.DEPOT_SALES)
        Screen.ReceiveProducts.route -> canRead(PermissionModule.DEPOT_RESTOCK)

        else -> true
    }
}
