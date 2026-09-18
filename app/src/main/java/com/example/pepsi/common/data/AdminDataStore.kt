package com.example.pepsi.common.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import com.example.pepsi.common.model.AuditLog
import java.util.Date

/**
 * Holds the small slice of Admin state that still has no backing endpoint
 * (audit logs, system health). Everything else — users, roles, depots,
 * products, quantities, prices — is fetched live from the real API.
 */
object AdminDataStore {

    val auditLogs = mutableStateListOf(
        AuditLog("LOG-001", Date(), "System Admin", "SYSTEM_START", "System initialized"),
    )

    val systemHealthPercent = mutableStateOf(96)

    // Historical readings preceding the live percent, used to draw the
    // Overview trend chart. The live value is always appended as the latest point.
    private val healthTrendHistory = listOf(90, 92, 91, 94, 95, 95)

    fun healthTrend(): List<Float> = (healthTrendHistory + systemHealthPercent.value).map { it.toFloat() }
}
