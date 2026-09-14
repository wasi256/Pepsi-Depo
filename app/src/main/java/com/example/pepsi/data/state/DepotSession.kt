package com.example.pepsi.data.state

import androidx.compose.runtime.mutableStateOf
import com.example.pepsi.data.network.DepotApiService
import com.example.pepsi.data.network.model.AdminDepotDto

/**
 * Which depot the attendant is currently acting for. There's no auth yet, so this is picked
 * from the depot list rather than derived from a logged-in identity, and is shared across the
 * restock/stock screens so it only needs to be picked once per session.
 */
object DepotSession {

    val depots = mutableStateOf<List<AdminDepotDto>>(emptyList())
    val selectedDepot = mutableStateOf<AdminDepotDto?>(null)
    val isLoading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    suspend fun ensureLoaded(apiService: DepotApiService) {
        if (depots.value.isNotEmpty() || isLoading.value) return
        isLoading.value = true
        error.value = null
        try {
            val page = apiService.getDepots(page = 1, pageSize = 100)
            depots.value = page.items
            if (selectedDepot.value == null) selectedDepot.value = page.items.firstOrNull()
        } catch (e: Exception) {
            error.value = e.localizedMessage ?: "Failed to load depots."
        } finally {
            isLoading.value = false
        }
    }

    fun select(depot: AdminDepotDto) {
        selectedDepot.value = depot
    }
}
