package com.example.pepsi.data.network

import com.example.pepsi.data.network.model.AdminProductDto
import com.example.pepsi.data.network.model.AdminQuantityDto
import com.example.pepsi.data.network.model.FactoryStockDto
import com.example.pepsi.data.network.model.PaginatedResponse
import com.example.pepsi.data.network.model.ProductionRequest
import com.example.pepsi.data.network.model.ProductionResponse
import com.example.pepsi.data.network.model.SupplyRequest
import com.example.pepsi.data.network.model.SupplyResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Endpoints documented at https://pepsi-depot-demo.onrender.com/docs
 */
interface FactoryApiService {

    @GET("admin/products")
    suspend fun getProducts(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
    ): PaginatedResponse<AdminProductDto>

    @GET("admin/quantities")
    suspend fun getQuantities(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
    ): PaginatedResponse<AdminQuantityDto>

    @POST("factory/production")
    suspend fun recordProduction(@Body request: ProductionRequest): ProductionResponse

    @GET("factory/production")
    suspend fun getProductionHistory(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
        @Query("date") date: String? = null,
        @Query("product_id") productId: Int? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: Int? = null,
    ): List<ProductionResponse>

    @POST("factory/supplies")
    suspend fun createSupply(@Body request: SupplyRequest): SupplyResponse

    @GET("factory/supplies")
    suspend fun getSupplyHistory(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
        @Query("date") date: String? = null,
        @Query("product_id") productId: Int? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: Int? = null,
    ): List<SupplyResponse>

    @GET("factory/stock")
    suspend fun getFactoryStock(): List<FactoryStockDto>
}
