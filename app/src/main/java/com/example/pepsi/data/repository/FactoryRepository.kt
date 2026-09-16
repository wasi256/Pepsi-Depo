package com.example.pepsi.data.repository

import com.example.pepsi.data.network.RetrofitClient
import com.example.pepsi.data.network.model.CurrentUserResponse
import com.example.pepsi.data.network.model.DepotRead
import com.example.pepsi.data.network.model.FactoryStockResponse
import com.example.pepsi.data.network.model.HttpValidationError
import com.example.pepsi.data.network.model.LoginRequest
import com.example.pepsi.data.network.model.PersonnelRead
import com.example.pepsi.data.network.model.ProductRead
import com.example.pepsi.data.network.model.ProductionCreate
import com.example.pepsi.data.network.model.ProductionResponse
import com.example.pepsi.data.network.model.QuantityRead
import com.example.pepsi.data.network.model.SupplyCreate
import com.example.pepsi.data.network.model.SupplyResponse
import com.example.pepsi.data.network.model.TokenResponse
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException

/**
 * Thin wrapper around the Factory API's Retrofit services that turns thrown
 * exceptions into a friendly message screens can show directly.
 */
object FactoryRepository {

    private val gson = Gson()

    private suspend fun <T> call(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: HttpException) {
        Result.failure(Exception(e.toFriendlyMessage()))
    } catch (e: IOException) {
        Result.failure(Exception("Couldn't reach the server. Check your connection and try again."))
    }

    private fun HttpException.toFriendlyMessage(): String {
        val body = response()?.errorBody()?.string()
        val detail = body?.let {
            runCatching { gson.fromJson(it, HttpValidationError::class.java) }.getOrNull()
        }
        val fieldMessage = detail?.detail?.firstOrNull()?.msg
        return fieldMessage ?: when (code()) {
            401 -> "Your session has expired. Please log in again."
            404 -> "Not found."
            409 -> "That conflicts with the current stock — refresh and try again."
            else -> "Something went wrong (HTTP ${code()})."
        }
    }

    suspend fun login(email: String, password: String): Result<TokenResponse> = call {
        RetrofitClient.authApi.login(LoginRequest(email, password))
    }

    suspend fun fetchCurrentUser(): Result<CurrentUserResponse> = call {
        RetrofitClient.authApi.me()
    }

    suspend fun fetchProducts(): Result<List<ProductRead>> = call {
        RetrofitClient.adminApi.getProducts(pageSize = 100).items
    }

    suspend fun fetchQuantities(): Result<List<QuantityRead>> = call {
        RetrofitClient.adminApi.getQuantities(pageSize = 100).items
    }

    suspend fun fetchDepots(): Result<List<DepotRead>> = call {
        RetrofitClient.adminApi.getDepots(pageSize = 100).items
    }

    /** Finds the "Depot/Depo Attendant" role and returns personnel assigned to it. */
    suspend fun fetchDepoAttendants(): Result<List<PersonnelRead>> = call {
        val roles = RetrofitClient.adminApi.getRoles(pageSize = 100).items
        val attendantRole = roles.firstOrNull {
            it.name.contains("depot attendant", ignoreCase = true) ||
                it.name.contains("depo attendant", ignoreCase = true) ||
                it.name.contains("attendant", ignoreCase = true)
        } ?: return@call emptyList()
        RetrofitClient.adminApi.getPersonnel(pageSize = 100, roleId = attendantRole.id).items
    }

    suspend fun recordProduction(
        productId: Int,
        quantityId: Int,
        quantityProduced: Int,
        productionDate: String?,
    ): Result<ProductionResponse> = call {
        RetrofitClient.factoryApi.recordProduction(
            ProductionCreate(productId, quantityId, quantityProduced, productionDate),
        )
    }

    suspend fun fetchProductionHistory(limit: Int = 10): Result<List<ProductionResponse>> = call {
        RetrofitClient.factoryApi.getProductionHistory(limit = limit)
    }

    suspend fun fetchStock(): Result<List<FactoryStockResponse>> = call {
        RetrofitClient.factoryApi.getFactoryStock()
    }

    suspend fun createSupply(
        productId: Int,
        quantityId: Int,
        amount: Int,
        depotId: Int,
        supplierId: Int,
    ): Result<SupplyResponse> = call {
        RetrofitClient.factoryApi.createSupply(SupplyCreate(productId, quantityId, amount, depotId, supplierId))
    }

    suspend fun fetchSupplyHistory(limit: Int = 10): Result<List<SupplyResponse>> = call {
        RetrofitClient.factoryApi.getSupplyHistory(limit = limit)
    }

    suspend fun deleteSupply(supplyId: Int): Result<Unit> = call {
        RetrofitClient.factoryApi.deleteSupply(supplyId)
    }
}
