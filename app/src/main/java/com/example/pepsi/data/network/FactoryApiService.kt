package com.example.pepsi.data.network

import com.example.pepsi.data.network.model.FactoryStockResponse
import com.example.pepsi.data.network.model.ProductionCreate
import com.example.pepsi.data.network.model.ProductionResponse
import com.example.pepsi.data.network.model.SupplyCreate
import com.example.pepsi.data.network.model.SupplyResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FactoryApiService {

    @POST("factory/production")
    suspend fun recordProduction(@Body request: ProductionCreate): ProductionResponse

    @GET("factory/production")
    suspend fun getProductionHistory(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
        @Query("date") date: String? = null,
        @Query("product_id") productId: Int? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: Int? = null,
    ): List<ProductionResponse>

    @GET("factory/production/{production_id}")
    suspend fun getProduction(@Path("production_id") productionId: Int): ProductionResponse

    @GET("factory/stock")
    suspend fun getFactoryStock(): List<FactoryStockResponse>

    @GET("factory/stock/{product_id}/{quantity_id}")
    suspend fun getFactoryStockItem(
        @Path("product_id") productId: Int,
        @Path("quantity_id") quantityId: Int,
    ): FactoryStockResponse

    @POST("factory/supplies")
    suspend fun createSupply(@Body request: SupplyCreate): SupplyResponse

    @GET("factory/supplies")
    suspend fun getSupplyHistory(
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 10,
        @Query("date") date: String? = null,
        @Query("product_id") productId: Int? = null,
        @Query("product_name") productName: String? = null,
        @Query("quantity") quantity: Int? = null,
        @Query("status") status: String? = null,
        @Query("depot_id") depotId: Int? = null,
    ): List<SupplyResponse>

    @GET("factory/supplies/{supply_id}")
    suspend fun getSupply(@Path("supply_id") supplyId: Int): SupplyResponse

    @DELETE("factory/supplies/{supply_id}")
    suspend fun deleteSupply(@Path("supply_id") supplyId: Int)
}
