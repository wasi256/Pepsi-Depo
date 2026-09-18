package com.example.pepsi.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.pepsi.data.model.RangePeriod
import com.example.pepsi.network.RetrofitClient
import com.example.pepsi.network.model.SupplyHistoryResponse
import com.example.pepsi.network.readErrorMessage
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Runs [action] whenever the screen becomes visible (including the first time), and again
 * when [key] changes. Depot screens use it so data is fresh after returning from another screen.
 */
@Composable
fun OnResume(key: Any? = null, action: suspend () -> Unit) {
    val scope = rememberCoroutineScope()
    val owner = LocalLifecycleOwner.current
    val latestAction by rememberUpdatedState(action)
    DisposableEffect(owner, key) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) scope.launch { latestAction() }
        }
        owner.lifecycle.addObserver(observer)
        onDispose { owner.lifecycle.removeObserver(observer) }
    }
}

/** "Load more" button shown under a paged list while further pages remain. */
@Composable
fun LoadMoreButton(visible: Boolean, isLoading: Boolean, onClick: () -> Unit) {
    if (!visible) return
    OutlinedButton(onClick = onClick, enabled = !isLoading, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(if (isLoading) "Loading…" else "Load more")
    }
}

fun formatMoney(amount: Double): String = "UGX %,.0f".format(amount)

private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

fun todayString(): String = dayFormat.format(Date())

/** First day (yyyy-MM-dd) covered by this period, counting back from today. */
fun RangePeriod.dateFrom(): String {
    val calendar = Calendar.getInstance()
    val daysBack = when (this) {
        RangePeriod.Today -> 0
        RangePeriod.Weekly -> 6
        RangePeriod.Monthly -> 29
        RangePeriod.Yearly -> 364
    }
    calendar.add(Calendar.DAY_OF_YEAR, -daysBack)
    return dayFormat.format(calendar.time)
}

private const val SUPPLY_PAGE_SIZE = 10
private const val MAX_SUPPLY_PAGES = 5

/**
 * Supplies the factory has sent that this depot hasn't confirmed or rejected yet. The API
 * pages supplies ten at a time and can't filter by status, so this reads up to five pages
 * (newest first) and keeps the pending ones.
 */
suspend fun loadPendingSupplies(): Result<List<SupplyHistoryResponse>> {
    return try {
        val pending = mutableListOf<SupplyHistoryResponse>()
        for (pageIndex in 0 until MAX_SUPPLY_PAGES) {
            val response = RetrofitClient.factorySupplyApi.listSupplies(skip = pageIndex * SUPPLY_PAGE_SIZE, limit = SUPPLY_PAGE_SIZE)
            if (!response.isSuccessful) return Result.failure(Exception(response.readErrorMessage()))
            val page = response.body().orEmpty()
            pending += page.filter { it.status.equals("pending", ignoreCase = true) }
            if (page.size < SUPPLY_PAGE_SIZE) break
        }
        Result.success(pending)
    } catch (e: Exception) {
        Result.failure(Exception("Network error: ${e.message}"))
    }
}
